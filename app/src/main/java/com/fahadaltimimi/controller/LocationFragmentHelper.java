package com.fahadaltimimi.controller;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Objects;

/**
 * Helper class for Location fragments. Used to start location services, check for location permissions and retrieve location.
 */

public class LocationFragmentHelper implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationFragmentHelper";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final double EARTHRADIUS = 6366198;

    private Fragment mFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationListener mLocationListener;

    public LocationFragmentHelper(Fragment fragment, LocationListener locationListener) {
        mFragment = fragment;
        mLocationListener = locationListener;

        mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(fragment.getActivity()))
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!LocationController.getLocationControler().isLocationEnabled(fragment.getActivity())) {
            Toast.makeText(fragment.getActivity(), "Enable location services for accurate data", Toast.LENGTH_SHORT).show();
        }

        requestLocationPermission();
    }

    /**
     * Starts location services. Can be called from fragments onStart to connect the google API client when fragment starts.
     */
    public void startLocationService() {
        mGoogleApiClient.connect();
    }

    /**
     * Stops location services. Can be called from fragments onStop to disconnect the google API client when fragment stops.
     */
    public void stopLocationService() {
        mGoogleApiClient.disconnect();
    }

    /**
     * Starts location updates to retrieve the devices current location if possible for the fragment.
     * @return Boolean determining if location updates were able to start
     */
    public boolean startLocationUpdates() {
        if (LocationController.getLocationControler().isLocationEnabled(mFragment.getActivity())) {
            LocationController.getLocationControler().startLocationUpdates(mFragment.getActivity(), new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    onLocationChanged(locationResult.getLastLocation());
                }
            });
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks that inputted request code and permission results indicate that location permissions were granted
     * @param requestCode Request code to check if it matches the request code of the location permission when requested
     * @param grantResults Results to check if location permissions were granted successfully
     * @return Boolean if the given details indicate that location permissions were granted
     */
    public boolean isRequestCodeLocationPermissionGranted(int requestCode, int[] grantResults) {
        return (requestCode == MY_PERMISSIONS_REQUEST_LOCATION &&
                (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED));
    }

    /**
     * Get's the range of visible coordinates of the given google map
     * @param googleMap Google map to check the coordinate range for
     * @return Coordinate range for given google map
     */
    public String[] getCoordinateRange(GoogleMap googleMap) {
        String coordinateRange[] = new String[4];
        if (googleMap == null) {
            coordinateRange[0] = "0";
            coordinateRange[1] = "0";
            coordinateRange[2] = "0";
            coordinateRange[3] = "0";
        } else {
            LatLngBounds curMapBounds = googleMap.getProjection()
                    .getVisibleRegion().latLngBounds;

            String minLatitude = String
                    .valueOf(curMapBounds.southwest.latitude);
            String maxLatitude = String
                    .valueOf(curMapBounds.northeast.latitude);

            String minLongitude = String
                    .valueOf(curMapBounds.southwest.longitude);
            String maxLongitude = String
                    .valueOf(curMapBounds.northeast.longitude);

            coordinateRange[0] = minLatitude;
            coordinateRange[1] = maxLatitude;
            coordinateRange[2] = minLongitude;
            coordinateRange[3] = maxLongitude;
        }

        return coordinateRange;
    }

    /**
     * Check for location permissions
     * @return Boolean indicating if location permissions granted already
     */
    public boolean checkLocationPermission() {
        return (ContextCompat.checkSelfPermission(Objects.requireNonNull(mFragment.getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    public boolean canRequestLocationUpdates() {
        return mGoogleApiClient.isConnected() && LocationController.getLocationControler().isLocationEnabled(mFragment.getActivity());
    }

    /**
     * Check for and request, if necessary, location permissions.
     * @return Boolean indicating if permission was granted already. If not, location permissions are requested fromt he user.
     */
    private boolean requestLocationPermission() {
        if (checkLocationPermission()) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mFragment.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mFragment.getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Prompt the user once explanation has been shown
                                mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Called by Location Services when the request to connect the client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        startLocationUpdates();
    }

    /**
     * Called by Location Services when client connection has suspended
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        mFragment.getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Called when the device's location has changed as detected by location services
     * @param location Device's new location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLocationListener.onLocationChanged(location);
    }

    /**
     * Create a new LatLng which lies toNorth meters north and toEast meters
     * east of startLL
     */
    public static LatLng move(LatLng startLL, double toNorth, double toEast) {
        double lonDiff = meterToLongitude(toEast, startLL.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startLL.latitude + latDiff, startLL.longitude
                + lonDiff);
    }

    /**
     * Calculates longitude value using given meters to east and latitude values
     */
    public static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        return Math.toDegrees(rad);
    }

    /**
     * Calculates latitude value using given meters to north
     */
    public static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        return Math.toDegrees(rad);
    }

    /**
     * Show a dialog returned by Google Play services for the connection error code
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        apiAvailability.getErrorDialog(mFragment.getActivity(), errorCode, MY_PERMISSIONS_REQUEST_LOCATION).show();
    }
}
