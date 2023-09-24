package com.example.tours;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Building implements Serializable {

    private final String id;

    private final LatLng latLng;
    private final double radius;

    private final String address;
    private final String description;
    private final String fenceColor;

    private final String imageURL;

    public Building(String id, LatLng latLng, double radius, String address, String description, String fenceColor, String imageURL) {
        this.id = id;
        this.latLng = latLng;
        this.radius = radius;
        this.address = address;
        this.description = description;
        this.fenceColor = fenceColor;
        this.imageURL = imageURL;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getImageURL() {
        return imageURL;
    }

//    building(String id, LatLng latLng, double radius) {
//        this.id = id;
//        this.latLng = latLng;
//        this.radius = radius;
//    }

    String getId() {
        return id;
    }

    LatLng getLatLng() {
        return latLng;
    }

    float getRadius() {
        return (float) radius;
    }


    @Override
    public String toString() {
        return "Building{" +
                "id='" + id + '\'' +
                ", latLng=" + latLng +
                ", radius=" + radius +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", fenceColor='" + fenceColor + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }
}
