package com.example.wequest.wequest.interfaces;

import android.location.Location;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by mohammed on 02/01/18.
 */

public interface CustomLocationListener {

    void onLocationFetched(Location location);

    void onLocationFailed(ConnectionResult connectionRequest);

}
