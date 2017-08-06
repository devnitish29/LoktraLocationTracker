package learning.nitish.loktralocationtracker.model;

import java.io.Serializable;

/**
 * Created by Nitish Singh Rathore on 6/8/17.
 */

public class LocationObject implements Serializable {


    long time;
    double latitude;
    double longitude;


    public LocationObject(long time, double latitude, double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
