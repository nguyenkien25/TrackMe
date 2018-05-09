package org.kn.trackme.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.MapView;

import org.kn.trackme.R;
import org.kn.trackme.adapter.HistoryAdapter;
import org.kn.trackme.utils.RealmController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public abstract class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.card_list)
    RecyclerView mRecyclerView;

    public Realm realm;

    protected HistoryAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        this.realm = RealmController.with(this).getRealm();

        // Determine the number of columns to display, based on screen width.
        int rows = getResources().getInteger(R.integer.map_grid_cols);
        GridLayoutManager layoutManager = new GridLayoutManager(this, rows, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        // Delay attaching Adapter to RecyclerView until we can ensure that we have correct
        // Google Play service version (in onResume).
    }

    protected abstract HistoryAdapter createMapListAdapter();

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onLowMemory();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mListAdapter = createMapListAdapter();
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            mRecyclerView.setAdapter(mListAdapter);
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1).show();
        }

        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onResume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onDestroy();
            }
        }

        super.onDestroy();
    }

    /**
     * Show a full mapView when a mapView card is selected. This method is attached to each CardView
     * displayed within this activity's RecyclerView.
     *
     * @param view The view (CardView) that was clicked.
     */
    @OnClick(R.id.record)
    public abstract void showNewRecord(View view);
}
