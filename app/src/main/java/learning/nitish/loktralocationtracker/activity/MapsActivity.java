package learning.nitish.loktralocationtracker.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import learning.nitish.loktralocationtracker.R;
import learning.nitish.loktralocationtracker.helper.MapHelperClass;
import learning.nitish.loktralocationtracker.helper.PermissionHelperClass;
import learning.nitish.loktralocationtracker.model.LocationObject;
import learning.nitish.loktralocationtracker.service.TrackingService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks {


    private final String[] requiredPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_WRITE_STORAGE = 112;

    private GoogleMap mMap;
    LocationManager locationManager;
    public static List<LocationObject> startToPresentLocations = new ArrayList<>();

    @BindView(R.id.start_tracking)
    Button button;
    @BindView(R.id.cardViewTime)
    CardView cardView;
    @BindView(R.id.txtTime)
    TextView txtTotalTime;

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private double latitudeValue = 0.0;
    private double longitudeValue = 0.0;
    private TrackingBroadCastReceiver routeReceiver;

    private boolean isServiceRunning = false;


    private MapHelperClass mapHelperClass;
    private PermissionHelperClass permissionHelperClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        startToPresentLocations.clear();
        initiateGoogleApiClient();


        mapHelperClass = new MapHelperClass();
        permissionHelperClass = new PermissionHelperClass();
        routeReceiver = new TrackingBroadCastReceiver();
        mLocationRequest = mapHelperClass.createLocationRequest();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    void initiateGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    private void checkGpsStatus() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            permissionHelperClass.showEnableGpsAlert(MapsActivity.this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkGpsStatus();
        checkpermission();
        if (routeReceiver == null) {
            routeReceiver = new TrackingBroadCastReceiver();
        }
        IntentFilter filter = new IntentFilter(TrackingService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(routeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(routeReceiver);
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onLocationChanged(Location location) {


        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(mapHelperClass.getMarkerInfo(MapsActivity.this, latLng)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));


    }


    private class TrackingBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String local = intent.getExtras().getString("RESULT_CODE");
            assert local != null;
            if (local.equals("LOCAL")) {
                if (startToPresentLocations.size() > 0) {
                    //prepare map drawing.
                    List<LatLng> locationPoints = mapHelperClass.getPoints(startToPresentLocations);
                    mapHelperClass.refreshMap(mMap);
                    mapHelperClass.markStartingLocationOnMap(getApplicationContext(), mMap, locationPoints.get(0));
                    mapHelperClass.drawRouteOnMap(mMap, locationPoints);
                    mapHelperClass.markEndingLocationOnMap(getApplicationContext(), mMap, locationPoints.get(locationPoints.size() - 1));
                }
            }
        }
    }


    private void checkpermission() {

        if (getPermissions()) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                makeRequest();
            } else {
                makeRequest();
            }
        } else {
            mGoogleApiClient.connect();
        }

    }


    private boolean getPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return true;
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                requiredPermissions,
                REQUEST_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            if (mLastLocation != null) {
                                latitudeValue = mLastLocation.getLatitude();
                                longitudeValue = mLastLocation.getLongitude();
                                mapHelperClass.refreshMap(mMap);
                                mapHelperClass.markStartingLocationOnMap(getApplicationContext(), mMap, new LatLng(latitudeValue, longitudeValue));
                                LocationObject locationObject = new LocationObject(System.currentTimeMillis(), latitudeValue, longitudeValue);
                                startToPresentLocations.add(locationObject);
                                mapHelperClass.startPolyline(mMap, new LatLng(latitudeValue, longitudeValue));
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }

            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public void statService() {

        Intent intent = new Intent(MapsActivity.this, TrackingService.class);
        startService(intent);

    }


    public void stopService() {

        Intent intent = new Intent(MapsActivity.this, TrackingService.class);
        stopService(intent);
    }


    @OnClick(R.id.start_tracking)
    public void startTracking() {

        if (!isServiceRunning) {
            cardView.setVisibility(View.GONE);
            statService();
            mapHelperClass.refreshMap(mMap);
            LocationObject firstObejct = startToPresentLocations.get(0);
            startToPresentLocations.clear();
            startToPresentLocations.add(firstObejct);
            mGoogleApiClient.connect();
            isServiceRunning = true;

            button.setText(getString(R.string.stop_shifting));
        } else {
            stopService();
            isServiceRunning = false;
            button.setText(getString(R.string.start_shifting));
            calculateTime();
        }

    }

    private void calculateTime() {

        long startTime = startToPresentLocations.get(0).getTime();
        long stopTime = startToPresentLocations.get(startToPresentLocations.size() - 1).getTime();

        long totalTime = stopTime - startTime;

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(totalTime);
        Date startdate = new Date(startTime);
        Date enddate = new Date(stopTime);
        long diffMs = enddate.getTime() - startdate.getTime();
        long diffSec = diffMs / 1000;

        long diffmin = diffSec / 60;
        long hrs = diffmin / 60;
        long min = diffmin % 60;
        long sec = diffSec % 60;
        System.out.println(sdf.format(resultdate));
        cardView.setVisibility(View.VISIBLE);
        txtTotalTime.setText(hrs + "h " + min + "m " + sec + "s");


    }
}
