package com.example.wequest.wequest.mapActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.SupplierListAdapter;
import com.example.wequest.wequest.basicActivities.SupplierDetailsActivity;
import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.MapMarkerFactory;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_PROVIDER;

public class RequesterSeeSupplierMapActivity extends FragmentActivity implements OnUserListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat;
    private double log;

    private String requestPath;
    private ListView suppliersListView;
    ArrayList<User> providers;

    private TextView userFound;

    private SupplierListAdapter supplierAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_requester_and_subscribers);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        initializeFields();


    }


    private void initializeFields() {

        //view Binidng
        suppliersListView = findViewById(R.id.suppliers);
        TextView noRespond = findViewById(R.id.no_respond);
        suppliersListView.setEmptyView(noRespond);

        userFound = findViewById(R.id.users_found);


        // gettings from the intent
        Request userRequest = getIntent().getParcelableExtra(REQUEST_OBJECT);
        requestPath = userRequest.getUid() + "/" + userRequest.getNeedKey();

        lat = userRequest.getLatitude();
        log = userRequest.getLongitude();


        // arrayList and adapter
        providers = new ArrayList<>();

        supplierAdapter = new SupplierListAdapter(this, R.layout.item_check_supplier, providers, requestPath);
        suppliersListView.setAdapter(supplierAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

        providers.clear();
        DatabaseReference suppliersPath = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_REQUESTS_PATH + requestPath)
                .child("providers");

        suppliersPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> providersUIDs = new ArrayList<>();

                if (snapshot.exists()) {
                    providersUIDs = (ArrayList<String>) snapshot.getValue();
                    supplierAdapter.setProvidersUIDs(providersUIDs);

                    WeQuestOperation questOperation;

                    for (String provider : providersUIDs) {

                        questOperation = new WeQuestOperation(provider);
                        questOperation.setOnUserFetchedListener(RequesterSeeSupplierMapActivity.this);
                    }
                }
                userFound.setText("Suppliers found:" + providersUIDs.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        suppliersListView.setOnItemClickListener((adapterView, view, position, l) -> {
            Intent intent = new Intent(RequesterSeeSupplierMapActivity.this, SupplierDetailsActivity.class);
            intent.putExtra(USER_PROVIDER, providers.get(position));
            startActivity(intent);
        });
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
        setUserOnTheMap(lat, log);
    }

    public void MoveToCurrentLocation(View view) {
        setUserOnTheMap(lat, log);
    }


    public void setUserOnTheMap(double lat, double log) {
        // Geo Hash

        LatLng userLoc = new LatLng(lat, log);
        mMap.addMarker(new MarkerOptions().position(userLoc).title("Me"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));

    }


    private void createMarker(final LatLng userLoc, User user) {

        MapMarkerFactory markerFactory = new MapMarkerFactory(this,markerOptions ->
        {
                mMap.addMarker(markerOptions);
                mMap.addMarker(markerOptions);

            }
        ,userLoc, true);

        markerFactory.initializeMarkerForUser(user.getUid());

    }

    @Override
    public void onUserFetched(final User user) {

        providers.add(user);
        ((ArrayAdapter) suppliersListView.getAdapter()).notifyDataSetChanged();

        createMarker(new LatLng(user.getLatitude(), user.getLongitude()), user);

    }

}
