package com.example.wequest.wequest.interfaces;

import com.example.wequest.wequest.models.Request;

import java.util.ArrayList;

public interface OnRequestListener {
    void onRequestFetched(ArrayList<Request> requests);
}
