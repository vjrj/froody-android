package io.github.froodyapp.util;

import android.content.Context;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import io.github.froodyapp.App;
import io.github.froodyapp.api.model_.BlockInfo;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.BlockInfoPlus;
import io.github.froodyapp.model.FroodyEntryPlus;

public class BlockCache {
    //#####################
    //##      Statics
    //#####################
    public static class BlockCacheItem implements Serializable {
        public ConcurrentHashMap<Long, FroodyEntryPlus> entries = new ConcurrentHashMap<>();  // = HashMap
        public BlockInfoPlus blockInfo;
    }

    public static class BlockCacheHolder {
        ConcurrentHashMap<String, BlockCacheItem> map;
    }

    private static BlockCache instance;
    //######################
    //##  Member
    //######################
    private ConcurrentHashMap<String, BlockCacheItem> cacheMap;

    //######################
    //##  Methods
    //######################
    private BlockCache() {
        cacheMap = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadFromAppSettings(Context context) {
        try {
            File file = new File(context.getCacheDir(), "map.dat");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            ConcurrentHashMap<String, BlockCacheItem> mapDat = (ConcurrentHashMap<String, BlockCacheItem>) inputStream.readObject();
            if (mapDat != null) {
                cacheMap = mapDat;
            }
            inputStream.close();
        } catch (Exception e) {
            App.log(getClass(), "Error: Cannot load CacheMap from cache---" + e.getMessage());
        }
    }

    public synchronized void saveToAppSettings(Context context) {
        try {
            File file = new File(context.getCacheDir(), "map.dat");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(cacheMap);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            App.log(getClass(), "Error: Cannot save CacheMap to cache---" + e.getMessage());
        }
    }

    public synchronized Vector<BlockInfoPlus> processBlockInfosAndGetModified(List<BlockInfo> blockInfosServer) {
        Vector<BlockInfoPlus> blocksModifiedOnServerButNotLocal = new Vector<>();

        for (BlockInfo infoServer : blockInfosServer) {
            BlockCacheItem item = getBlockCacheItemAt(infoServer.getGeohash());
            if (item == null) {
                item = new BlockCacheItem();
                item.blockInfo = new BlockInfoPlus(infoServer);
            } else {
                item.blockInfo.setNewerBlockInfo(infoServer);
            }

            if (item.blockInfo.getHasBlockBeenModified()) {
                blocksModifiedOnServerButNotLocal.add(item.blockInfo);
            }
            updateBlockCache(item);
        }
        return blocksModifiedOnServerButNotLocal;
    }

    /**
     * Process entries, returns a list of new or modified entries. Deleted ones got wasDeleted set to true
     *
     * @param entriesFromServer The entries received from a /block/get/ call
     * @param requestedAt       When the entries where requested
     * @return A List of deleted,added or modified entries
     */
    public synchronized List<FroodyEntryPlus> processEntries(List<FroodyEntry> entriesFromServer, DateTime requestedAt) {
        List<FroodyEntryPlus> retEntries = new ArrayList<>();
        for (FroodyEntry fe : entriesFromServer) {
            FroodyEntryPlus entry = new FroodyEntryPlus(fe);
            String block6 = entry.getGeohashWithPrecision(6);
            if (block6 == null) {
                continue;   // Won't happen, if server is doing well
            }

            BlockCacheItem cacheItem = getBlockCacheItemAt(block6);
            if (cacheItem == null) {
                cacheItem = new BlockCacheItem();
                cacheItem.blockInfo = new BlockInfoPlus(block6, requestedAt);
            } else {
                cacheItem.blockInfo.setModificationDate(requestedAt);
            }

            // Process cached entries
            FroodyEntryPlus entryOld = cacheItem.entries.get(entry.getEntryId());
            if (entryOld != null) {
                if (entryOld.hasExtendedInfoLoaded()) {
                    entry.setAddress(entryOld.getAddress());
                    entry.setContact(entryOld.getContact());
                    entry.setDescription(entryOld.getDescription());
                }
                cacheItem.entries.remove(entry.getEntryId());
            }


            //TODO: Maybe compare to -3 weeks -> Import from file cache algorithm
            if (!entry.getWasDeleted()) {
                // New or modified entry
                cacheItem.entries.put(entry.getEntryId(), entry);
            }

            retEntries.add(entry);
            updateBlockCache(cacheItem);
        }
        return retEntries;
    }

    public void processExtendedEntry(FroodyEntryPlus entry) {
        String block6 = entry.getGeohashWithPrecision(6);
        BlockCacheItem cacheItem = getBlockCacheItemAt(block6);
        if (block6 != null) {
            if (cacheItem == null) {
                // Won't happen, because extended entries will retrieved by existing ones
                cacheItem = new BlockCacheItem();
                cacheItem.blockInfo = new BlockInfoPlus(block6, Helpers.getNow());
            }

            // Delete from cache
            if (cacheItem.entries.get(entry.getEntryId()) != null) {
                cacheItem.entries.remove(entry.getEntryId());
            }

            // Update cache if it was not deleted
            if (!entry.getWasDeleted()) {
                // New or modified entry
                cacheItem.entries.put(entry.getEntryId(), entry);
            }
            updateBlockCache(cacheItem);
        }
    }

    public FroodyEntryPlus tryGetEntryByIdFromCache(FroodyEntryPlus entry) {
        String block6 = entry.getGeohashWithPrecision(6);
        BlockCacheItem cacheItem = getBlockCacheItemAt(block6);

        if (block6 != null && cacheItem != null) {
            // Iterate all entries in BlockCacheItem
            for (int i = 0, size = cacheItem.entries.size(); i < size; i++) {
                for (FroodyEntryPlus entryFromCache : cacheItem.entries.values()) {
                    if (entryFromCache.getEntryId().equals(entry.getEntryId())) {
                        return entryFromCache;
                    }
                }
            }
        }
        return null;
    }

    public List<FroodyEntryPlus> getAllCachedEntries() {
        List<FroodyEntryPlus> entries = new ArrayList<>();
        // Iterate all items in cacheMap
        for (Map.Entry<String, BlockCacheItem> stringBlockCacheItemEntry : cacheMap.entrySet()) {
            BlockCacheItem blockCacheItem = stringBlockCacheItemEntry.getValue();
            entries.addAll(new ArrayList<>(blockCacheItem.entries.values()));

            // Iterate all entries in BlockCacheItem
            //for (int i = 0, size = blockCacheItem.entries.size(); i < size; i++) {
            //    FroodyEntryPlus entry = blockCacheItem.entries.valueAt(i);
            //    entries.add(entry);
            //}
        }
        return entries;
    }

    /**
     * Get block cache at specific blockGeohash. Returns null if nothing was cached yet
     *
     * @param blockGeohash The geohash to request at
     * @return A BlockCacheItem or null if nothing was inserted yet
     */
    public BlockCacheItem getBlockCacheItemAt(String blockGeohash) {
        return cacheMap.get(blockGeohash);
    }

    /**
     * Update (=Replace) the BlockCacheItem at item.blockInfo.geoHash
     *
     * @param item The item to place
     */
    private void updateBlockCache(BlockCacheItem item) {
        cacheMap.put(item.blockInfo.getGeohash(), item);
    }

    public void clearCache(Context context) {
        cacheMap = new ConcurrentHashMap<>();
        saveToAppSettings(context);
    }


    //######################
    //##  Getter / Setter
    //######################
    public ConcurrentHashMap<String, BlockCacheItem> getCacheMap() {
        return cacheMap;
    }

    public void setCacheMap(ConcurrentHashMap<String, BlockCacheItem> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public static BlockCache getInstance() {
        if (instance == null) {
            instance = new BlockCache();
        }
        return instance;
    }
}
