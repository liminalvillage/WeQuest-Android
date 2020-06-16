package com.example.wequest.wequest.mapActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.RequestDetailEntryActivity;
import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.LocationHandler;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_NAME;

public class RequesterMapActivity extends FragmentActivity implements OnMapReadyCallback, CustomLocationListener {

    private static final int LOCATION_REQ_PERMISSION = 3123;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private LatLng userLatLng;
    private int userCurrentKarma;
    private long dueDate;
    private LoadToast transactionToast;
    private double userTimeCredit;
    private String needKey;
    private String needName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps_requester);

        WeQuestOperation operation = new WeQuestOperation(FireBaseHelper.getUid());
        operation.setOnUserFetchedListener(new OnUserListener() {
            @Override
            public void onUserFetched(User user) {
                userCurrentKarma = user.getKarma();
                userTimeCredit = user.getTimeCredit();
                needKey = getIntent().getStringExtra(NEED_KEY);
                needName = getIntent().getStringExtra(NEED_NAME);
            }
        });

        FloatingActionButton goRequestEntryAct = findViewById(R.id.go_request_act);
        goRequestEntryAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (location == null)
                    MDToast.makeText(RequesterMapActivity.this, "Location is not defined").show();

                else {
                    Intent intent = new Intent(RequesterMapActivity.this, RequestDetailEntryActivity.class);
                    intent.putExtra(NEED_KEY, needKey);
                    intent.putExtra(NEED_NAME, needName);
                    intent.putExtra("location", userLatLng);
                    startActivity(intent);

                }
            }
        });

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
    private LocationHandler handler;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        handler = new LocationHandler(this, this);
        handler.getLocationPermission();

    }


    public void MoveToCurrentLocation(View view) {
        if (location == null)
            handler.getLocationPermission();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        handler.handleRequestPermissionResult(requestCode, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }


    public void setNewLatLngToCamera(double lat, double log) {
        // Geo Hash

        LatLng userLoc = new LatLng(lat, log);
        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(userLoc).title("Marker"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                userLatLng = latLng;
                setNewLatLngToCamera(latLng.latitude, latLng.longitude);
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (null != this.mGoogleApiClient)
            mGoogleApiClient.connect();
    }


    @Override
    public void onBackPressed() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel the request");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }


    public void backToMain(View view) {
        onBackPressed();
    }

    @Override
    public void onLocationFetched(Location location) {
        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        this.location = location;
        setNewLatLngToCamera(userLatLng.latitude, userLatLng.longitude);
    }

    @Override
    public void onLocationFailed(ConnectionResult connectionRequest) {
        MDToast.makeText(this, "There was an error on retrieving Location\nTry Again Latter", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }
}
