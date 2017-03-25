package io.github.froodyapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import io.github.froodyapp.BuildConfig;
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

    @SuppressLint("CommitPrefEdits")
    public void resetSettings() {
        prefApp.edit().clear().commit();
    }

    //###########################################
    //## Helpers for setting/getting based on
    //## type; Key based on resources
    //############################################

    public Context getApplicationContext() {
        return context;
    }

    public void clearAppSettings() {
        prefApp.edit().clear().commit();
    }

    public String getKey(int stringKeyResourceId) {
        return context.getString(stringKeyResourceId);
    }

    public boolean isKeyEqual(String key, int stringKeyRessourceId) {
        return key.equals(getKey(stringKeyRessourceId));
    }

    private void setString(SharedPreferences pref, int keyRessourceId, String value) {
        pref.edit().putString(context.getString(keyRessourceId), value).apply();
    }

    private void setInt(SharedPreferences pref, int keyRessourceId, int value) {
        pref.edit().putInt(context.getString(keyRessourceId), value).apply();
    }

    private void setLong(SharedPreferences pref, int keyRessourceId, long value) {
        pref.edit().putLong(context.getString(keyRessourceId), value).apply();
    }

    private void setBool(SharedPreferences pref, int keyRessourceId, boolean value) {
        pref.edit().putBoolean(context.getString(keyRessourceId), value).apply();
    }

    private void setStringArray(SharedPreferences pref, int keyRessourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append("%%%");
            sb.append(value.toString());
        }
        setString(pref, keyRessourceId, sb.toString().replaceFirst("%%%", ""));
    }

    private String[] getStringArray(SharedPreferences pref, int keyRessourceId) {
        String value = pref.getString(context.getString(keyRessourceId), "%%%");
        if (value.equals("%%%")) {
            return new String[0];
        }
        return value.split("%%%");
    }

    private String getString(SharedPreferences pref, int ressourceId, String defaultValue) {
        return pref.getString(context.getString(ressourceId), defaultValue);
    }

    private String getString(SharedPreferences pref, int ressourceId, int ressourceIdDefaultValue) {
        return pref.getString(context.getString(ressourceId), context.getString(ressourceIdDefaultValue));
    }

    private boolean getBool(SharedPreferences pref, int ressourceId, boolean defaultValue) {
        return pref.getBoolean(context.getString(ressourceId), defaultValue);
    }

    private int getInt(SharedPreferences pref, int ressourceId, int defaultValue) {
        return pref.getInt(context.getString(ressourceId), defaultValue);
    }

    private long getLong(SharedPreferences pref, int ressourceId, long defaultValue) {
        return pref.getLong(context.getString(ressourceId), defaultValue);
    }

    public int getColor(SharedPreferences pref, String key, int defaultColor) {
        return pref.getInt(key, defaultColor);
    }

    public int getColorRes(@ColorRes int resColorId) {
        return ContextCompat.getColor(context, resColorId);
    }

    private void setDouble(SharedPreferences pref, int keyResId, double value) {
        prefApp.edit().putLong(context.getString(keyResId), Double.doubleToRawLongBits(value)).apply();
    }

    private double getDouble(SharedPreferences pref, int keyResId, double defaultValue) {
        if (!prefApp.contains(context.getString(keyResId))) {
            return defaultValue;
        }
        return Double.longBitsToDouble(prefApp.getLong(context.getString(keyResId), 0));
    }

    private void setIntList(SharedPreferences pref, int keyResId, ArrayList<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPERATOR);
            sb.append(Integer.toString(value));
        }
        setString(prefApp, keyResId, sb.toString().replaceFirst(ARRAY_SEPERATOR, ""));
    }

    private ArrayList<Integer> getIntList(SharedPreferences pref, int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = getString(prefApp, keyResId, ARRAY_SEPERATOR);
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
        return getLong(prefApp, R.string.pref_key__user_id, -1);
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
        setLong(prefApp, R.string.pref_key__user_id, value);
    }


    public String getFroodyServer() {
        return getString(prefApp, R.string.pref_key__froody_server, context.getString(R.string.server_default));
    }

    public boolean hasLastMapLocation() {
        return getLastMapLocationZoom() >= MapOSMFragment.ZOOMLEVEL_BLOCK4_TRESHOLD;
    }

    public double getLastMapLocationLatitude() {
        return getDouble(prefApp, R.string.pref_key__last_map_location_latitude, 0);
    }

    public double getLastMapLocationLongitude() {
        return getDouble(prefApp, R.string.pref_key__last_map_location_longitude, 0);
    }

    public int getLastMapLocationZoom() {
        return getInt(prefApp, R.string.pref_key__last_map_location_zoom, 0);
    }

    public void setLastMapLocation(double lat, double lng, int zoom) {
        setDouble(prefApp, R.string.pref_key__last_map_location_latitude, lat);
        setDouble(prefApp, R.string.pref_key__last_map_location_longitude, lng);
        setInt(prefApp, R.string.pref_key__last_map_location_zoom, zoom);
    }

    public int getLastCertification() {
        return getInt(prefApp, R.string.pref_key__entry__last_certification, 0);
    }

    public void setLastCertification(int value) {
        setInt(prefApp, R.string.pref_key__entry__last_certification, value);
    }

    public String getLastContactInfo() {
        return getString(prefApp, R.string.pref_key__entry__last_contact_info, "");
    }

    public void setLastContactInfo(String value) {
        setString(prefApp, R.string.pref_key__entry__last_contact_info, value);
    }

    public int getLastDistribution() {
        return getInt(prefApp, R.string.pref_key__entry__last_distribution, 0);
    }


    public void setLastEntryTypes(ArrayList<Integer> values) {
        setIntList(prefApp, R.string.pref_key__entry__last_entry_types, values);
    }

    public ArrayList<Integer> getLastEntryTypes() {
        return getIntList(prefApp, R.string.pref_key__entry__last_entry_types);
    }

    public void setLastDistribution(int value) {
        setInt(prefApp, R.string.pref_key__entry__last_distribution, value);
    }

    public boolean getAllowLocationListeningGps() {
        return getBool(prefApp, R.string.pref_key__allow_location_listening_gps, true);
    }

    public boolean getAllowLocationListeningNetwork() {
        return getBool(prefApp, R.string.pref_key__allow_location_listening_net, true);
    }

    public boolean getAllowLocationListeningAny() {
        return getAllowLocationListeningGps() || getAllowLocationListeningNetwork();
    }

    public void setLastFoundLocation(String geohash, String address) {
        setString(prefApp, R.string.pref_key__last_found_location_address, address);
        setString(prefApp, R.string.pref_key__last_found_location_geohash, geohash);
    }

    public String[] getLastFoundLocation() {
        return new String[]{
                getString(prefApp, R.string.pref_key__last_found_location_geohash, ""),
                getString(prefApp, R.string.pref_key__last_found_location_address, "")
        };
    }

    public boolean isAppFirstStart() {
        boolean value = getBool(prefApp, R.string.pref_key__app_first_start, true);
        setBool(prefApp, R.string.pref_key__app_first_start, false);
        return value;
    }

    public boolean isAppCurrentVersionFirstStart() {
        int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
        setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }
}