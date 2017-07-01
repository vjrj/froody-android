package io.github.froodyapp.util;

import android.content.Context;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;

import io.github.froodyapp.App;
import io.github.froodyapp.BuildConfig;
import io.github.froodyapp.R;
import io.github.froodyapp.activity.MapOSMFragment;
import io.github.froodyapp.api.model_.FroodyUser;
import io.github.gsantner.opoc.util.AppSettingsBase;

/**
 * Settings Wrapper for the app
 */
public class AppSettings extends AppSettingsBase {
    //#####################
    //## Methods
    //#####################
    private AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    //###########################################
    //## Settings options
    //############################################
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

    public boolean isDevDebugModeEnabled() {
        return getBool(prefApp, R.string.pref_key__dev_mode_debugging, false);
    }

    public void setDevDebugModeEnabled(boolean value) {
        setBool(prefApp, R.string.pref_key__dev_mode_debugging, value);
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

    public void setLastSelectedEntryTypes(ArrayList<Integer> values) {
        setIntList(prefApp, R.string.pref_key__entry__last_selected_entry_types, values);
    }

    public ArrayList<Integer> getLastSelectedEntryTypes() {
        return getIntList(prefApp, R.string.pref_key__entry__last_selected_entry_types);
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
        return getBool(prefApp, R.string.pref_key__app_first_start, true);
    }

    public void setAppFirstStart(boolean value) {
        setBool(prefApp, R.string.pref_key__app_first_start, value);
    }

    public boolean isAppFirstStartCurrentVersion() {
        int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
        setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }

    public void setEntryTypeSelectionDialogTabLastUsed(int value) {
        setInt(prefApp, R.string.pref_key__entry_type_selection_dialog__tab_last_used, value);
    }

    public int getEntryTypeSelectionDialogTabLastUsed() {
        return getInt(prefApp, R.string.pref_key__entry_type_selection_dialog__tab_last_used, 0);
    }

    public boolean isNetworkHttpProxyEnabled() {
        return getBool(R.string.pref_key__network__http_proxy_enabled, false);
    }

    public String getNetworkHttpProxyHost() {
        return getString(R.string.pref_key__network__http_proxy_host, "127.0.0.1");
    }

    public int getNetworkHttpProxyPort() {
        return getIntOfStringPref(R.string.pref_key__network__http_proxy_port, 8118);
    }

    public Proxy getNetworkHttpProxy() {
        return new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(getNetworkHttpProxyHost(), getNetworkHttpProxyPort()));
    }
}