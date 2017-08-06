package learning.nitish.loktralocationtracker.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
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

import learning.nitish.loktralocationtracker.activity.MapsActivity;
import learning.nitish.loktralocationtracker.helper.MapHelperClass;
import learning.nitish.loktralocationtracker.helper.SharedPrefHelperClass;
import learning.nitish.loktralocationtracker.model.LocationObject;

/**
 * Created by Nitish Singh Rathore on 6/8/17.
 */

public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = TrackingService.class.getSimpleName();
    public static final String ACTION = "learning.nitish.loktralocationtracker.service.TrackingService";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private double latitudeValue = 0.0;
    private double longitudeValue = 0.0;
    private long startTimeInMilliSeconds = 0L;
    private SharedPrefHelperClass sharedPrefHelperClass;
    private static float LatCount = 0;
    private static float LonCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPrefHelperClass = new SharedPrefHelperClass(getApplicationContext());
        if (isRouteTrackingOn()) {
            startTimeInMilliSeconds = System.currentTimeMillis();

        }
        MapHelperClass mapHelperClass = new MapHelperClass();
        mLocationRequest = mapHelperClass.createLocationRequest();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

//        LatCount = LatCount + 0.01f;
        LonCount = LonCount + 0.001f;

        if (isRouteTrackingOn() && startTimeInMilliSeconds == 0) {
            startTimeInMilliSeconds = System.currentTimeMillis();
        }

        if (isRouteTrackingOn() && startTimeInMilliSeconds > 0) {
            latitudeValue = location.getLatitude() + LatCount;
            longitudeValue = location.getLongitude() + LonCount;


            LocationObject locationObject = new LocationObject(System.currentTimeMillis(), latitudeValue, longitudeValue);
            MapsActivity.startToPresentLocations.add(locationObject);

            // send local broadcast receiver to application components
            Intent localBroadcastIntent = new Intent(ACTION);
            localBroadcastIntent.putExtra("RESULT_CODE", "LOCAL");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localBroadcastIntent);
            long timeoutTracking = 2 * 60 * 60 * 1000;
            if (System.currentTimeMillis() >= startTimeInMilliSeconds + timeoutTracking) {
                //turn of the tracking
                sharedPrefHelperClass.setServiceState(false);
                this.stopSelf();
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
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, TrackingService.this);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private boolean isRouteTrackingOn() {
        return sharedPrefHelperClass.getServiceState();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        this.stopSelf();
        super.onDestroy();
    }
}
