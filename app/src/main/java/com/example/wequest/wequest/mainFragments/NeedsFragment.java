package com.example.wequest.wequest.mainFragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.HumanNeedListAdapter;
import com.example.wequest.wequest.adapters.HumanNeedSubCategoryAdapter;
import com.example.wequest.wequest.basicActivities.MainActivity;
import com.example.wequest.wequest.basicActivities.NeedRequestActivity;
import com.example.wequest.wequest.interfaces.CustomItemClickListener;
import com.example.wequest.wequest.interfaces.MyOnBackPressedListener;
import com.example.wequest.wequest.models.HumanNeed;
import com.example.wequest.wequest.models.SubHumanNeed;
import com.example.wequest.wequest.utils.InternetAvailabilityChecker;
import com.example.wequest.wequest.utils.NeedRequestUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_HUMAN_NEED_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.HUMAN_NEED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.HUMAN_SUB_CAT_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_NAME;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_REQUESTS_NUMBER;

/**
 * A simple {@link Fragment} subclass.
 */
public class NeedsFragment extends Fragment implements MyOnBackPressedListener {

    private int currentStatus;
    private String humanNeedListRef = "wequest/humanneeds";
    private String needKey;
    private View view;
    private RelativeLayout noConnection;

    private ProgressBar progressBar;
    private FloatingActionButton backActionButton;
    private String prevTitle;

    private ArrayList<HumanNeed> humanNeeds; // the humanNeeds Objects, fetched from FB
    private LayoutAnimationController controller;
    private RecyclerView recyclerView;
    private int selectedHumanNeedPosition;
    private OnHumanSubneedListListener secondListOpened;
    private String HUMAN_NEEDS_LIST = "HUMAN_NEEDS_LIST";
    private String CURRENT_SELECTED_HUMAN_NEED_POS = "CURRENT_SELECTED_HUMAN_NEED_POS";
    private String CURRENT_STATUS_KEY = "CURRENT_STATUS";
    private String CURRENT_NEED_KEY = "CURRENT_NEED_KEY";
    private String SCROLL_POSITION = "SCROLL_POSITION";

    public NeedsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            needKey = savedInstanceState.getString(CURRENT_NEED_KEY);

