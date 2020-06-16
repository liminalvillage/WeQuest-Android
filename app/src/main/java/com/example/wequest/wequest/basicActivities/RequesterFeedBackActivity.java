package com.example.wequest.wequest.basicActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ActivityRequesterFeedBackBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_FEEDBACK_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.SUPPLIER_NAME;

public class RequesterFeedBackActivity extends AppCompatActivity {


    private ActivityRequesterFeedBackBinding binding;
    private LoadToast transactionToast;
    public static int CURRENT_FEEDBACK_STEP = 0;
    public final static int FINAL_FEEDBACK_STEP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_requester_feed_back);
        Log.e(getLocalClassName(), "FEEDBACk");

        transactionToast = new LoadToast(this);
        transactionToast.setText("transacting");
        transactionToast.setTextColor(android.R.color.holo_green_dark);
        transactionToast.setBorderWidthDp(5);
        transactionToast.setTranslationY(100);
        transactionToast.setProgressColor(R.color.color_success);
        transactionToast.show();


    }


    public void feedbacking(View view) {


        final String chosenProvider = getIntent().getStringExtra(SUPPLIER_NAME);

        if (binding.rating.getRating() < 1) {
//            transactionToast.error();
            MDToast.makeText(this, "Please set a Rating!", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
            return;
        }

        // giving feedback

        final DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_FEEDBACK_PATH).
                child(chosenProvider).child("feedbacks");
        feedbackRef.push().setValue(binding.feedback.getText().toString(), (err1, err2) -> {
            updateProgressFinish();
        });


        // rating
        final DatabaseReference ratingRef = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_PROFILE_PATH).child(chosenProvider).child("ratings");
        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Integer> ratings = (ArrayList<Integer>) dataSnapshot.getValue();
                int startNum = (int) binding.rating.getRating();
                int personRatedNum = Integer.parseInt(String.valueOf(ratings.get(startNum - 1)));
                ratings.set(startNum - 1, personRatedNum + 1);

                double totalRators = 0;
                double individualStarRatings = 0;
                for (int i = 0; i < ratings.size(); i++) {
                    individualStarRatings += Integer.parseInt(String.valueOf(ratings.get(i))) * (i + 1);
                    totalRators += Integer.parseInt(String.valueOf(ratings.get(i)));
                }


                double userRating = individualStarRatings / totalRators;
                final DatabaseReference supplierTotalRating = FirebaseDatabase.getInstance().
                        getReference(FIREBASE_USER_PROFILE_PATH).child(chosenProvider).child("rating");

                supplierTotalRating.setValue(userRating);

                updateProgressFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void updateProgressFinish() {

        CURRENT_FEEDBACK_STEP++;

        if (CURRENT_FEEDBACK_STEP == FINAL_FEEDBACK_STEP) {
            finish();
            transactionToast.success();
        }
    }


}
