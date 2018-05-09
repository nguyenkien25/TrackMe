package org.kn.trackme.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.kn.trackme.R;
import org.kn.trackme.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    @BindView(R.id.distance)
    TextView tvDistance;
    @BindView(R.id.avg_speed)
    TextView tvAvgSpeed;
    @BindView(R.id.duration)
    TextView tvDuration;
    @BindView(R.id.map)
    MapView mapView;

    protected GoogleMap mGoogleMap;
    protected List<String> listRouteMaps;
    private double distance;
    private Context mContext;

    public HistoryViewHolder(Context context, View view) {
        super(view);
        ButterKnife.bind(this, view);

        mContext = context;

        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    public void setRouteMaps(List<String> listRouteMaps) {
        this.listRouteMaps = listRouteMaps;

        // If the map is ready, update its content.
        if (mGoogleMap != null) {
            updateMapContents();
        }
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        MapsInitializer.initialize(mContext);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // If we have map data, update the map content.
        if (listRouteMaps.size() > 0) {
            updateMapContents();
        }
    }

    protected void updateMapContents() {
        // Since the mapView is re-used, need to remove pre-existing mapView features.
        mGoogleMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        List<LatLng> lstLatLngRoute = new ArrayList<>();

        for (int i = 0; i < listRouteMaps.size(); i++) {
            String[] location = listRouteMaps.get(i).split(",");
            try {
                LatLng latLng = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
                polylineOptions.add(latLng);
                lstLatLngRoute.add(latLng);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        polylineOptions.width(5).color(Color.BLUE).geodesic(true);
        mGoogleMap.addPolyline(polylineOptions);
        if (distance < 3) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lstLatLngRoute.get(lstLatLngRoute.size() - 1), 17f));
        } else {
            Utils.zoomRoute(mGoogleMap, lstLatLngRoute);
        }
        String[] locationBegin = listRouteMaps.get(0).split(",");
        String[] locationEnd = listRouteMaps.get(listRouteMaps.size() - 1).split(",");
        mGoogleMap.addMarker(new MarkerOptions()
                .position((new LatLng(Double.parseDouble(locationBegin[0]), Double.parseDouble(locationBegin[1]))))
        );
        mGoogleMap.addMarker(new MarkerOptions()
                .position((new LatLng(Double.parseDouble(locationEnd[0]), Double.parseDouble(locationEnd[1]))))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location))
        );
    }
}
