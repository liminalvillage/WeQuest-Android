package com.example.wequest.wequest.basicActivities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ActivitySupplierDetailsBinding;
import com.example.wequest.wequest.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_FEEDBACK_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_PROVIDER;

public class SupplierDetailsActivity extends AppCompatActivity {


    private ArrayList<String> feedbacks;

    private ActivitySupplierDetailsBinding binding;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_supplier_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        User provider = getIntent().getParcelableExtra(USER_PROVIDER);

        binding.userProfileLayout.reputation.setStepSize(0.1f);


        FirebaseDatabase.getInstance().getReference(FIREBASE_USER_FEEDBACK_PATH).child(provider.getUid()).child("feedbacks")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        feedbacks = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                feedbacks.add(snapshot.getValue(String.class));
                            }
                        }
                        ArrayAdapter<String> feedBackAdapter = new ArrayAdapter<>(SupplierDetailsActivity.this, android.R.layout.simple_list_item_1, feedbacks);
                        binding.feedbackListview.setAdapter(feedBackAdapter);
                        binding.feedbackListview.setEmptyView(findViewById(R.id.no_feedback));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        Picasso.get().load(provider.getPhotoUrl()).into(binding.userProfileLayout.cirleImage);
        binding.userProfileLayout.userName.setText(provider.getUsername());
        binding.userProfileLayout.timeCredit.setText(String.valueOf(provider.getTimeCredit()));
        binding.userProfileLayout.karma.setText(String.valueOf(provider.getKarma()));
        binding.userProfileLayout.reputation.setRating((float) provider.getRating());

    }


}

