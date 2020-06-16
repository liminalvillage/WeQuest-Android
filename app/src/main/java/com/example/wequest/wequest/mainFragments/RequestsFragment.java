package com.example.wequest.wequest.mainFragments;


 import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

 import androidx.annotation.NonNull;
 import androidx.databinding.DataBindingUtil;
 import androidx.fragment.app.Fragment;
 import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

 import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.RequestListAdapter;
import com.example.wequest.wequest.adapters.RequestStatusFragmentAdapter;
import com.example.wequest.wequest.databinding.FragmentRequestStatusBinding;
import com.example.wequest.wequest.models.Request;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private ListView requestListView;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<Request> requestsList;
    private RequestListAdapter requestAdapter;

    public RequestsFragment() {
        // Required empty public constructor
    }

    FragmentRequestStatusBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_status, container, false);
        if (getActivity() == null)
            return binding.getRoot();

        RequestStatusFragmentAdapter adapter = new RequestStatusFragmentAdapter(getChildFragmentManager());

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        if(savedInstanceState != null)
            binding.viewPager.setCurrentItem(savedInstanceState.getInt("POS", 0));


        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if(binding !=null)
        outState.putInt("POS",binding.viewPager.getCurrentItem() );
        super.onSaveInstanceState(outState);

    }
}
