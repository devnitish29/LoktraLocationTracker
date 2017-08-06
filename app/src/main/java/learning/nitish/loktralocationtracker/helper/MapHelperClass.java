package learning.nitish.loktralocationtracker.helper;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import learning.nitish.loktralocationtracker.activity.MapsActivity;
import learning.nitish.loktralocationtracker.model.LocationObject;

/**
 * Created by Nitish Singh Rathore on 6/8/17.
 */

public class MapHelperClass {
    public static final String TAG = MapsActivity.class.getSimpleName();

    public void markStartingLocationOnMap(Context context, GoogleMap mapObject, LatLng location) {
        mapObject.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title(getMarkerInfo(context, location)));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    public void markEndingLocationOnMap(Context context, GoogleMap mapObject, LatLng location) {

        mapObject.addMarker(new MarkerOptions()
                .position(location)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(getMarkerInfo(context, location)));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
    }


    public List<LatLng> getPoints(List<LocationObject> mLocations) {
        List<LatLng> points = new ArrayList<LatLng>();
        for (LocationObject mLocation : mLocations) {
            points.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }
        return points;
    }

    public void startPolyline(GoogleMap map, LatLng location) {
        if (map == null) {
            Log.e(TAG, "Map object is not null");
            return;
        }
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.add(location);
        map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(12f)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {

        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true).clickable(true);
        options.addAll(positions);
        map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(0).latitude, positions.get(0).longitude))
                .zoom(17)
                .bearing(90)
                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }


    public String getMarkerInfo(Context context, LatLng location) {
        String info = "";
        String sublocality = "";
        String locality = "";

        Geocoder geocoder = new Geocoder(context.getApplicationContext());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 5);

            if (addressList != null && addressList.size() > 0) {

                if (addressList.get(0).getSubLocality() != null) {
                    sublocality = addressList.get(0).getSubLocality();
                }

                if (addressList.get(0).getLocality() != null) {
                    locality = addressList.get(0).getLocality();
                }
                if (sublocality.length() == 0) {
                    info = locality;
                } else if (locality.length() == 0) {
                    info = sublocality;
                } else {
                    info = sublocality.concat(",").concat(locality);
                }

            } else {
                info = "Sorry!!! No Info";
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;


    }

}
