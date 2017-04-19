package io.github.froodyapp.location;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;

import io.github.froodyapp.activity.MapOSMFragment;
import io.github.froodyapp.util.AppCast;

/**
 * Notifier, which sends out the current visible location on the osmdroid map
 * Reduces amount of location changed events
 */
public class MapListenerNotifier extends Thread {
    //########################
    //## Static
    //########################
    private final static long HAPPENING_INTERVAL = 1000;
    private final static long HAPPENING_INTERVAL_WORLD = 5000;
    private static long LAST_HAPPENING_TIME = 0;

    //########################
    //## Member
    //########################
    private final MapView map;

    //########################
    //## Methods
    //########################
    public MapListenerNotifier(MapView map) {
        this.map = map;
    }

    private synchronized static void setLastHappeningTime(long v) {
        if (v > LAST_HAPPENING_TIME) {
            LAST_HAPPENING_TIME = v;
        }
    }

    @Override
    public void run() {
        try {
            sleep(HAPPENING_INTERVAL);
        } catch (InterruptedException ignored) {
        }
        long now = System.currentTimeMillis();
        if ((now - LAST_HAPPENING_TIME) >= HAPPENING_INTERVAL && map != null && map.getZoomLevel() > MapOSMFragment.ZOOMLEVEL_REQUEST_TRESHOLD_TO_4) {
            setLastHappeningTime(now);
            IGeoPoint center = map.getMapCenter();
            AppCast.MAP_POSITION_CHANGED.send(map.getContext(), center.getLatitude(), center.getLongitude(), map.getZoomLevel());
        }

        if ((now - LAST_HAPPENING_TIME) >= HAPPENING_INTERVAL_WORLD && map != null && map.getZoomLevel() <= MapOSMFragment.ZOOMLEVEL_REQUEST_TRESHOLD_TO_4) {
            setLastHappeningTime(now);
            IGeoPoint center = map.getMapCenter();
            AppCast.MAP_POSITION_CHANGED.send(map.getContext(), center.getLatitude(), center.getLongitude(), map.getZoomLevel());
        }
    }
}
