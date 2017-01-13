package io.github.froodyapp.service;

import android.content.Context;

import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.util.List;

import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.activity.MapOSMFragment;
import io.github.froodyapp.api.api.BlockApi;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.BlockInfo;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.BlockInfoPlus;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.Helpers;

/**
 * Task for loading blocks via API
 */
public class EntryByBlockLoader extends Thread {
    //########################
    //## Member
    //########################
    private final Context context;
    private final double lat;
    private final double lng;
    private final int zoom;


    //########################
    //## Methods
    //########################

    //public EntryByBlockLoader(Context context, MapView map, long userId) {
    //    this(context, map.getMapCenter().getLatitude(), map.getMapCenter().getLongitude(), map.getZoomLevel(), userId);
    //}

    public EntryByBlockLoader(Context context, double lat, double lng, int zoom) {
        this.context = context;
        this.lat = lat;
        this.lng = lng;
        this.zoom = zoom;
    }

    @Override
    public void run() {
        int precisionToLoad = zoom < MapOSMFragment.ZOOMLEVEL_BLOCK6_TRESHOLD ? 5 : 6;
        String geohash = GeoHash.withCharacterPrecision(lat, lng, precisionToLoad).toBase32();
        BlockCache blockCache = BlockCache.getInstance();

        BlockInfo blockInfo;
        BlockCache.BlockCacheItem blockCacheItem = blockCache.getBlockCacheItemAt(geohash);
        if (blockCacheItem == null) {
            blockInfo = new BlockInfoPlus(geohash, Helpers.getNow().minusWeeks(3));
        } else {
            blockInfo = blockCacheItem.blockInfo;
        }

        BlockApi blockApi = new BlockApi();
        try {
            // Request info for Block
            List<BlockInfo> blockInfos = blockApi.blockInfoGet(geohash, blockInfo.getModificationDate());
            for (BlockInfoPlus bpu : blockCache.processBlockInfosAndGetModified(blockInfos)) {
                // For every block
                try {
                    // Request new/modified blocks from server
                    DateTime requestedAt = Helpers.getNow();
                    List<FroodyEntry> entries = blockApi.blockGetGet(bpu.getGeohash(), bpu.getPreviousModificationDate());

                    // Process entries from server into local cache
                    List<FroodyEntryPlus> newOrModifiedEntries = blockCache.processEntries(entries, requestedAt);
                    publishNewOrModifiedEntries(newOrModifiedEntries);
                } catch (ApiException | JsonParseException e) {
                    App.log(getClass(), "ERROR: Getting Block " + e.getMessage());
                }
            }

        } catch (ApiException | JsonParseException | NullPointerException e) {
            App.log(getClass(), "ERROR: Getting BlockInfo " + e.getMessage());
        }
    }

    private void publishNewOrModifiedEntries(List<FroodyEntryPlus> response) {
        if (response != null && response.size() > 0 && context != null) {
            AppCast.FROODY_ENTRIES_LOADED.send(context, response);
        }
    }
}
