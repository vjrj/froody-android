package io.github.froodyapp.service;

import android.app.Activity;

import com.google.gson.JsonParseException;

import io.github.froodyapp.App;
import io.github.froodyapp.api.api.EntryApi;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;

/**
 * Helper for loading extended informations about an FroodyEntry
 */
public class EntryDetailsLoader extends Thread {
    //########################
    //## Member
    //########################
    final Activity activity;
    final FroodyEntryPlus entry;
    final ExtendedInfoLoaderListener listener;
    final String requestedBy;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param activity activity to post result to
     * @param entry    the froodyEntry
     * @param listener callback listener (activity)
     */
    public EntryDetailsLoader(final Activity activity, final FroodyEntryPlus entry, final ExtendedInfoLoaderListener listener, String requestedBy) {
        this.activity = activity;
        this.entry = entry;
        this.listener = listener;
        this.requestedBy = requestedBy;
    }

    @Override
    public void run() {
        EntryApi api = new EntryApi();
        try {
            FroodyEntry result = api.entryByIdGet(entry.getEntryId());
            if (result != null) {
                entry.setContact(result.getContact());
                entry.setDescription(result.getDescription());
                entry.setAddress(result.getAddress());
                entry.setGeohash(result.getGeohash());
                entry.loadLocationFromGeohash();
                entry.setCreationDate(result.getCreationDate());
                entry.setModificationDate(result.getModificationDate());
                entry.setEntryType(result.getEntryType());
                entry.setCertificationType(result.getCertificationType());
                entry.setDistributionType(result.getDistributionType());
                entry.setWasDeleted(result.getWasDeleted());

                postResult();
            }
        } catch (ApiException | JsonParseException e) {
            App.log(getClass(), "ERROR: Could not get details of froodyEntry " + e.getMessage());
        }
    }

    /**
     * Post the result to activity
     */
    private void postResult() {
        AppCast.FROODY_ENTRY_DETAILS_LOADED.send(activity.getApplicationContext(), entry, requestedBy);
        if (activity != null && listener != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    listener.onFroodyEntryExtendedInfoLoaded(entry);
                }
            });
        }
    }

    /**
     * Callback listener
     */
    public interface ExtendedInfoLoaderListener {
        /**
         * Extended FroodyEntry infos were loaded
         *
         * @param entry ref to froodyEntry
         */
        void onFroodyEntryExtendedInfoLoaded(FroodyEntryPlus entry);
    }
}
