package com.example.wequest.wequest.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.LOCATION_REQ_PERMISSION;

public class LocationHandler implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CHECK_SETTINGS = 30140;
    private Activity mContext;
    private CustomLocationListener myLocationListener; // interface
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;



    public LocationHandler(Activity mContext, CustomLocationListener myLocationListener) {
        this.mContext = mContext;

        this.myLocationListener = myLocationListener;

    }

    private synchronized void buildGoogleApiClient() {


        if (mGoogleApiClient == null) {
            Log.e("Connected", "its null");

            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.connect();
        }
    }


    public void getLocationPermission() {

        // checking if the permission os granted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mContext.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_PERMISSION);
                return;
            }

            buildGoogleApiClient();
        } else {
            buildGoogleApiClient();

        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {


        Log.e("Connected", "Connected");

        mLocationRequest = new LocationRequest();
//      mLocationRequest.setInterval(10000);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();


        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(mContext)
                .checkLocationSettings(locationSettingsRequest);


        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);


                /**
                 * Check if Settings->Location is enabled/disabled
                 * Not app specific permission (location)
                 * Here I am talking of the scenario where Settings->Location is disabled and user runs the app.
                 */
                // All location settings are satisfied. The client can initialize location

                getLastLocation();
                // requests here.
            } catch (ApiException exception) {



                /**
                 * Go in exception because Settings->Location is disabled.
                 * First it will Enable Location Services (GPS) then check for run time permission to app.
                 */
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),

                            /**
                             * Display enable Enable Location Services (GPS) dialog like Google Map and then
                             * check for run time permission to app.
                             */

                            // and check the result in onActivityResult().
                            mGoogleApiClient.disconnect();


                            resolvable.startResolutionForResult(
                                    mContext,
                                    REQUEST_CHECK_SETTINGS);

                            getLastLocation();


                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                myLocationListener.onLocationFetched(locationResult.getLastLocation());
            }


        }, Looper.myLooper());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(getClass().getSimpleName(), "Connection Suspended :" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(getClass().getSimpleName(), connectionResult.getErrorMessage());
        myLocationListener.onLocationFailed(connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {

    }


    public void handleRequestPermissionResult(int requestCode, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQ_PERMISSION:
                if (grantResults[0] == 0) {
                    this.getLocationPermission();

                } else {
                    Toast.makeText(mContext, "Location Permission Denied" + grantResults[0], Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}
