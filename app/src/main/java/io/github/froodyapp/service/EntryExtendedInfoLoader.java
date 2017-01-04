package io.github.froodyapp.service;

import android.app.Activity;

import com.google.gson.JsonParseException;

import java.util.List;
import java.util.Map;

import io.github.froodyapp.App;
import io.github.froodyapp.api.api.EntryApi;
import io.github.froodyapp.api.invoker.ApiCallback;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;

/**
 * Helper for loading extended informations about an FroodyEntry
 */
public class EntryExtendedInfoLoader extends Thread implements ApiCallback<FroodyEntry> {
    //########################
    //## Member
    //########################
    final Activity activity;
    final FroodyEntryPlus entry;
    final ExtendedInfoLoaderListener listener;

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
    public EntryExtendedInfoLoader(final Activity activity, final FroodyEntryPlus entry, final ExtendedInfoLoaderListener listener) {
        this.activity = activity;
        this.entry = entry;
        this.listener = listener;
    }

    @Override
    public void run() {
        EntryApi api = new EntryApi();
        try {
            api.entryByIdGetAsync(entry.getEntryId(), this);
        } catch (ApiException | JsonParseException e) {
            App.log(getClass(), "ERROR: Could not get details of froodyEntry " + e.getMessage());
        }
    }

    @Override
    public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
        App.log(getClass(), "ERROR: Could not get details of froodyEntry " + e.getMessage());
    }

    @Override
    public void onSuccess(FroodyEntry result, int statusCode, Map<String, List<String>> responseHeaders) {
        if (result != null) {
            entry.setContact(result.getContact());
            entry.setDescription(result.getDescription());
            entry.setAddress(result.getAddress());

            postResult();
        }
    }

    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
    }

    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
    }

    /**
     * Post the result to activity
     */
    private void postResult() {
        AppCast.FROODY_ENTRY_DETAILS_LOADED.send(activity.getApplicationContext(), entry);
        if (activity != null) {
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
