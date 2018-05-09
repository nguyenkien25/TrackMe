package org.kn.trackme.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.kn.trackme.BuildConfig;
import org.kn.trackme.R;
import org.kn.trackme.model.TrackMeInfo;
import org.kn.trackme.utils.RealmController;
import org.kn.trackme.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;

public class RecordActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.img_pause)
    ImageButton imgPause;
    @BindView(R.id.img_resume)
    ImageButton imgResume;
    @BindView(R.id.img_stop)
    ImageButton imgStop;

    @BindView(R.id.distance)
    TextView tvDistance;
    @BindView(R.id.speed)
    TextView tvSpeed;
    @BindView(R.id.duration)
    TextView tvDuration;

    @OnClick(R.id.img_pause)
    public void pause() {
        isPause = true;
        imgPause.setVisibility(View.GONE);
        imgResume.setVisibility(View.VISIBLE);
        imgStop.setVisibility(View.VISIBLE);
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
    }

    @OnClick(R.id.img_resume)
    public void resume() {
        isPause = false;
        imgPause.setVisibility(View.VISIBLE);
        imgResume.setVisibility(View.GONE);
        imgStop.setVisibility(View.GONE);
        startTime = System.currentTimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    @OnClick(R.id.img_stop)
    public void stop() {
        //Stop location sharing service to app server.........
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        isAlreadyStartedService = false;
        addTrackMe();
        //Ends................................................
        finish();
    }

    private static final String TAG = RecordActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 5;
    private boolean isAlreadyStartedService = false;
    private boolean isPause = false;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private RealmList<String> locations;
    private Marker markerCurrent;
    private Handler customHandler;
    private long timeInMilliseconds;
    private long timeSwapBuff;
    private long updatedTime;
    private long startTime;
    private double distance;
    private double speed;
    private int numberCheck;
    private double avgSpeed;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        this.realm = RealmController.with(this).getRealm();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayServices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
    }

    /**
     * Check Google Play services
     */
    private void checkGooglePlayServices() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            //Passing null to indicate that it is executing for the first time.
            checkAndPromptInternet(null);
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_google_play_service_available, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check & Prompt Internet connection
     */
    private Boolean checkAndPromptInternet(DialogInterface dialog) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }

        if (dialog != null) {
            dialog.dismiss();
        }
        //Yes there is active internet connection. Next check Location is granted by user or not.
        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startLocationUpdates();
            //startLocationMonitorService();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
        builder.setTitle(R.string.title_alert_no_internet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText, (dialog, which) -> {
            //Block the Application Execution until user grants the permissions
            if (checkAndPromptInternet(dialog)) {
                //Now make sure about location permission.
                if (checkPermissions()) {
                    //Step 2: Start the Location Monitor Service
                    //Everything is there to start the service.
                    startLocationUpdates();
                    // startLocationMonitorService();
                } else if (!checkPermissions()) {
                    requestPermissions();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok, v -> {
                // Request permission
                ActivityCompat.requestPermissions(RecordActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(RecordActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                startLocationUpdates();
                //startLocationMonitorService();

            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings, v -> {
                    // Build intent that displays the App settings screen.
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Stop location sharing service to app server.........
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        isAlreadyStartedService = false;
        //Ends................................................
    }

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        if (!isAlreadyStartedService) {
            distance = 0.0;
            speed = 0.0;
            avgSpeed = 0.0;
            numberCheck = 0;
            locations = new RealmList<>();
            //Start location sharing service to app server.........

            // Create the location request to start receiving updates
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(Utils.UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(Utils.FASTEST_INTERVAL);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (!isPause) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                }
            };
            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            timeInMilliseconds = 0L;
            timeSwapBuff = 0L;
            updatedTime = 0L;
            startTime = System.currentTimeMillis();
            customHandler = new Handler();
            customHandler.postDelayed(updateTimerThread, 0);
            //Ends................................................
        }
    }

    public void onLocationChanged(Location location) {
        numberCheck++;
        // New location has now been determined
        Log.d(TAG, "Updated Location: " + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
        // You can now create a LatLng Object for use with maps
        locations.add(Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!isAlreadyStartedService) {
            mGoogleMap.addMarker(new MarkerOptions().position(latLng));
            markerCurrent = mGoogleMap.addMarker(new MarkerOptions().position(latLng));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
            isAlreadyStartedService = true;
        } else {
            try {
                PolylineOptions polylineOptions = new PolylineOptions();
                String[] locationBegin = locations.get(locations.size() - 2).split(",");
                LatLng lastLatlng = new LatLng(Double.parseDouble(locationBegin[0]), Double.parseDouble(locationBegin[1]));
                polylineOptions.add(lastLatlng);
                polylineOptions.add(latLng);
                polylineOptions.width(5).color(Color.BLUE).geodesic(true);
                mGoogleMap.addPolyline(polylineOptions);
                markerCurrent.setPosition(latLng);

                Location locationA = new Location("point A");
                locationA.setLatitude(lastLatlng.latitude);
                locationA.setLongitude(lastLatlng.longitude);
                Location locationB = new Location("point B");
                locationB.setLatitude(latLng.latitude);
                locationB.setLongitude(latLng.longitude);
                distance += (locationA.distanceTo(locationB) / 1000);
                speed = locationA.distanceTo(locationB) / (Utils.UPDATE_INTERVAL * 1000);
                avgSpeed = ((avgSpeed * (numberCheck - 1)) + speed) / numberCheck;
                Log.d(TAG, "distance " + distance);
                tvDistance.setText(Utils.formatDistence(distance) + " " + getString(R.string.extension_distance));
                tvSpeed.setText(Utils.formatSpeed(speed) + " " + getString(R.string.extension_speed));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = System.currentTimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            tvDuration.setText(Utils.formatLongTime(updatedTime));
            customHandler.postDelayed(this, 0);
        }
    };

    public void addTrackMe() {
        realm.beginTransaction();
        TrackMeInfo trackMeInfo = new TrackMeInfo();
        trackMeInfo.setId(System.currentTimeMillis());
        trackMeInfo.setDistance(distance);
        trackMeInfo.setAvgSpeed(avgSpeed);
        trackMeInfo.setDuration(updatedTime);
        trackMeInfo.setLocation(locations);
        realm.copyToRealm(trackMeInfo);
        realm.commitTransaction();
    }
}
