package com.fahadaltimimi.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.fahadaltimimi.controller.LocationFragmentHelper;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;

import java.util.Objects;

public abstract class LocationListFragment extends BaseListFragment
        implements LocationListener {

    private LocationFragmentHelper mLocationFragmentHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationFragmentHelper = new LocationFragmentHelper(this, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationFragmentHelper.startLocationService();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationFragmentHelper.stopLocationService();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, Objects.requireNonNull(grantResults));

        if (mLocationFragmentHelper.isRequestCodeLocationPermissionGranted(requestCode, grantResults)) {
            onLocationPermissionGranted();
        }
    }

    protected String[] getCoordinateRange(GoogleMap googleMap) {
        return mLocationFragmentHelper.getCoordinateRange(googleMap);
    }

    protected boolean startLocationUpdates() {
        return mLocationFragmentHelper.startLocationUpdates();
    }

    protected boolean checkLocationPermission() {
        return mLocationFragmentHelper.checkLocationPermission();
    }

    protected  boolean canRequestLocationUpdates() {
        return mLocationFragmentHelper.canRequestLocationUpdates();
    }

    protected abstract void onLocationPermissionGranted();
}
