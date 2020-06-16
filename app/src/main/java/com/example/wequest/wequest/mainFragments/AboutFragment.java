package com.example.wequest.wequest.mainFragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.IntroductionActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_about, container, false);
        Button openIntro = view.findViewById(R.id.open_intro);
        openIntro.setOnClickListener(clickedView -> {
            Intent intent = new Intent(getContext(), IntroductionActivity.class);
            intent.putExtra(IntroductionActivity.IS_FROM_ABOUT,true );
            startActivity(intent);

        });
        return view;
    }

}
