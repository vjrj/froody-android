package io.github.froodyapp.service;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;

import org.osmdroid.bonuspack.location.GeocoderNominatim;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import io.github.froodyapp.App;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;

/**
 * Reverse geo coder via osmdroid API
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
        AppSettings appSettings = AppSettings.get();
        appSettings.setLastFoundLocation(geohash, address);
    }

    /**
     * Resolve address of one entry
     *
     * @param entry Entry
     * @return String with address, or empty string in case of error
     */
    private String resolveGeoAddress(FroodyEntryPlus entry) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(entry.getLatitude(), entry.getLongitude(), 3);
        } catch (IOException e) {
            App.log(getClass(), "Error: Cannot reverse geocode " + entry.getEntryId());
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            return extractAddressDetails(address);
        }
        return "";
    }

    // Extract the address details from address object of osmdroid api
    private String extractAddressDetails(Address address) {
        StringBuffer sb = new StringBuffer();
        //sb.append(trim(address.getCountryName()));    // Country - Austria
        //sb.append(trim(address.getCountryCode()));    // Country short - AT
        //sb.append(trim(address.getFeatureName()));    // StreetNumber - 23
        sb.append(trim(address.getThoroughfare()));     // StreetName - Softwarepark
        sb.append(", ");
        sb.append(trim(address.getPostalCode()));       // ZipCode   4323
        sb.append(" ");
        sb.append(trim(address.getLocality()));         // Village  Hagenberg
        return sb.toString().trim();
    }

    // Trim string if possible
    private String trim(String str) {
        return TextUtils.isEmpty(str) ? "" : str.trim();
    }
}