package com.example.wequest.wequest.basicActivities;

import android.content.Intent;
 import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ActivityFinishTransactionBinding;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import net.steamcrafted.loadtoast.LoadToast;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FINISHED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;

public class FinishTransactionActivity extends AppCompatActivity {


    private Request request;
    private static final int LAST_STEP_TRANSACTION = 4;
    int CURRENT_TRANSACTION_STEP;
    AppCompatRatingBar ratingBar;
    LoadToast transactionToast;
    TextView suppliersName;
    private EditText feedbackEd;
    private ActivityFinishTransactionBinding binding;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_finish_transaction);
        feedbackEd  = findViewById(R.id.feedback);

//        suppliersName = findViewById(R.id.supplier_name); //MOHAMMED
        // TODO: this did not allow compilation. Please fix and uncomment
        ratingBar = findViewById(R.id.rating);
        request = getIntent().getParcelableExtra(REQUEST_OBJECT);

        int karma = request.getKarma();
        double hour = request.getHour();

        TextView karmaTV = findViewById(R.id.karma);
        TextView hourTV = findViewById(R.id.time_credit);

        karmaTV.setText(getString(R.string.karma_transfered, karma));
        hourTV.setText(getString(R.string.hour_transfered, hour));


        final LoadToast loadToast = new LoadToast(this);
        loadToast.setText("Waiting For Supplier to Scan the QrCode");
        loadToast.setTranslationY(32);
        loadToast.setProgressColor(ContextCompat.getColor(this,R.color.primary ));
        loadToast.show();

        generateQrCode(request.getChosenProvider());

        DatabaseReference qrCodeRef = FireBaseReferenceUtils.getQrCodeTransactionRef(request.getChosenProvider());
        qrCodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue(Integer.class) == 0) {
                        loadToast.success();
                        dataSnapshot.getRef().removeValue();
                        finishTransaction();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void generateQrCode(String textQRcode){

        MultiFormatWriter formatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix =formatWriter.encode(textQRcode, BarcodeFormat.QR_CODE,200 ,200 ) ;

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            Bitmap qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix);

            binding.qrCode.setImageBitmap(qrCodeBitmap);
            //finishTransaction();



        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void finishTransaction() {

        transactionToast = new LoadToast(this);
        transactionToast.setText("transacting");
        transactionToast.setTextColor(android.R.color.holo_green_dark);
        transactionToast.setBorderWidthDp(5);
        transactionToast.setTranslationY(100);
        transactionToast.setProgressColor(R.color.color_success);
        transactionToast.show();


        final String chosenProvider = request.getChosenProvider();

        //1- adding the timecredits to the supplier
        final DatabaseReference timeCreditsToSupplier = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_PROFILE_PATH + chosenProvider).child("timeCredit");
        timeCreditsToSupplier.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double providerTimeCredit = dataSnapshot.getValue(Double.class);
                providerTimeCredit = providerTimeCredit + request.getHour();

               // double totalMin = hourToCommit * 60;
              //  double newTimeCredit = totalMin / 6;
                //providerTimeCredit = newTimeCredit + providerTimeCredit;
                Log.e(getLocalClassName(),"Hours:"+providerTimeCredit);

                timeCreditsToSupplier.setValue(providerTimeCredit);
                checkTransactionStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // 2- taken timeCredit from the requester

        final DatabaseReference timeCreditsTakenFromRequester = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_PROFILE_PATH + FireBaseHelper.getUid()).child("timeCredit");
        timeCreditsTakenFromRequester.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double requesterTimeCredit = dataSnapshot.getValue(Double.class);
                double hourToCommit = request.getHour();

                //double totalMin = hourToCommit * 60;
                requesterTimeCredit = requesterTimeCredit - hourToCommit;
                timeCreditsTakenFromRequester.setValue(requesterTimeCredit);
                checkTransactionStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /// 3- adding the karma points
        final DatabaseReference karmaToProviderRef = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_PROFILE_PATH + chosenProvider).child("karma");
        karmaToProviderRef.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int providerKarma = dataSnapshot.getValue(Integer.class);
                int karmaToBeAdded = request.getKarma();
                providerKarma = providerKarma + karmaToBeAdded;
                karmaToProviderRef.setValue(providerKarma);
                checkTransactionStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // 4- karma to be taken from the requester
        final DatabaseReference requesterKarmaRef = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_PROFILE_PATH).child(FireBaseHelper.getUid()).child("karma");

        requesterKarmaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int requesterKarma = dataSnapshot.getValue(Integer.class);

                int karmaToBeTaken = request.getKarma();
                requesterKarma = requesterKarma - karmaToBeTaken;

                requesterKarmaRef.setValue(requesterKarma);

                checkTransactionStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void checkTransactionStatus() {
        CURRENT_TRANSACTION_STEP++;
        if (CURRENT_TRANSACTION_STEP == LAST_STEP_TRANSACTION) {
            transactionToast.success();

            //updating the request status
            request.setStatus(FINISHED_STATUS);
            WeQuestOperation.updateRequestStatus(request.getUid() + "/" + request.getNeedKey(), FINISHED_STATUS);
            //updating the needed karma for such reqeust


            WeQuestOperation.updateDataCountNumber(FireBaseReferenceUtils.getNeedKarmaRef(request.getNeedKey()),WeQuestOperation.DATA_VALUE_DECREMENT);

            WeQuestOperation.updateDataCountNumber(FirebaseDatabase.getInstance().
                    getReference(WeQuestConstants.NEED_REQUESTS_NUMBER)
                    .child(String.valueOf(request.getNeedKey().charAt(0))).
                            child(String.valueOf(request.getNeedKey().charAt(1))),WeQuestOperation.DATA_VALUE_DECREMENT );
            Intent intent = new Intent(this,RequesterFeedBackActivity.class);
            intent.putExtra(WeQuestConstants.SUPPLIER_NAME, request.getChosenProvider());
            startActivity(intent);
            finish();
        }
    }

}
