package org.kn.trackme.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final int UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    public static final int FASTEST_INTERVAL = 2000; /* 2 sec */

    public static void zoomRoute(GoogleMap map, List<LatLng> lstLatLngRoute) {
        if (map == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) {
            return;
        }
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute) {
            boundsBuilder.include(latLngPoint);
        }

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    public static String formatLongTime(long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static String formatDistence(double distance) {
        return String.format("%.2f", distance);
    }

    public static String formatSpeed(double speed) {
        return String.format("%.2f", speed);
    }
}
