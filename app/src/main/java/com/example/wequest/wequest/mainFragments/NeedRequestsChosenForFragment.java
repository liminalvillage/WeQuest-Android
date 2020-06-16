package com.example.wequest.wequest.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestChosenForAdapter;
import com.example.wequest.wequest.databinding.FragmentNeedRequestsChosenForBinding;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NeedRequestsChosenForFragment extends Fragment {
    public NeedRequestsChosenForFragment() {
        // Required empty public constructor
    }

    private FragmentNeedRequestsChosenForBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_need_requests_chosen_for, container, false);

        binding.requestImChosenFor.listViewRequests.setEmptyView(binding.noRequestView);

        buildRequestsChosenForList();
        binding.requestImChosenFor.swipRefresh.setColorSchemeResources(R.color.refresh1, R.color.refresh2, R.color.refresh3);

        binding.requestImChosenFor.swipRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.noRequestView.setVisibility(View.GONE);
                binding.requestImChosenFor.listViewRequests.setVisibility(View.GONE);
                buildRequestsChosenForList();
            }
        });

        return binding.getRoot();
    }
    private void buildRequestsChosenForList() {
        binding.requestImChosenFor.swipRefresh.setRefreshing(true);




        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference requestsImChosenForRef = FireBaseReferenceUtils.getRequestsChosenForRef(uid);


        requestsImChosenForRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot grandShot) {
                if (getContext() == null)
                    return;

                ArrayList<Request> requests = new ArrayList<>();
                for (DataSnapshot parentShot : grandShot.getChildren()) {

                    for (DataSnapshot childShot : parentShot.getChildren()) {

                        requests.add(childShot.getValue(Request.class));
                    }
                }

                RequestChosenForAdapter adapter = new RequestChosenForAdapter(getContext(),
                        R.layout.request_chosenfor_list_item, requests);

                binding.requestImChosenFor.listViewRequests.setAdapter(adapter);
                binding.requestImChosenFor.listViewRequests.setVisibility(View.VISIBLE);
                binding.requestImChosenFor.swipRefresh.setRefreshing(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
