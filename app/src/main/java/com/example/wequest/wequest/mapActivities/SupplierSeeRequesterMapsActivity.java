package com.example.wequest.wequest.mapActivities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.FragmentActivity;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestsToSupplierAdapter;
import com.example.wequest.wequest.basicActivities.RequestDetailActivity;
import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.example.wequest.wequest.interfaces.OnRequestListener;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.DistanceScrollBarUtilies;
import com.example.wequest.wequest.utils.LocationHandler;
import com.example.wequest.wequest.utils.MapMarkerFactory;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.example.wequest.wequest.utils.DistanceScrollBarUtilies.OnScrollDistanceSetListener;
import static com.example.wequest.wequest.utils.WeQuestOperation.ONE_REQUEST_NEEDS;

public class SupplierSeeRequesterMapsActivity extends FragmentActivity implements OnMapReadyCallback, CustomLocationListener, OnRequestListener, OnScrollDistanceSetListener {

    public static final String REQUEST_FETCH_TYPE = "REQUEST_FETCH_TYPE";
    private GoogleMap mMap;

    private String needKey;
    private ListView requestersList;
    private AppCompatSeekBar seekBar;
    private ArrayList<Request> requests;
    private RequestsToSupplierAdapter requestsForMeAdapter;
    private LoadToast requestLoadingToast;
    private TextView requestsFound;
    private int currentSeekbarDistanceValue;
    private LatLng supplierLL;
    private LocationHandler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_see_requester_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        setUpFields();
        setListeners();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handler.handleRequestPermissionResult(requestCode, grantResults);
    }

    private void setListeners() {
        requestersList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(SupplierSeeRequesterMapsActivity.this, RequestDetailActivity.class);
            intent.putExtra(REQUEST_OBJECT, requests.get(i));
            startActivity(intent);
        });

        DistanceScrollBarUtilies.setListeners(seekBar, this);

    }

    private void setUpFields() {
        currentSeekbarDistanceValue = 250;
        seekBar = findViewById(R.id.distance_seekbar);
        requestLoadingToast = new LoadToast(this);
        requestersList = findViewById(R.id.requesters);
        needKey = getIntent().getStringExtra(NEED_KEY);
        requestersList = findViewById(R.id.requesters);
        requests = new ArrayList<>();
        requestsFound = findViewById(R.id.requests_found);

        requestersList.setEmptyView(findViewById(R.id.no_request_found));


        requestLoadingToast.setText("Please wait...");
        requestLoadingToast.setBorderWidthDp(8);
        requestLoadingToast.setTranslationY(64);
        requestLoadingToast.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        handler = new LocationHandler(this, this);
        fetchRequests();

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void fetchRequests() {
        requests = new ArrayList<>();

        requestsForMeAdapter = new RequestsToSupplierAdapter(this, R.layout.request_list_item, requests);
        requestersList.setAdapter(requestsForMeAdapter);
        requestLoadingToast.show();

        handler.getLocationPermission();
    }

    public void setRequestOnTheMap(double lat, double log, int pos) {
        // Geo Hash

        LatLng userLoc = new LatLng(lat, log);
        createMarker(userLoc,requests.get(pos).getUid());


    }



    private void createMarker(final LatLng userLoc, String uid) {

        final MapMarkerFactory markerFactory = new MapMarkerFactory(this, markerOptions -> {
            mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
        },userLoc);


        markerFactory.initializeMarkerForUser(uid);

    }


    public void setSupplierOnTheMap(double lat, double log) {

        LatLng userLoc = new LatLng(lat, log);
        mMap.addMarker(new MarkerOptions().position(userLoc).title(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
    }

    @Override
    public void onRequestFetched(ArrayList<Request> requests) {


        if (requests != null) {
            requestsFound.setText(getString(R.string.requests_found,requests.size()));

            if(requests.isEmpty()) {

                mMap.clear();
                setSupplierOnTheMap(supplierLL.latitude,supplierLL.longitude);
             } else {

                this.requests = requests;
                requestsForMeAdapter = new RequestsToSupplierAdapter(this, R.layout.request_list_item, requests);
                requestersList.setAdapter(requestsForMeAdapter);

                for (int i = 0; i < requests.size(); i++) {
                    Request request = requests.get(i);
                    setRequestOnTheMap(request.getLatitude(), request.getLongitude(), i);
                }
            }

        }
        requestLoadingToast.success();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == LocationHandler.REQUEST_CHECK_SETTINGS){
            handler.getLocationPermission();
        }
    }

    @Override
    public void onLocationFetched(Location location) {
        setSupplierOnTheMap(location.getLatitude(), location.getLongitude());


        supplierLL = new LatLng(location.getLatitude(), location.getLongitude());


        // here we retrieve all the requests for a specific needs, but first only the UID of that request so that i can then
        // use to to retrieve all the data of that request in the request path
        // then...........
        int requestNeedType = getIntent().getIntExtra(REQUEST_FETCH_TYPE, ONE_REQUEST_NEEDS);
        WeQuestOperation.fetchNeedRequests(requestNeedType, needKey, this, currentSeekbarDistanceValue, supplierLL);
    }

    @Override
    public void onLocationFailed(ConnectionResult connectionRequest) {
        requestLoadingToast.error();
        MDToast.makeText(this, "There was an error on retrieving Location\nTry Again Latter", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDistanceChanged(int currentSeekbarDistanceValue) {
        this.currentSeekbarDistanceValue = currentSeekbarDistanceValue;
        fetchRequests();
    }



}
