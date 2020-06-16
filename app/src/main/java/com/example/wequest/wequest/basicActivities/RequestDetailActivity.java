package com.example.wequest.wequest.basicActivities;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ActivityRequestDetailBinding;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.notifications.NotificationSender;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FINISH_ACTIVITY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;

public class RequestDetailActivity extends AppCompatActivity {


    private String uid;
    private String needKey;
    private Request userRequest;
    private int currentRequestKarma;
    private ActivityRequestDetailBinding binding;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener view = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //todo there wont be notif going to the requester itself

            if (userRequest.getUid().equals(FireBaseHelper.getUid())) {
                MDToast.makeText(RequestDetailActivity.this, "You cannot accept your own request", MDToast.TYPE_INFO, MDToast.LENGTH_LONG).show();
                return;
            }
            switch (view.getId()) {
                case R.id.no:
                    new AlertDialog.Builder(RequestDetailActivity.this).
                            setMessage("Are you sure to reject this Request")
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, which) -> {
                                removeRequestFromUserNotification();
                                finish();
                            }).show();
                    break;
                case R.id.yes:

                    new AlertDialog.Builder(RequestDetailActivity.this)
                            .setMessage("Are You Confirm Accepting This Request")
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, which) -> {

                                // todo , when should we fix the needed karma
                                // setting the current needed karma for thr request, so it will be transferred during the transaction time
                                DatabaseReference reqPath = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH).child(userRequest.getUid())
                                        .child(userRequest.getNeedKey()).child("karma");
                                reqPath.setValue(currentRequestKarma);


                                // setting this user_profile as a supplier for the requester'need
                                final DatabaseReference requestPath = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH)
                                        .child(uid).child(needKey).child("providers");


                                NotificationSender.notifyNewSupplier(userRequest.getUid(), userRequest.getNeedKey(), userRequest.getNeedTitle());

                                //adding the new Supplier
                                final LoadToast loadToast = new LoadToast(RequestDetailActivity.this);
                                loadToast.setText("Accepting");
                                loadToast.show();

                                requestPath.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ArrayList<String> providers;
                                        // todo if you have time fix this one,
                                        // so that, we just to it liek this .child(uid).setValue(true);
                                        if (dataSnapshot.exists()) {
                                            providers = (ArrayList<String>) dataSnapshot.getValue();
                                            if (!providers.contains(FireBaseHelper.getUid()))
                                                providers.add(FireBaseHelper.getUid());
                                        } else {
                                            providers = new ArrayList<>();
                                            providers.add(FireBaseHelper.getUid());
                                        }

                                        requestPath.setValue(providers);
                                        loadToast.success();
                                        removeRequestFromUserNotification();
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }).show();

                    break;
            }


            finishActivity(FINISH_ACTIVITY);

        }

    };

    private void checkingIfRequestIsChosenBefore() {

        String uid = FirebaseAuth.getInstance().getUid();

        if (userRequest.getProviders() != null) {
            if (userRequest.getProviders().contains(uid)) {
                Log.e("CONTAINS", "YES");
                binding.acceptThis.setText("Reject This Request?");
                binding.yes.setVisibility(View.GONE);
                binding.no.setVisibility(View.VISIBLE);
            }

            return;
        }


        binding.acceptThis.setText("Can you provide for this request?");
        binding.yes.setVisibility(View.VISIBLE);
        binding.no.setVisibility(View.GONE);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_request_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userRequest = getIntent().getParcelableExtra(REQUEST_OBJECT);


        binding.yes.setOnClickListener(view);
        binding.no.setOnClickListener(view);


        binding.requestMessage.setText(userRequest.getRequestInformation());
        binding.requestTitle.setText(userRequest.getNeedTitle());

        binding.requestTime.setText(getString(R.string.hour_detail, String.valueOf(userRequest.getHour())));
        needKey = userRequest.getNeedKey();
        uid = userRequest.getUid();

//        WeQuestOperation needKarma = new WeQuestOperation(needKey);
//        needKarma.setOnNeedKarmaListener(KARM +A_REQUEST_DETAILS, new OnNeedKarma() {
//            @Override
//            public void onNeedKarmaFetched(int karma) {
//
//
//                currentRequestKarma = karma;
//                binding.requestKarma.setText("Karma : " + currentRequestKarma);
//            }
//        });

        binding.requestKarma.setText("Karma : " + userRequest.getKarma());

        checkingIfRequestIsChosenBefore();

        WeQuestOperation operation = new WeQuestOperation(uid);
        operation.setOnUserFetchedListener(user -> {
            Picasso.get().load(user.getPhotoUrl()).into(binding.userPic);
            binding.requesterName.setText(user.getUsername());
            binding.reputation.setRating((float) user.getRating());
        });
    }

    private void removeRequestFromUserNotification() {
        DatabaseReference requestNotificationPathForMe = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH).
                child(FireBaseHelper.getUid() + "/requeststome").child(uid).child(needKey);
        requestNotificationPathForMe.removeValue();
    }

}
