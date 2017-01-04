package io.github.froodyapp.service;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;
import android.util.Log;

import org.osmdroid.bonuspack.location.GeocoderNominatim;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;

/**
 * Task for reverse geo coding lat/lng
 */
public class EntryReverseGeocoder extends Thread {
    //########################
    //## Statics
    //########################
    private static final String USER_AGENT__NOMINATIM = "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
    private static final ConcurrentHashMap<String, String> alreadyDecodedAddresses = new ConcurrentHashMap<>();

    //########################
    //## Member
    //########################
    private final Context context;
    private final GeocoderNominatim geocoder;
    private final FroodyEntryPlus entry;

    //########################
    //## Methods
    //########################
    public EntryReverseGeocoder(Context context, FroodyEntryPlus entry) {
        this.context = context != null ? context.getApplicationContext() : null;
        this.entry = entry;
        geocoder = new GeocoderNominatim(Locale.getDefault(), USER_AGENT__NOMINATIM);
    }

    @Override
    public void run() {
        if (context == null || entry == null || entry.getLatitude() == null || entry.getLongitude() == null) {
            return;
        }

        // Decode only if not already decoded once
        String geohash = entry.getGeohash();
        String address = alreadyDecodedAddresses.get(geohash);
        if (address == null) {
            address = resolveGeoAddress(entry);
            alreadyDecodedAddresses.put(geohash, address);
        }

        // Set the geocoded address
        entry.setAddress(address);

        AppCast.FROODY_ENTRY_GEOCODED.send(context, entry);
        AppSettings appSettings = new AppSettings(context);
        appSettings.setLastFoundLocation(geohash, address);
    }

    /**
     * Geo-Addresse aufloesen
     *
     * @param entry Eintrag
     * @return String mit addresse, oder leerer string bei fehler
     */
    private String resolveGeoAddress(FroodyEntryPlus entry) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(entry.getLatitude(), entry.getLongitude(), 3);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error: Cannot reverse geocode " + entry.getEntryId());
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            StringBuffer sb = new StringBuffer();
            extractFromAddress(address, sb);
            return sb.toString().trim();
        }
        return "";
    }

    /**
     * Extract the address (small)
     *
     * @param address address object
     * @param sb      stringbuffer
     */
    public void extractFromAddress(Address address, StringBuffer sb) {
        //sb.append(trim(address.getCountryName()));    // Country - Austria
        //sb.append(trim(address.getCountryCode()));    // Country short - AT
        //sb.append(trim(address.getFeatureName()));    // StreetNumber - 23
        sb.append(trim(address.getThoroughfare()));     // StreetName
        sb.append(", ");
        sb.append(trim(address.getPostalCode()));       // ZipCode   4323
        sb.append(" ");
        sb.append(trim(address.getLocality()));         // Village  Hagenberg
    }

    /**
     * Trim string
     *
     * @param str Whitespaced string
     * @return Non-whitespaced string
     */
    private String trim(String str) {
        return TextUtils.isEmpty(str) ? "" : str.trim();
    }
}