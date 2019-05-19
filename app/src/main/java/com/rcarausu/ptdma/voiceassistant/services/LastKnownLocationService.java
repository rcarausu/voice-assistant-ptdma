package com.rcarausu.ptdma.voiceassistant.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.rcarausu.ptdma.voiceassistant.utils.RequestCodes;

import java.util.logging.Level;
import java.util.logging.Logger;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LastKnownLocationService {


    private static final LastKnownLocationService INSTANCE = new LastKnownLocationService();

    private Location lastKnownLocation;
    private LocationCallback locationCallback;

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    private LastKnownLocationService() {}

    // Declared as singleton so all classes use the same location
    public static LastKnownLocationService getInstance() {
        return INSTANCE;
    }

    public void grantLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                RequestCodes.REQUEST_LOCATION_PERMISSIONS_CODE);
    }

    public boolean checkLocationPermissions(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void setLocationHandlersAndCallback(Activity activity) {

        Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(activity).getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lastKnownLocation = location;
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't set last known location");
            }
        });

        setLocationCallback();
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest()
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setLocationCallback() {

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    lastKnownLocation = locationResult.getLastLocation();
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(Activity activity) {

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);

        client.requestLocationUpdates(createLocationRequest(),
                locationCallback,
                null);
    }

    public void stopLocationUpdates(Activity activity) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
        client.removeLocationUpdates(locationCallback);
    }

}
