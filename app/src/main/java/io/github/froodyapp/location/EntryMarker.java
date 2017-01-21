package io.github.froodyapp.location;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.FroodyEntryFormatter;

/**
 * Marker container for osmdroid MapView
 */
public class EntryMarker extends Marker implements Marker.OnMarkerClickListener {
    //########################
    //## Static
    //########################
    private static final int MARKER_SIZE_IN_DP = 40;

    public static EntryMarker from(MapView mapView, FroodyEntryPlus froodyEntry) {
        return new EntryMarker(mapView, froodyEntry);
    }

    //########################
    //## Members
    //########################
    private OnMarkerClickListener anotherMarkerClickListener = null;
    private final FroodyEntryPlus froodyEntry;

    //########################
    //## Methods
    //########################
    public EntryMarker(MapView mapView, FroodyEntryPlus froodyEntry) {
        super(mapView);
        this.froodyEntry = froodyEntry;
        super.setOnMarkerClickListener(this);

        Context context = mapView.getContext();
        if (context != null) {
            FroodyEntryFormatter froodyEntryFormatter = new FroodyEntryFormatter(context, froodyEntry);
            setTitle(froodyEntryFormatter.getEntryTypeName());
            setIcon(froodyEntryFormatter.getEntryTypeImage());
        }
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        setPosition(new GeoPoint(froodyEntry.getLatitude(), froodyEntry.getLongitude()));
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        AppCast.FROODY_ENTRY_TAPPED.send(mapView.getContext(), froodyEntry);
        return anotherMarkerClickListener == null || anotherMarkerClickListener.onMarkerClick(marker, mapView);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof EntryMarker) {
            EntryMarker other = ((EntryMarker) obj);
            return froodyEntry.getEntryId().equals(other.getFroodyEntry().getEntryId());
        }
        return super.equals(obj);
    }

    @Override
    @SuppressWarnings("SuspiciousNameCombination")
    // Override default osmdroid drawing behaviour to scale by dp units
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow || mIcon == null) {
            return;
        }

        // Scale by DP
        int height = (int) (mapView.getContext().getResources().getDisplayMetrics().scaledDensity * MARKER_SIZE_IN_DP);
        int width = height;
        if (mIcon.getIntrinsicWidth() > mIcon.getIntrinsicHeight()) {
            height = width * mIcon.getIntrinsicHeight() / mIcon.getIntrinsicWidth();
        } else {
            width = height * mIcon.getIntrinsicWidth() / mIcon.getIntrinsicHeight();
        }

        // Draw like in original method
        final Projection pj = mapView.getProjection();
        pj.toPixels(mPosition, mPositionPixels);
        Rect rect = new Rect(0, 0, width, height);
        rect.offset(-(int) (mAnchorU * width), -(int) (mAnchorV * height));
        mIcon.setBounds(rect);
        mIcon.setAlpha((int) (mAlpha * 255));

        float rotationOnScreen = (mFlat ? -mBearing : mapView.getMapOrientation() - mBearing);
        drawAt(canvas, mIcon, mPositionPixels.x, mPositionPixels.y, false, rotationOnScreen);
    }


    //########################
    //## Getter & Setter
    //########################
    public FroodyEntry getFroodyEntry() {
        return froodyEntry;
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        anotherMarkerClickListener = listener;
    }
}
