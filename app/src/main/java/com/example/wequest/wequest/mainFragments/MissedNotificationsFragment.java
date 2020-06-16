package com.example.wequest.wequest.mainFragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestsToSupplierAdapter;
import com.example.wequest.wequest.databinding.FragmentMissedNotificationsBinding;
import com.example.wequest.wequest.mapActivities.SupplierMapActivity;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;

/**
 * A simple {@link Fragment} subclass.
 */
public class MissedNotificationsFragment extends Fragment {


    public MissedNotificationsFragment() {
        // Required empty public constructor
    }




    private FragmentMissedNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_missed_notifications, container, false);
        binding.listViewRequests.setEmptyView(binding.noNotification);
        binding.swipRefresh.setColorSchemeResources(R.color.refresh1, R.color.refresh2, R.color.refresh3);
        binding.swipRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.noNotification.setVisibility(View.INVISIBLE);
                fetchNotificationRefs();
            }
        });

         fetchNotificationRefs();

        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        fetchNotificationRefs();
    }

    private void fetchNotificationRefs() {

        DatabaseReference requestsToMe = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH).
                child(FireBaseHelper.getUid() + "/requeststome");


        requestsToMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> requestPaths = new ArrayList<>();


                for (DataSnapshot currentRequestPath : snapshot.getChildren()) {

                    if(currentRequestPath.hasChildren()) {
                        String requestKeyPath = "";
                        for (DataSnapshot dataSnapshot : currentRequestPath.getChildren()) {
                            requestKeyPath = currentRequestPath.getKey() + "/" + dataSnapshot.getKey();
                        }
                        requestPaths.add(requestKeyPath);
                    } else {
                        currentRequestPath.getRef().removeValue();
                    }

                }

                initializeNotificationList(requestPaths);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(getClass().getSimpleName(),databaseError.toString() );
            }
        });
    }

    private void initializeNotificationList(ArrayList<String> requestPaths) {

        if(getContext() == null)
            return;

        final ArrayList<Request> userRequests = new ArrayList<>();
        final RequestsToSupplierAdapter requestNotfAdapter = new RequestsToSupplierAdapter(getContext(), R.layout.request_list_item, userRequests);
        binding.listViewRequests.setAdapter(requestNotfAdapter);

        DatabaseReference requestPath = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH);
        for (int i = 0; i < requestPaths.size(); i++) {


            requestPath.child(requestPaths.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        Request request = dataSnapshot.getValue(Request.class);
                        // todo think about this  later on, i dont like call notify DataSet Changed everytime
                        userRequests.add(request);
                        requestNotfAdapter.notifyDataSetChanged();
                    } else {
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        binding.listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), SupplierMapActivity.class);
                intent.putExtra(REQUEST_OBJECT, userRequests.get(i));
                startActivity(intent);
            }

        });
        binding.swipRefresh.setRefreshing(false);

    }
}
