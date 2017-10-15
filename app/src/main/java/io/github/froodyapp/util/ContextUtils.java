package io.github.froodyapp.util;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.api.invoker.ApiClient;
import io.github.froodyapp.api.invoker.Configuration;

@SuppressWarnings("WeakerAccess")
public class ContextUtils extends net.gsantner.opoc.util.ContextUtils {
    protected ContextUtils(Context context) {
        super(context);
    }


    public static ContextUtils get() {
        return new ContextUtils(App.get());
    }


    public static DateTime getNow() {
        return DateTime.now(DateTimeZone.UTC);
    }

    public static String latLngToGeohash(double lat, double lng, int precision) {
        return GeoHash.withCharacterPrecision(lat, lng, precision).toBase32();
    }

    public static Double[] geohashToLatLng(String geohash) {
        GeoHash gh = GeoHash.fromGeohashString(geohash);
        if (gh != null && gh.getPoint() != null) {
            return new Double[]{
                    gh.getPoint().getLatitude(),
                    gh.getPoint().getLongitude()
            };
        }
        return null;
    }

    public void setupFroodyApi() {
        AppSettings appSettings = AppSettings.get();
        String server = appSettings.getFroodyServer();
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath(server);

        if (appSettings.isNetworkHttpProxyEnabled()) {
            apiClient.getHttpClient().setProxy(appSettings.getNetworkHttpProxy());
        }
    }
}
