package com.example.wequest.wequest.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetAvailabilityChecker {

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManger = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManger == null)
            return false;

        NetworkInfo info = connectivityManger.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }
}
