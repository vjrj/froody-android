package io.github.froodyapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.activity.MapOSMFragment;
import io.github.froodyapp.api.model_.FroodyUser;

/**
 * Settings Wrapper for the app
 */
public class AppSettings {
    private static final String ARRAY_SEPERATOR = "%%";
    public static final String SHARED_PREF_APP = "app";
    //#####################
    //## Members
    //#####################
    private final SharedPreferences prefApp;
    private final Context context;

    //#####################
    //## Methods
    //#####################

    /**
     * Constructor
     *
     * @param context Android context
     */
    public AppSettings(Context context) {
        this.context = context.getApplicationContext();
        prefApp = this.context.getSharedPreferences(SHARED_PREF_APP, Context.MODE_PRIVATE);
    }

    private String tr(int stringResourceId) {
        return context.getString(stringResourceId);
    }

    @SuppressLint("CommitPrefEdits")
    public void resetSettings() {
        prefApp.edit().clear().commit();
    }

    //###########################################
    //## Helpers for setting/getting based on
    //## type; Key based on resources
    //############################################
    private void setString(int keyResId, String value) {
        prefApp.edit().putString(tr(keyResId), value).apply();
    }

    private String getString(int keyResId, String defaultValue) {
        return prefApp.getString(tr(keyResId), defaultValue);
    }

    private void setInt(int keyResId, int value) {
        prefApp.edit().putInt(tr(keyResId), value).apply();
    }

    private int getInt(int keyResId, int defaultValue) {
        return prefApp.getInt(tr(keyResId), defaultValue);
    }

    private void setLong(int keyResId, long value) {
        prefApp.edit().putLong(tr(keyResId), value).apply();
    }

    private long getLong(int keyResId, long defaultValue) {
        return prefApp.getLong(tr(keyResId), defaultValue);
    }

    private void setBool(int keyResId, boolean value) {
        prefApp.edit().putBoolean(tr(keyResId), value).apply();
    }

    private boolean getBool(int keyResId, boolean defaultValue) {
        return prefApp.getBoolean(tr(keyResId), defaultValue);
    }

    private void setDouble(int keyResId, double value) {
        prefApp.edit().putLong(tr(keyResId), Double.doubleToRawLongBits(value)).apply();
    }

    private double getDouble(int keyResId, double defaultValue) {
        if (!prefApp.contains(tr(keyResId))) {
            return defaultValue;
        }
        return Double.longBitsToDouble(prefApp.getLong(tr(keyResId), 0));
    }

    private void setIntList(int keyResId, ArrayList<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPERATOR);
            sb.append(Integer.toString(value));
        }
        setString(keyResId, sb.toString().replaceFirst(ARRAY_SEPERATOR, ""));
    }

    private ArrayList<Integer> getIntList(int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = prefApp.getString(tr(keyResId), ARRAY_SEPERATOR);
        if (value.equals(ARRAY_SEPERATOR)) {
            return ret;
        }
        for (String s : value.split(ARRAY_SEPERATOR)) {
            ret.add(Integer.parseInt(s));
        }
        return ret;
    }

    //#################################
    //#################################
    //##
    //## Getter & Setter for settings
    //##
    //#################################
    public long getFroodyUserId() {
        return getLong(R.string.pref_key__user_id, -1);
    }

    public boolean hasFroodyUserId() {
        return getFroodyUserId() != -1;
    }

    public FroodyUser getFroodyUser() {
        if (hasFroodyUserId()) {
            FroodyUser u = new FroodyUser();
            u.setUserId(getFroodyUserId());
            return u;
        }
        return null;
    }

    public void setFroodyUserId(long value) {
        setLong(R.string.pref_key__user_id, value);
    }


    public String getFroodyServer() {
        return getString(R.string.pref_key__froody_server, context.getString(R.string.server_default));
    }

    public void setFroodyServer(String value) {
        setString(R.string.pref_key__froody_server, value);
    }

    public boolean hasLastLocationOnMap() {
        return getLastLocationZoom() >= MapOSMFragment.ZOOMLEVEL_BLOCK5_TRESHOLD;
    }

    public double getLastLocationLatitude() {
        return getDouble(R.string.pref_key__lastlocation_latitude, 0);
    }

    public double getLastLocationLongitude() {
        return getDouble(R.string.pref_key__lastlocation_longitude, 0);
    }

    public int getLastLocationZoom() {
        return getInt(R.string.pref_key__lastlocation_zoom, 0);
    }

    public void setLastLocation(double lat, double lng, int zoom) {
        setDouble(R.string.pref_key__lastlocation_latitude, lat);
        setDouble(R.string.pref_key__lastlocation_longitude, lng);
        setInt(R.string.pref_key__lastlocation_zoom, zoom);
    }

    public int getLastCertification() {
        return getInt(R.string.pref_key__entry__last_certification, 0);
    }

    public void setLastCertification(int value) {
        setInt(R.string.pref_key__entry__last_certification, value);
    }

    public String getLastContactInfo() {
        return getString(R.string.pref_key__entry__last_contact_info, "");
    }

    public void setLastContactInfo(String value) {
        setString(R.string.pref_key__entry__last_contact_info, value);
    }

    public int getLastDistribution() {
        return getInt(R.string.pref_key__entry__last_distribution, 0);
    }


    public void setLastEntryTypes(ArrayList<Integer> values) {
        setIntList(R.string.pref_key__entry__last_entry_types, values);
    }

    public ArrayList<Integer> getLastEntryTypes() {
        return getIntList(R.string.pref_key__entry__last_entry_types);
    }

    public void setLastDistribution(int value) {
        setInt(R.string.pref_key__entry__last_distribution, value);
    }

    public String getMyEntries() {
        return getString(R.string.pref_key__my_entries, "[]");
    }

    public void setMyEntries(String value) {
        setString(R.string.pref_key__my_entries, value);
    }

    public boolean getAllowLocationListeningGps() {
        return getBool(R.string.pref_key__allow_location_listening_gps, true);
    }

    public boolean getAllowLocationListeningNetwork() {
        return getBool(R.string.pref_key__allow_location_listening_net, true);
    }

    public boolean getAllowLocationListeningAny() {
        return getAllowLocationListeningGps() || getAllowLocationListeningNetwork();
    }

    public ConcurrentHashMap<String, BlockCache.BlockCacheItem> getLastBlockCache() {
        String text = getString(R.string.pref_key__entry__blockcache, null);
        try {
            BlockCache.BlockCacheHolder blockCacheHolder = new Gson().fromJson(text, BlockCache.BlockCacheHolder.class);
            if (blockCacheHolder != null && blockCacheHolder.map != null) {
                return blockCacheHolder.map;
            }
        } catch (Exception e) {
            App.log(getClass(), "Error: Empty BlockCache");
        }
        return new ConcurrentHashMap<>();
    }

    public void setLastBlockCache(ConcurrentHashMap<String, BlockCache.BlockCacheItem> blockCache) {
        BlockCache.BlockCacheHolder blockCacheHolder = new BlockCache.BlockCacheHolder();
        blockCacheHolder.map = blockCache;
        String json = new Gson().toJson(blockCacheHolder, BlockCache.BlockCacheHolder.class);
        setString(R.string.pref_key__entry__blockcache, json);
    }

}