package io.github.froodyapp.location;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.util.AppCast;

public class RadiusMarkerClusterWithClusterClick extends RadiusMarkerClusterer {
    //########################
    //## Methods
    //########################
    public RadiusMarkerClusterWithClusterClick(Context c) {
        super(c);
        mMaxClusteringZoomLevel = 21;
    }

    @Override
    public Marker buildClusterMarker(final StaticCluster cluster, MapView mapView) {
        Marker pin = new Marker(mapView);
        pin.setPosition(cluster.getPosition());
        pin.setInfoWindow(null);
        pin.setAnchor(mAnchorU, mAnchorV);
        pin.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                clusterWasClicked(cluster, mapView);
                return true;
            }
        });

        Bitmap bitmap = Bitmap.createBitmap(mClusterIcon.getWidth(), mClusterIcon.getHeight(), mClusterIcon.getConfig());
        Canvas bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawBitmap(mClusterIcon, 0, 0, null);
        String text = Integer.toString(cluster.getSize());
        int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
        bitmapCanvas.drawText(text, mTextAnchorU * bitmap.getWidth(),
                mTextAnchorV * bitmap.getHeight() - textHeight / 2, mTextPaint);
        pin.setIcon(new BitmapDrawable(mapView.getContext().getResources(), bitmap));

        return pin;
    }

    private void clusterWasClicked(StaticCluster cluster, MapView map) {
        ArrayList<FroodyEntry> entries = new ArrayList<>();
        for (int i = 0; i < cluster.getSize(); i++) {
            EntryMarker entryMarker = (EntryMarker) cluster.getItem(i);
            entries.add(entryMarker.getFroodyEntry());
        }

        // Send a broadcast
        AppCast.FROODY_ENTRIES_TAPPED.send(map.getContext(), entries);
    }
}
