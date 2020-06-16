package com.example.wequest.wequest.basicActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestsToSupplierAdapter;
import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.example.wequest.wequest.interfaces.OnRequestListener;
import com.example.wequest.wequest.mapActivities.RequesterMapActivity;
import com.example.wequest.wequest.mapActivities.SupplierSeeRequesterMapsActivity;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.DistanceScrollBarUtilies;
import com.example.wequest.wequest.utils.LocationHandler;
import com.example.wequest.wequest.utils.ToolTipHintShower;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_NAME;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.example.wequest.wequest.mapActivities.SupplierSeeRequesterMapsActivity.REQUEST_FETCH_TYPE;
import static com.example.wequest.wequest.utils.WeQuestOperation.ONE_REQUEST_NEEDS;

public class NeedRequestActivity extends AppCompatActivity implements OnRequestListener, CustomLocationListener, DistanceScrollBarUtilies.OnScrollDistanceSetListener {


    private static final String IS_NEW_NEED_TOOLTIP_SHOWN = "IS_NEW_NEED_TOOLTIP_SHOWN";
    private String needKey;
    private String needName;
    private ListView requestsList;
    private RequestsToSupplierAdapter requestsForMeAdapter;
    private ArrayList<Request> requests;
    private FloatingActionButton addNewRequest;
    private FloatingActionButton subscribe;
    private AppCompatSeekBar seekBar;
    private LatLng supplierLL;
    private LoadToast requestLoadingToast;
    private int currentSeekbarDistanceValue;
    private LocationHandler handler;
    private Location location;

    @Override
    public void onLocationFailed(ConnectionResult connectionRequest) {
        requestLoadingToast.error();
        MDToast.makeText(this, "There was an error on retrieving Location\nTry Again Latter", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_main, menu);
        menu.getItem(0).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.map_view) {
            Intent intent = new Intent(this, SupplierSeeRequesterMapsActivity.class);
            intent.putExtra(NEED_KEY, needKey);
            intent.putExtra(REQUEST_FETCH_TYPE, ONE_REQUEST_NEEDS);
            startActivity(intent);
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_request);
        needKey = getIntent().getStringExtra(NEED_KEY);
        handler = new LocationHandler(this, this);

        currentSeekbarDistanceValue = 500;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        needName = getIntent().getStringExtra(NEED_NAME);
        // some data
        viewBinding();
        setListeners();
        makeToolTipTexts();
        makeToast();
    }

    private void makeToolTipTexts() {


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isShown = preferences.getBoolean(IS_NEW_NEED_TOOLTIP_SHOWN, false);

        if (true) {


            ToolTipHintShower.builder(this, R.id.subscribe,
                    R.string.need_sub_tooltip, R.string.need_sub_tooltip_secondary)
                    .setPromptStateChangeListener((prompt, state) -> {
                        Log.e("STATE", "" + state);
                        switch (state) {
                            case MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED:
                                ToolTipHintShower.builder(this, R.id.add_new_request
                                        , R.string.add_request_tooltip,
                                        R.string.add_request_tooltip_secondary).show();
                        }
                    })
                    .show();

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(IS_NEW_NEED_TOOLTIP_SHOWN, true);
            editor.apply();
        }
    }

    private void makeToast() {
        requestLoadingToast = new LoadToast(this);
        requestLoadingToast.setTranslationY(64);
        requestLoadingToast.setText("Please wait...");

    }

    private void setListeners() {
        addNewRequest.setOnClickListener(view -> {

                    Intent intent = new Intent(NeedRequestActivity.this, RequesterMapActivity.class);
                    intent.putExtra(NEED_KEY, needKey);
                    intent.putExtra(NEED_NAME, needName);
                    startActivity(intent);
                }
        );
        subscribe.setOnClickListener(v -> {
            if (location == null) {
                requestLoadingToast.setText("Getting Location");
                requestLoadingToast.show();
                handler.getLocationPermission();

            } else {
                WeQuestOperation.subscribe(NeedRequestActivity.this, location, needKey);
            }

        });
        requestsList.setOnItemClickListener((parent, view, position, id) -> {
                    Intent intent = new Intent(NeedRequestActivity.this, RequestDetailActivity.class);
                    intent.putExtra(REQUEST_OBJECT, requests.get(position));
                    startActivity(intent);
                }
        );

        DistanceScrollBarUtilies.setListeners(seekBar, this);
    }

    private void viewBinding() {
        requestsList = findViewById(R.id.requesters);
        seekBar = findViewById(R.id.distance_seekbar);

        requestsList.setEmptyView(findViewById(R.id.no_request_found));
        addNewRequest = findViewById(R.id.add_new_request);
        subscribe = findViewById(R.id.subscribe);
    }

    @Override
    public void onRequestFetched(ArrayList<Request> requests) {

        if (requests != null) {
            this.requests = requests;
            requestsForMeAdapter = new RequestsToSupplierAdapter(this, R.layout.request_list_item, requests);
            requestsList.setAdapter(requestsForMeAdapter);

        }
        requestLoadingToast.success();
    }


    @Override
    protected void onStart() {
        super.onStart();
        fetchRequests();
    }

    private void fetchRequests() {
        requestLoadingToast.show();

        if (supplierLL != null)
            WeQuestOperation.fetchNeedRequests(ONE_REQUEST_NEEDS, needKey, this, currentSeekbarDistanceValue, supplierLL);
        else
            handler.getLocationPermission();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // there are to returning back to onAR, in either of them, we should call this method


        if (resultCode == RESULT_OK)
            handler.getLocationPermission();
        else {
            requestLoadingToast.error();
            MDToast.makeText(this, "Location not enabled", MDToast.TYPE_ERROR, MDToast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        handler.handleRequestPermissionResult(requestCode, grantResults);

    }

    @Override
    public void onLocationFetched(Location location) {
        this.location = location;
        supplierLL = new LatLng(location.getLatitude(), location.getLongitude());
        WeQuestOperation.fetchNeedRequests(ONE_REQUEST_NEEDS, needKey, this, currentSeekbarDistanceValue, supplierLL);

    }

    @Override
    public void onDistanceChanged(int currentSeekbarDistanceValue) {
        this.currentSeekbarDistanceValue = currentSeekbarDistanceValue;
        Log.e(getClass().getSimpleName(), String.valueOf(currentSeekbarDistanceValue));

        fetchRequests();
    }
}
