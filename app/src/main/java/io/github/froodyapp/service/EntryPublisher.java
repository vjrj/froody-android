package io.github.froodyapp.service;

import android.app.Activity;

import com.google.gson.JsonParseException;

import io.github.froodyapp.App;
import io.github.froodyapp.api.api.EntryApi;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.ResponseEntryAdd;
import io.github.froodyapp.model.FroodyEntryPlus;

/**
 * Helper for loading extended informations about an FroodyEntry
 */
public class EntryPublisher extends Thread {
    //########################
    //## Member
    //########################
    private final Activity activity;
    private final FroodyEntryPlus froodyEntry;
    private final EntryPublishListener listener;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param activity    activity to post result to
     * @param froodyEntry the froodyEntry
     * @param listener    callback listener (activity)
     */
    public EntryPublisher(final Activity activity, final FroodyEntryPlus froodyEntry, final EntryPublishListener listener) {
        this.activity = activity;
        this.froodyEntry = froodyEntry;
        this.listener = listener;
    }

    @Override
    public void run() {
        EntryApi entryApi = new EntryApi();
        try {
            ResponseEntryAdd responseEntryAdd = entryApi.entryAddPost(froodyEntry.getUserId(), froodyEntry.getGeohash(), froodyEntry.getEntryType(), froodyEntry.getDistributionType(), froodyEntry.getCertificationType(), froodyEntry.getDescription(), froodyEntry.getContact(), froodyEntry.getAddress());
            postResult(responseEntryAdd, true);
        } catch (ApiException | JsonParseException e) {
            App.log(getClass(), "ERROR: Adding Entry " + e.getMessage());
            postResult(null, false);
        }
    }


    /**
     * Post the result to activity
     */

    private void postResult(final ResponseEntryAdd responseEntryAdd, final boolean wasAdded) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (listener != null) {
                        listener.onFroodyEntryPublished(responseEntryAdd, wasAdded);
                    }
                }
            });
        }
    }

    /**
     * Callback listener
     */
    public interface EntryPublishListener {
        void onFroodyEntryPublished(ResponseEntryAdd response, boolean wasAdded);
    }
}
