package org.kn.trackme.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TrackMeInfo extends RealmObject {

    @PrimaryKey
    private long id;
    private double distance;
    private long duration;
    private double avgSpeed;
    private RealmList<String> location;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public RealmList<String> getLocation() {
        return location;
    }

    public void setLocation(RealmList<String> location) {
        this.location = location;
    }
}
