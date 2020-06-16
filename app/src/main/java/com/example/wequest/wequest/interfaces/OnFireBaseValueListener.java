package com.example.wequest.wequest.interfaces;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by mohammed on 09/01/18.
 */

public interface OnFireBaseValueListener {

    void onValueFetched(DataSnapshot snapshot);
}
