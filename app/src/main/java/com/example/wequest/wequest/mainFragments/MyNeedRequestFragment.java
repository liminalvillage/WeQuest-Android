package com.example.wequest.wequest.mainFragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestListAdapter;
import com.example.wequest.wequest.databinding.FragmentMyNeedRequestBinding;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.NeedRequestUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.view.View.GONE;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyNeedRequestFragment extends Fragment {


    private FragmentMyNeedRequestBinding binding;
    private ArrayList<Request> requestsList;

    public MyNeedRequestFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_need_request, container, false);
        if (getContext() == null)
            return binding.getRoot();
         /*
            getting the time server which was saved in ShPref
         */

        requestsList = new ArrayList<>();
        long timeServer = NeedRequestUtil.getTimeServer(getContext());
        RequestListAdapter requestAdapter;
        requestAdapter = new RequestListAdapter(getContext(), R.layout.item_my_requests, requestsList, timeServer);
        binding.myRequestListview.listViewRequests.setEmptyView(binding.noRequestView);
        binding.myRequestListview.listViewRequests.setAdapter(requestAdapter);

        binding.myRequestListview.swipRefresh.setOnRefreshListener(() -> {
            binding.noRequestView.setVisibility(View.INVISIBLE);
            binding.myRequestListview.listViewRequests.setVisibility(GONE);
            updateMyRequestList();
        });


        binding.myRequestListview.listViewRequests.setAdapter(requestAdapter);


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMyRequestList();

    }

    void updateMyRequestList() {
        {
            binding.myRequestListview.swipRefresh.setRefreshing(true);

            DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference
                    (FIREBASE_USER_REQUESTS_PATH + FireBaseHelper.getUid());

            requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    requestsList.clear();
                    if (snapshot.exists()) {

                        for (DataSnapshot requestSnap : snapshot.getChildren()) {
                            Request myRequest = requestSnap.getValue(Request.class);
                            requestsList.add(myRequest);
                        }
                    }

                    ((RequestListAdapter) binding.myRequestListview.listViewRequests.getAdapter()).notifyDataSetChanged();
                    binding.myRequestListview.listViewRequests.setVisibility(View.VISIBLE);
                    binding.myRequestListview.swipRefresh.setRefreshing(false);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(getClass().getSimpleName(), databaseError.getDetails());
                }
            });


            // request received
        }
    }
}
