package io.github.froodyapp.util;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.InetSocketAddress;
import java.net.Proxy;

import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.api.invoker.ApiClient;
import io.github.froodyapp.api.invoker.Configuration;

public class Helpers extends io.github.gsantner.opoc.util.Helpers {
    protected Helpers(Context context) {
        super(context);
    }


    public static Helpers get() {
        return new Helpers(App.get());
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
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(appSettings.getNetworkHttpProxyHost(), appSettings.getNetworkHttpProxyPort()));
            apiClient.getHttpClient().setProxy(proxy);
        }
    }
}
