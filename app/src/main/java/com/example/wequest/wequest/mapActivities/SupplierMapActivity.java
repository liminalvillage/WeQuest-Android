package com.example.wequest.wequest.mapActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.RequestDetailActivity;
import com.example.wequest.wequest.models.Request;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.valdesekamdem.library.mdtoast.MDToast;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FINISH_ACTIVITY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;

public class SupplierMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Request userRequest;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_supplier);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the fetchUser will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the fetchUser has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        userRequest = getIntent().getParcelableExtra(REQUEST_OBJECT);


        if (userRequest == null) {
            // then we only have the LatLng
            latLng = getIntent().getParcelableExtra("location");

        } else {
            latLng = new LatLng(userRequest.getLatitude(), userRequest.getLongitude());
            // else we have the complete Request Object
            findViewById(R.id.more_details).setVisibility(View.VISIBLE);


        }
        setNewLatLngToCamera(latLng.latitude, latLng.longitude);
    }


    public void MoveToCurrentLocation(View view) {
        if (latLng != null)
            setNewLatLngToCamera(latLng.latitude,latLng.longitude);
        else
            MDToast.makeText(this, "Location Not Defined", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }


    public void setNewLatLngToCamera(double lat, double log) {
        // Geo Hash

        LatLng userLoc = new LatLng(lat, log);
        mMap.addMarker(new MarkerOptions().position(userLoc).title("Marker"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
    }

    public void requestDetail(View view) {
        Intent intent = new Intent(SupplierMapActivity.this, RequestDetailActivity.class);
        intent.putExtra(REQUEST_OBJECT, userRequest);

        startActivityForResult(intent, FINISH_ACTIVITY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_ACTIVITY) {
            finish();
        }
    }
}
