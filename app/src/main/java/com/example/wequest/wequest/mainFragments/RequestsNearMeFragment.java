package com.example.wequest.wequest.mainFragments;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestsToSupplierAdapter;
import com.example.wequest.wequest.basicActivities.MainActivity;
import com.example.wequest.wequest.basicActivities.RequestDetailActivity;
import com.example.wequest.wequest.databinding.FragmentRequestsNearMeBinding;
import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.example.wequest.wequest.interfaces.OnRequestListener;
import com.example.wequest.wequest.mapActivities.SupplierSeeRequesterMapsActivity;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.DistanceScrollBarUtilies;
import com.example.wequest.wequest.utils.LocationHandler;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.example.wequest.wequest.mapActivities.SupplierSeeRequesterMapsActivity.REQUEST_FETCH_TYPE;
import static com.example.wequest.wequest.utils.WeQuestOperation.ALL_REQUEST_NEEDS;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsNearMeFragment extends Fragment implements CustomLocationListener, OnRequestListener, DistanceScrollBarUtilies.OnScrollDistanceSetListener {

    @Override
    public void onLocationFailed(ConnectionResult connectionRequest) {
        requestLoadingToast.error();
        if (getContext() != null)
            MDToast.makeText(getContext(), "There was an error on retrieving Location\nTry Again Latter", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }

    private FragmentRequestsNearMeBinding binding;
    private LocationHandler handler;
    private int currentSeekbarDistanceValue;
    private LoadToast requestLoadingToast;
    private List<Request> requests;
    private LatLng supplierLL;

    public RequestsNearMeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestFetched(ArrayList<Request> requests) {

        if (requests != null && getContext() != null) {
            this.requests = requests;
            RequestsToSupplierAdapter requestsForMeAdapter = new RequestsToSupplierAdapter(getContext(), R.layout.request_list_item, requests);
            binding.requesters.setAdapter(requestsForMeAdapter);
        }
        requestLoadingToast.success();
    }

    private void fetchRequests() {
        requestLoadingToast.show();

        if (supplierLL != null)
            WeQuestOperation.fetchNeedRequests(ALL_REQUEST_NEEDS, "", this, currentSeekbarDistanceValue, supplierLL);
        else
            handler.getLocationPermission();

    }


    @Override
    public void onStart() {
        super.onStart();
        fetchRequests();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_requests_near_me, container, false);
        setHasOptionsMenu(true);
        binding.requesters.setEmptyView(binding.noRequestFound);
        handler = new LocationHandler((MainActivity) getContext(), this);

        currentSeekbarDistanceValue = 500;

        setListeners();
        makeToast();


        return binding.getRoot();
    }

    private void makeToast() {
        requestLoadingToast = new LoadToast(getContext());
        requestLoadingToast.setTranslationY(64);
        requestLoadingToast.setText("Please wait...");

    }

    private void setListeners() {
        binding.requesters.setOnItemClickListener((parent, view, position, id) -> {

//                Request request = requests.get(position);
//
//                    Intent intent = new Intent(getContext(), ChooseSupplierActivity.class);
//                    intent.putExtra(USER_REQUEST_PATH, request.getUid()+"/"+request.getNeedKey());
//                    intent.putExtra(USER_PROVIDER, FirebaseAuth.getInstance().getUid());
//                    intent.putExtra(ChatControllerUtil.IS_USER_SUPPLIER, true);
//                    intent.putExtra(SUPPLIER_NAME, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
//                    parent.getContext().startActivity(intent);

                    Intent intent = new Intent(getContext(), RequestDetailActivity.class);
                    intent.putExtra(REQUEST_OBJECT, requests.get(position));
                    startActivity(intent);
                }
        );

        DistanceScrollBarUtilies.setListeners(binding.seekbarLayout.distanceSeekbar, this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        new MenuInflater(getContext()).inflate(R.menu.menu_main, menu);
        menu.getItem(0).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.map_view) {
            Intent intent = new Intent(getContext(), SupplierSeeRequesterMapsActivity.class);
            intent.putExtra(NEED_KEY, "");
            intent.putExtra(REQUEST_FETCH_TYPE, ALL_REQUEST_NEEDS);
            startActivity(intent);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationFetched(Location location) {
        supplierLL = new LatLng(location.getLatitude(), location.getLongitude());
        WeQuestOperation.fetchNeedRequests(ALL_REQUEST_NEEDS, "", this, currentSeekbarDistanceValue, supplierLL);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // there are to returning back to onAR, in either of them, we should call this method


        if (resultCode == RESULT_OK)
            handler.getLocationPermission();
        else {
            requestLoadingToast.error();
            MDToast.makeText(getContext(), "Location not enabled", MDToast.TYPE_ERROR, MDToast.LENGTH_LONG).show();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        handler.handleRequestPermissionResult(requestCode, grantResults);

    }

    @Override
    public void onDistanceChanged(int currentSeekbarDistanceValue) {
        this.currentSeekbarDistanceValue = currentSeekbarDistanceValue;
        Log.e(getClass().getSimpleName(), String.valueOf(currentSeekbarDistanceValue));

        fetchRequests();
    }
}
