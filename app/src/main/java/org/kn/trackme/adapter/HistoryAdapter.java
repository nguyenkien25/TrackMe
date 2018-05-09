package org.kn.trackme.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;

import org.kn.trackme.R;
import org.kn.trackme.model.TrackMeInfo;
import org.kn.trackme.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
    protected HashSet<MapView> mMapViews = new HashSet<>();
    protected ArrayList<TrackMeInfo> trackMeInfos;
    private Context context;

    public void setTrackMes(ArrayList<TrackMeInfo> trackMeInfos) {
        this.trackMeInfos = trackMeInfos;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_list_item, viewGroup, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(viewGroup.getContext(), view);

        mMapViews.add(viewHolder.mapView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder viewHolder, int position) {
        TrackMeInfo trackMeInfo = trackMeInfos.get(position);

        viewHolder.itemView.setTag(trackMeInfo);

        viewHolder.tvDistance.setText(Utils.formatDistence(trackMeInfo.getDistance()) + " " + context.getString(R.string.extension_distance));
        viewHolder.tvAvgSpeed.setText(Utils.formatSpeed(trackMeInfo.getAvgSpeed()) + " " + context.getString(R.string.extension_avg_speed));
        viewHolder.tvDuration.setText(Utils.formatLongTime(trackMeInfo.getDuration()));

        viewHolder.setRouteMaps(trackMeInfo.getLocation());
        viewHolder.setDistance(trackMeInfo.getDistance());
    }

    @Override
    public int getItemCount() {
        return trackMeInfos == null ? 0 : trackMeInfos.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }
}
