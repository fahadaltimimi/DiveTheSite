package com.fahadaltimimi.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationController {

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private static LocationController sLocationController;
    private LocationCallback mCurrentLocationCallback = null;

    // The private constructor forces users to use RunManager.get(Context)
    private LocationController() {
        //
    }

    public static LocationController getLocationControler() {
        if (sLocationController == null) {
            // Use the application context to avoid leaking activities
            sLocationController = new LocationController();
        }
        return sLocationController;
    }

    public void startLocationUpdates(Context context, LocationCallback locationCallback) {
        if (context != null && isLocationEnabled(context.getApplicationContext()) &&
                (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) &&
                (locationCallback != null) && (mCurrentLocationCallback != locationCallback)) {
            stopLocationUpdates(context);

            // Create the location request to start receiving updates
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(context.getApplicationContext());
            settingsClient.checkLocationSettings(locationSettingsRequest);

            mCurrentLocationCallback = locationCallback;
            getFusedLocationProviderClient(context.getApplicationContext()).requestLocationUpdates(locationRequest, mCurrentLocationCallback, Looper.myLooper());
        }
    }

    public void stopLocationUpdates(Context context) {
        if (context != null && mCurrentLocationCallback != null) {
            LocationServices.getFusedLocationProviderClient(context.getApplicationContext()).removeLocationUpdates(mCurrentLocationCallback);
            mCurrentLocationCallback = null;
        }
    }

    public Location getLocation(Context context, Location defaultLocation) {
        if ((ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) &&
                LocationServices.getFusedLocationProviderClient(context.getApplicationContext()).getLastLocation().isComplete() &&
                LocationServices.getFusedLocationProviderClient(context.getApplicationContext()).getLastLocation().getResult() != null) {
            return LocationServices.getFusedLocationProviderClient(context.getApplicationContext()).getLastLocation().getResult();
        } else {
            return defaultLocation;
        }
    }

    public boolean isLocationEnabled(Context context) {
        android.location.LocationManager manager = (android.location.LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        return (manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && manager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER));
    }
}