            humanNeeds = savedInstanceState.getParcelableArrayList(HUMAN_NEEDS_LIST);
            selectedHumanNeedPosition = savedInstanceState.getInt(CURRENT_SELECTED_HUMAN_NEED_POS, HUMAN_NEED_STATUS);
            currentStatus = savedInstanceState.getInt(CURRENT_STATUS_KEY);
            ((MainActivity) getContext()).getSupportActionBar().setTitle(humanNeeds.get(selectedHumanNeedPosition).getName());
            if (currentStatus == HUMAN_NEED_STATUS)
                setupHumanNeedsList(humanNeeds);
            else {
                setupHumanSubCatList();
            }
        } else {
            fetchNeedsList(FirebaseDatabase.getInstance().getReference(humanNeedListRef));
            currentStatus = HUMAN_NEED_STATUS;
        }

        if (currentStatus == HUMAN_NEED_STATUS) {
            backActionButton.setEnabled(false);
            backActionButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackClicked() {
        goToMainHumanList();
    }


    public interface OnHumanSubneedListListener {
        void isOnSubNeedList(boolean isOnList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_needs, container, false);
        viewBindings();

        validateInternetConnectivityForViews();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(humanNeedListRef);


        backActionButton.setOnClickListener(view -> goToMainHumanList());

        Log.e("onCreateView", "currentStatus: " + currentStatus);

        return view;
    }

    private void goToMainHumanList() {
        secondListOpened.isOnSubNeedList(false);

        if (((MainActivity) getContext()).getSupportActionBar() != null)
            ((MainActivity) getContext()).getSupportActionBar().setTitle(R.string.needs_fragment_title);
        else
            return;

        if (currentStatus == HUMAN_SUB_CAT_STATUS) {
            currentStatus--;
            needKey = needKey.substring(0, needKey.length() - 1);
            setupHumanNeedsList(humanNeeds);
        }

        if (currentStatus == HUMAN_NEED_STATUS) {
            backActionButton.setEnabled(false);
            backActionButton.setVisibility(View.GONE);
            humanNeedListRef = FIREBASE_HUMAN_NEED_PATH;
            needKey = "";
        }

    }


    private void validateInternetConnectivityForViews() {

        if (getContext() != null) {
            if (InternetAvailabilityChecker.hasInternetConnection(getContext())) {
                progressBar.setVisibility(View.GONE);
                noConnection.setVisibility(View.GONE);
            } else
                noConnection.setVisibility(View.GONE);
        }
    }


    private void viewBindings() {

        secondListOpened = (MainActivity) getContext();
        needKey = "";

        controller = AnimationUtils.loadLayoutAnimation(
                getActivity(), R.anim.list_animation_controller);
        recyclerView = view.findViewById(R.id.recyler_view_requests);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressBar = view.findViewById(R.id.progress_bar);
        noConnection = view.findViewById(R.id.loading_data);
        backActionButton = view.findViewById(R.id.prevList);

    }


    private void fetchNeedsList(DatabaseReference ref) {
        //Get data snapshot at your "users" root node
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot

                        humanNeeds = new ArrayList<>();


                        for (DataSnapshot needShot : dataSnapshot.getChildren()) {
                            ArrayList<SubHumanNeed> subHumanNeeds = new ArrayList<>();

                            for (long i = 0; i < needShot.getChildrenCount() - 1; i++) {
                                SubHumanNeed currentSubHumanNeed = new SubHumanNeed();
                                currentSubHumanNeed.setId((int) i);

                                currentSubHumanNeed.setName(needShot.child(String.valueOf(i)).getValue(String.class));
                                subHumanNeeds.add(currentSubHumanNeed);
                            }
                            humanNeeds.add(new HumanNeed(needShot.child("name").getValue(String.class), subHumanNeeds));
                        }

                        progressBar.setVisibility(View.GONE);
                        noConnection.setVisibility(View.GONE);

                        setupHumanNeedsList(humanNeeds);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void setupHumanNeedsList(final ArrayList<HumanNeed> needs) {


        if (getContext() == null)
            return;

        HumanNeedListAdapter needListAdapter = new HumanNeedListAdapter(getContext(), needs, (v, position) -> {


            //disabling touch
            ((MainActivity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            progressBar.setVisibility(View.VISIBLE);
            backActionButton.setEnabled(true);
            backActionButton.setVisibility(View.VISIBLE);


            if (currentStatus == HUMAN_NEED_STATUS) {

                DatabaseReference needRequestsQn = FirebaseDatabase.getInstance().getReference(NEED_REQUESTS_NUMBER)
                        .child(String.valueOf(needKey + position));
                ((MainActivity) getContext()).getSupportActionBar().setTitle(humanNeeds.get(position).getName());


                needRequestsQn.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        currentStatus++;// tracking and updating the current status of the need list, weather we are on the Human needs or their category
                        needKey += position;// tracking the need ID
                        selectedHumanNeedPosition = position;

                        if (dataSnapshot.exists()) {

                            ArrayList<SubHumanNeed> subNeeds = humanNeeds.get(position).getNeeds();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                subNeeds.get(Integer.parseInt(snapshot.getKey())).
                                        setNumberOfRequests(snapshot.getValue(Integer.class));
                            }
                        }
                        setupHumanSubCatList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        recyclerView.setAdapter(needListAdapter);
        recyclerView.setLayoutAnimation(controller);

    }

    private void setupHumanSubCatList() {

        secondListOpened.isOnSubNeedList(true);

        final ArrayList<SubHumanNeed> subCatNeeds = humanNeeds.get(selectedHumanNeedPosition).getNeeds();


        progressBar.setVisibility(View.GONE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        final ArrayList<SubHumanNeed> newSubCatNeeds = new ArrayList<>(subCatNeeds);

        NeedRequestUtil.sortNeedsByRequest(newSubCatNeeds);

        if (getContext() == null)
            return;


        HumanNeedSubCategoryAdapter subCategoryAdapter = new HumanNeedSubCategoryAdapter(getContext(), newSubCatNeeds, selectedHumanNeedPosition, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                Intent intent = new Intent(getActivity(), NeedRequestActivity.class);
                intent.putExtra(NEED_KEY, needKey + newSubCatNeeds.get(position).getId());
                intent.putExtra(NEED_NAME, newSubCatNeeds.get(position).getName());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(subCategoryAdapter);
        recyclerView.setLayoutAnimation(controller);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENT_STATUS_KEY, currentStatus);
        outState.putString(CURRENT_NEED_KEY, needKey);
        outState.putParcelableArrayList(HUMAN_NEEDS_LIST, humanNeeds);
        outState.putInt(CURRENT_SELECTED_HUMAN_NEED_POS, selectedHumanNeedPosition);
        Log.e("CURRENT", String.valueOf(currentStatus));
        super.onSaveInstanceState(outState);
    }


}