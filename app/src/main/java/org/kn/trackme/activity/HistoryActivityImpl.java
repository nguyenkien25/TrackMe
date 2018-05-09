package org.kn.trackme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.kn.trackme.adapter.HistoryAdapter;
import org.kn.trackme.model.TrackMeInfo;
import org.kn.trackme.utils.RealmController;

import java.util.ArrayList;

public class HistoryActivityImpl extends HistoryActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected HistoryAdapter createMapListAdapter() {
        HistoryAdapter adapter = new HistoryAdapter();
        ArrayList<TrackMeInfo> list = new ArrayList<>();
        list.addAll(RealmController.getInstance().getTrackMes());
        adapter.setTrackMes(list);
        adapter.setContext(this);

        return adapter;
    }

    @Override
    public void showNewRecord(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }

}
