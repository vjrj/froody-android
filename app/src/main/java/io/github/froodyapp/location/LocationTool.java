package io.github.froodyapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import io.github.froodyapp.util.AppCast;

/**
 * Toolset for locating android device
 *
 * @link http://stackoverflow.com/a/3145655
 */
public class LocationTool {
    //########################
    //## Static
    //########################
    public static final int REQUEST_LOCATION_PERM = 42;
    private static final boolean DEBUG_FAKE_LOCATION_ENABLED = false; // PRs with true will get ignored
    private static final double[] DEBUG_FAKE_LOCATION = {48.378765, 14.512982};
    private static final int WAITING_TIME_LOCATION_MS = 15000;

    public static class LocationToolResponse implements Serializable {
        public String requestedBy = "";
        public String provider = "";
        public double lat;
        public double lng;
    }

    //########################
    //## Members
    //########################
    private Timer lastLocationTimer;
    private LocationManager locationManager;
    private boolean isEnabledGPS = false;
    private boolean isEneabledNET = false;
    private Context context;
    private String requestedBy = "";
    private final boolean settingAllowGpsListening;
    private final boolean settingAllowNetListening;

    //########################
    //## Methods
    //########################

    public LocationTool() {
        this(true, true);
    }

    public LocationTool(boolean settingAllowGpsListening, boolean settingAllowNetListening) {
        this.settingAllowNetListening = settingAllowNetListening;
        this.settingAllowGpsListening = settingAllowGpsListening;
    }

    //########################
    //## Listener
    //########################
    /**
     * Network location listener
     */
    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            //lastLocationTimer.cancel();
            postLocation(location);
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    /**
     * Gps location listener
     */
    private final LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            lastLocationTimer.cancel();
            postLocation(location);
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    locationManager.removeUpdates(this);
                    locationManager.removeUpdates(locationListenerNetwork);
                } catch (SecurityException ignored) {
                    // Already handled outside
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    //########################
    //## More methods
    //########################

    /**
     * Ask for location permission (if >= 6.0)
     *
     * @param activity Activity to show request on
     * @return
     */
    public boolean askForLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERM);
            return false;
        }
        return true;
    }

    /**
     * Provide a fake location to listeners
     */
    private void provideFakeLocation() {
        // ONLY enabled if Debug mode
        if (DEBUG_FAKE_LOCATION_ENABLED) {
            Location lo = new Location("fake");
            lo.setLatitude(DEBUG_FAKE_LOCATION[0]);
            lo.setLongitude(DEBUG_FAKE_LOCATION[1]);
            lo.setTime(System.currentTimeMillis());
            postLocation(lo);
        }
    }

    /**
     * Request the location, will run async and return data
     * dispatched to UI-Thread via LocationFoundListener
     */
    public boolean requestLocation(Context context, String requestedBy) {
        this.context = context.getApplicationContext();
        this.requestedBy = requestedBy;

        if (DEBUG_FAKE_LOCATION_ENABLED) {
            provideFakeLocation();
            return true;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        //exceptions will be thrown if provider is not permitted.
        try {
            isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignore) {
        }
        try {
            isEneabledNET = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignore) {
        }
        //don't start listeners if no provider is enabled
        if (!isEnabledGPS && !isEneabledNET) {
            return false;
        }
        if (isEnabledGPS && settingAllowNetListening && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if (isEneabledNET && settingAllowGpsListening && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        lastLocationTimer = new Timer();
        lastLocationTimer.schedule(new GetLastLocation(), WAITING_TIME_LOCATION_MS);
        return isEnabledGPS || isEneabledNET;
    }


    /**
     * Disable the location tool
     * Call in onPause or if no data are wanted anymore
     */
    public void disableLocationTool() {
        if (lastLocationTimer != null)
            lastLocationTimer.cancel();
        if (locationManager != null && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);
        }
    }

    /**
     * Post location to listener, dispatch to UI-Thread
     *
     * @param location the location
     */
    public void postLocation(final Location location) {
        if (location != null) {
            LocationToolResponse response = new LocationToolResponse();
            response.lat = location.getLatitude();
            response.lng = location.getLongitude();
            response.provider = location.getProvider();
            response.requestedBy = requestedBy;
            AppCast.LOCATION_FOUND.send(context, response);
        } else {
            AppCast.NO_FOUND_LOCATION.send(context);
        }
    }

    /**
     * Task for reporting the Last Known Location
     */
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try {
                locationManager.removeUpdates(locationListenerGps);
                locationManager.removeUpdates(locationListenerNetwork);
            } catch (SecurityException ignored) {
                // Already handled in UI
            }

            Location net_loc = null, gps_loc = null;
            if (isEnabledGPS && settingAllowNetListening && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (isEneabledNET && settingAllowGpsListening)
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime())
                    postLocation(gps_loc);
                else
                    postLocation(net_loc);
                return;
            }
            if (gps_loc != null) {
                postLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                postLocation(net_loc);
                return;
            }
            postLocation(null);
        }
    }
}

