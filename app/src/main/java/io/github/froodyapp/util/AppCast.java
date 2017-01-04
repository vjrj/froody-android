package io.github.froodyapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.api.model_.FroodyUser;
import io.github.froodyapp.location.LocationTool;
import io.github.froodyapp.model.FroodyEntryPlus;

public class AppCast {
    //########################
    //## Send broadcast
    //########################
    private static void sendBroadcast(Context c, Intent i) {
        if (c != null) {
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        }
    }

    //########################
    //## Data retrieve helper
    //########################
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    public static ArrayList<FroodyEntryPlus> getFroodyEntryListFromIntent(Intent intent, String extra) {
        return ((ArrayList<FroodyEntryPlus>) (intent.getSerializableExtra(extra)));
    }

    public static FroodyEntryPlus getFroodyEntryFromIntent(Intent intent) {
        return (FroodyEntryPlus) (intent.getSerializableExtra(FroodyEntryInfoExtra.EXTRA_FROODY_ENTRY));
    }

    //########################
    //## Filter
    //########################
    public static IntentFilter getLocalBroadcastFilter() {
        IntentFilter intentFilter = new IntentFilter();
        String[] BROADCAST_ACTIONS = {
                FROODY_ENTRY_DETAILS_LOADED.ACTION,
                FROODY_ENTRY_TAPPED.ACTION,
                FROODY_ENTRIES_LOADED.ACTION,
                FROODY_ENTRIES_TAPPED.ACTION,
                FROODY_USER_REGISTERED.ACTION,
                FROODY_ENTRY_GEOCODED.ACTION,
                FROODY_ENTRY_DELETED.ACTION,
                MAP_POSITION_CHANGED.ACTION,
                LOCATION_FOUND.ACTION
        };
        for (String action : BROADCAST_ACTIONS) {
            intentFilter.addAction(action);
        }
        return intentFilter;
    }

    //########################
    //## Basics
    //########################
    private static class PositionInfoExtras {
        public static final String EXTRA_LATITUDE = "EXTRA_LATITUDE";
        public static final String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";
        public static final String EXTRA_ZOOM = "EXTRA_ZOOM";

        static void putPositionExtras(Intent i, double lat, double lng, int zoom) {
            i.putExtra(EXTRA_LATITUDE, lat);
            i.putExtra(EXTRA_LONGITUDE, lng);
            i.putExtra(EXTRA_ZOOM, zoom);
        }
    }

    private static class FroodyEntryInfoExtra {
        public static final String EXTRA_FROODY_ENTRY = "EXTRA_FROODY_ENTRY";

        public static void putFroodyEntryExtra(Intent i, FroodyEntry entry) {
            i.putExtra(EXTRA_FROODY_ENTRY, entry);
        }

        public static void sendEntry(Context c, FroodyEntryPlus entry, String ACTION) {
            Intent i = new Intent(ACTION);
            putFroodyEntryExtra(i, entry);
            sendBroadcast(c, i);
        }
    }

    //########################
    //## Actions
    //########################
    public static class MAP_POSITION_CHANGED extends PositionInfoExtras {
        public static final String ACTION = "MAP_POSITION_CHANGED";

        public static void send(Context c, double lat, double lng, int zoom) {
            Intent i = new Intent(ACTION);
            putPositionExtras(i, lat, lng, zoom);
            sendBroadcast(c, i);
        }
    }

    public static class LOCATION_FOUND {
        public static final String ACTION = "LOCATION_FOUND";
        public static final String EXTRA_LOCATION_TOOL_RESPONSE = "EXTRA_LOCATION_TOOL_RESPONSE";

        public static void send(Context c, LocationTool.LocationToolResponse response) {
            Intent i = new Intent(ACTION);
            i.putExtra(EXTRA_LOCATION_TOOL_RESPONSE, response);
            sendBroadcast(c, i);
        }

        public static LocationTool.LocationToolResponse getResponseFromIntent(Intent i) {
            Serializable s = i.getSerializableExtra(
                    AppCast.LOCATION_FOUND.EXTRA_LOCATION_TOOL_RESPONSE);
            return ((LocationTool.LocationToolResponse) s);
        }
    }


    public static class FROODY_ENTRY_TAPPED extends FroodyEntryInfoExtra {
        public static final String ACTION = "FROODY_ENTRY_TAPPED";

        public static void send(Context c, FroodyEntryPlus entry) {
            sendEntry(c, entry, ACTION);
        }
    }

    public static class FROODY_ENTRIES_LOADED {
        public static final String ACTION = "FROODY_ENTRIES_LOADED";
        public static final String EXTRA_FROODY_ENTRIES = "EXTRA_FROODY_ENTRIES";

        public static void send(Context c, List<FroodyEntryPlus> entries) {
            Intent i = new Intent(ACTION);
            i.putExtra(EXTRA_FROODY_ENTRIES, new ArrayList<>(entries));
            sendBroadcast(c, i);
        }
    }

    public static class FROODY_ENTRIES_TAPPED {
        public static final String ACTION = "FROODY_ENTRIES_TAPPED";
        public static final String EXTRA_FROODY_ENTRIES = "EXTRA_FROODY_ENTRIES";

        public static void send(Context c, List<FroodyEntry> entries) {
            Intent i = new Intent(ACTION);
            i.putExtra(EXTRA_FROODY_ENTRIES, new ArrayList<>(entries));
            sendBroadcast(c, i);
        }
    }

    public static class FROODY_USER_REGISTERED {
        public static final String ACTION = "FROODY_USER_REGISTERED";
        public static final String EXTRA_FROODY_USER = "EXTRA_FROODY_USER";

        public static void send(Context c, FroodyUser user) {
            Intent i = new Intent(ACTION);
            i.putExtra(EXTRA_FROODY_USER, user);
            sendBroadcast(c, i);
        }
    }

    public static class FROODY_ENTRY_GEOCODED extends FroodyEntryInfoExtra {
        public static final String ACTION = "FROODY_ENTRY_GEOCODED";

        public static void send(Context c, FroodyEntryPlus entry) {
            sendEntry(c, entry, ACTION);
        }
    }

    public static class FROODY_ENTRY_DELETED extends FroodyEntryInfoExtra {
        public static final String ACTION = "FROODY_ENTRY_DELETED";
        public static final String EXTRA_WAS_DELETED = "EXTRA_WAS_DELETED";

        public static void send(Context c, FroodyEntryPlus entry, boolean wasDeleted) {
            Intent i = new Intent(ACTION);
            putFroodyEntryExtra(i, entry);
            i.putExtra(EXTRA_WAS_DELETED, wasDeleted);
            sendBroadcast(c, i);
        }
    }

    public static class FROODY_ENTRY_DETAILS_LOADED extends FroodyEntryInfoExtra {
        public static final String ACTION = "FROODY_ENTRY_DETAILS_LOADED";

        public static void send(Context c, FroodyEntryPlus entry) {
            sendEntry(c, entry, ACTION);
        }
    }


}
