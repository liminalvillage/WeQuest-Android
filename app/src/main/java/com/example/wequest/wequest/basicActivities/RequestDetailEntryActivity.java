package com.example.wequest.wequest.basicActivities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_REQUEST_TO_ME_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_NAME;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NOT_STARTED_STATUS;


public class RequestDetailEntryActivity extends AppCompatActivity implements VerticalStepperForm {

    private static final String KARMA = "karma";
    private static final String REQUEST_TITLE = "REQUEST_TITLE";
    private static final String REQUEST_DESC = "REQUEST_DESC";
    private static final String REQUEST_EXP = "REQUEST_EXP";
    private static final String HOUR_TIME = "HOUR_TIME";

    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 25;
    private static final int MIN_DESCRIPTION_LENGTH = 5;
    private static final int MAX_DESCRIPTION_LENGTH = 150;
    private static final String DUE_DATE = "dueDate";

    private Button pickExp;
    private double karmaForRequest;
    private EditText titleEd, descEd, hourEd, karmaEd;
    private LatLng userLocation;
    private LoadToast transactionToast;
    private double userTimeCredit;
    private User user;
    private TextView expDate;
    private boolean isNeededKarmaInitialized;

    private double dueDate;
    private String needKey;
    private String needName;

    private VerticalStepperFormLayout stepper;
    private Bundle globalSavedInstanceState;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail_entery);
        needKey = getIntent().getStringExtra(NEED_KEY);
        needName = getIntent().getStringExtra(NEED_NAME);

        initializeRequestKarma(needKey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null)
            dueDate = savedInstanceState.getInt(DUE_DATE, 0);


        this.globalSavedInstanceState = savedInstanceState;


        //
        createAndSInitializeStepper();


        WeQuestOperation operation = new WeQuestOperation(FireBaseHelper.getUid());
        operation.setOnUserFetchedListener(user -> {
            RequestDetailEntryActivity.this.user = user;
            userTimeCredit = user.getTimeCredit();
        });

        userLocation = getIntent().getParcelableExtra("location");


    }

    private void createAndSInitializeStepper() {
        stepper = findViewById(R.id.vertical_stepper_form);
        String[] mySteps = {getString(R.string.title), getString(R.string.describtion), getString(R.string.hours_need), getString(R.string.karmas_giving), getString(R.string.opt_exp)};
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);


        // Setting up and initializing the form

        VerticalStepperFormLayout.Builder.newInstance(stepper,
                mySteps,
                this, this)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .displayBottomNavigation(true).init();

    }

    private void makeRequest() {


        transactionToast = new LoadToast(this);
        transactionToast.setText("Requesting, Please wait a few seconds ...");
        transactionToast.setTextColor(android.R.color.holo_green_dark);
        transactionToast.setBorderWidthDp(5);
        transactionToast.setTranslationY(300);
        transactionToast.setProgressColor(R.color.color_success);
        transactionToast.show();

        if (!isNeededKarmaInitialized) {
            MDToast.makeText(this, "There is a problem,try again later", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
            return;
        }

        if (Integer.parseInt(karmaEd.getText().toString()) > user.getKarma() || karmaForRequest > user.getKarma()) {
            MDToast.makeText(this, "Not enough karma points,\n" +
                    "please satisfy other's needs first", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
            transactionToast.hide();
            return;
        }

        // miran you should calculate this time credit according to the time the user_profile sets
        //like 1 hour, then 10 credits,

        final double timeCreditUserHasToPay;
        if (TextUtils.isEmpty(hourEd.getText().toString())) {
            timeCreditUserHasToPay = 0;

        } else
            timeCreditUserHasToPay = Double.parseDouble(hourEd.getText().toString());

        if (userTimeCredit < timeCreditUserHasToPay) {


            MDToast.makeText(this, "Not enough hours in your wallet,\nplease satisfy other's needs first", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
            transactionToast.hide();
            return;
        }


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        needKey = getIntent().getStringExtra(NEED_KEY);


        final DatabaseReference userRef = FirebaseDatabase.getInstance().
                getReference(FIREBASE_USER_REQUESTS_PATH).child(FireBaseHelper.getUid()).child(needKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    final Request myRequest = new Request();
                    myRequest.setUid(user.getUid());
                    myRequest.setNeedKey(needKey);
                    myRequest.setNeedCategoryName(needName);
                    myRequest.setNeedTitle(titleEd.getText().toString());
                    myRequest.setHour(timeCreditUserHasToPay);
                    myRequest.setKarma(Integer.parseInt(karmaEd.getText().toString()));
                    myRequest.setDueDate(dueDate == 0 ? 0 : dueDate + 3600000);
                    myRequest.setRequestInformation(descEd.getText().toString());
                    myRequest.setStatus(NOT_STARTED_STATUS);
                    myRequest.setLatitude(userLocation.latitude);
                    myRequest.setLongitude(userLocation.longitude);
                    userRef.setValue(myRequest, (DatabaseError databaseError, DatabaseReference databaseReference) -> {

//                     increase the karma by one at the last step of adding the request
                        WeQuestOperation.updateDataCountNumber(FireBaseReferenceUtils.getNeedKarmaRef(needKey), WeQuestOperation.DATA_VALUE_INCREMENT);


                        /// a referesh to the subscriebrs so that they can see the requester on the map later on
                        DatabaseReference requestsToSupplier = FirebaseDatabase.getInstance().getReference(FIREBASE_REQUEST_TO_ME_PATH).child(String.valueOf(needKey));

                        HashMap<String, Double> latLong = new HashMap<>();
                        latLong.put("LAT", myRequest.getLatitude());
                        latLong.put("LONG", myRequest.getLongitude());
                        latLong.put("STATUS", (double) NOT_STARTED_STATUS);

                        requestsToSupplier.child(user.getUid()).setValue(latLong);


                        /// increqasing the the number of the need request
                        DatabaseReference needRequestNumberRef = FirebaseDatabase.getInstance().
                                getReference(WeQuestConstants.NEED_REQUESTS_NUMBER)
                                .child(String.valueOf(needKey.charAt(0))).child(String.valueOf(needKey.charAt(1)));

                        WeQuestOperation.updateDataCountNumber(needRequestNumberRef, WeQuestOperation.DATA_VALUE_INCREMENT);

                        // sending the notifications

                        AsyncTask.execute(() -> {
                            Log.e("Notification Thread:", "started");
                            WeQuestOperation operation = new WeQuestOperation(RequestDetailEntryActivity.this, needKey);
                            operation.SendNotficationToSubs(transactionToast, new LatLng(myRequest.getLatitude(), myRequest.getLongitude()));
                        });
                    });

                } else {
                    MDToast.makeText(RequestDetailEntryActivity.this, "You already have an active request in this category before!\nDelete your request before requesting again", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
                    transactionToast.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeRequestKarma(String needKey) {


        FireBaseReferenceUtils.getNeedKarmaRef(needKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            karmaForRequest = dataSnapshot.getValue(Integer.class);

                        isNeededKarmaInitialized = true;

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public View createStepContentView(int stepNumber) {

        String requestTitle = null;
        String requestDesc = null;
        String hour = null;
        String karma = null;
        String expText = null;

        if (globalSavedInstanceState != null) {

            requestTitle = globalSavedInstanceState.getString(REQUEST_TITLE);

            requestDesc = globalSavedInstanceState.getString(REQUEST_DESC);

            hour = globalSavedInstanceState.getString(HOUR_TIME);

            karma = globalSavedInstanceState.getString(KARMA);
            expText = globalSavedInstanceState.getString(REQUEST_EXP);
        }


        View view = null;
        switch (stepNumber) {
            case 0:
                titleEd = new EditText(this);

                titleEd.setHint("The Title for your Request");

                if (requestTitle != null)
                    titleEd.setText(requestTitle);

                titleEd.addTextChangedListener(new MyTextWatcher(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH));
                view = titleEd;
                break;
            case 1:

                descEd = new EditText(this);
                descEd.setHint("Add some details to your request");

                if (requestDesc != null)
                    descEd.setText(requestDesc);


                descEd.addTextChangedListener(new MyTextWatcher(MIN_DESCRIPTION_LENGTH, MAX_DESCRIPTION_LENGTH));
                view = descEd;
                break;
            case 2:
                hourEd = new EditText(this);
                hourEd.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                hourEd.setHint(R.string.hour_hint);

                if (hour != null)
                    hourEd.setText(hour);

                view = hourEd;
                break;
            case 3:
                karmaEd = new EditText(this);
                karmaEd.setInputType(InputType.TYPE_CLASS_NUMBER);
                karmaEd.setHint("the amount of karma you give");

                if (karma != null)
                    karmaEd.setText(karma);


                karmaEd.addTextChangedListener(new MyTextWatcher());
                view = karmaEd;
                break;
            case 4:

                LinearLayout expLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.name_stepper_view, null, false);
                expDate = expLayout.findViewById(R.id.exp_tv);
                pickExp = expLayout.findViewById(R.id.time_img);
                if (expText != null)
                    expDate.setText(expText);

                setPickExpListener();


                view = expLayout;

                break;

        }


        return view;
    }

    private void setPickExpListener() {
        pickExp.setOnClickListener(v -> {
            final int mYear, mMonth, mDay;

            // Get Current Date

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
//
//
            DatePickerDialog datePickerDialog = new DatePickerDialog(RequestDetailEntryActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);

                        expDate.setText(String.valueOf(dayOfMonth + "/" + monthOfYear + "/" + year));
                        dueDate = calendar.getTimeInMillis();
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        });
    }

    @Override
    public void onStepOpening(int sNumber) {


        int currentLenght = 0;
        switch (sNumber) {
            case 0:
                if (!TextUtils.isEmpty(titleEd.getText().toString()))
                    currentLenght = titleEd.getText().toString().length();
                checkLength(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH, currentLenght);
                break;
            case 1:
                if (!TextUtils.isEmpty(descEd.getText().toString()))
                    currentLenght = descEd.getText().toString().length();

                checkLength(MIN_DESCRIPTION_LENGTH, MAX_DESCRIPTION_LENGTH, currentLenght);
                break;
            case 2:
                stepper.setStepAsCompleted(2);
                break;
            case 3:
                stepper.setActiveStepAsUncompleted("at least 1 karma");
                break;
            case 4:
                stepper.setStepAsCompleted(4);
                break;
            case 5:
                setRequestSummary();
                break;

        }

    }

    private void setRequestSummary() {
        TextView view = findViewById(R.id.request_summary);
        view.setGravity(Gravity.CENTER);
        String reqTitle = titleEd.getText().toString();
        String reqKarma = karmaEd.getText().toString();
        String reqHour = hourEd.getText().toString();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Long currentMiliSecondTime = preferences.getLong("TIME", 0);


        String currentRealTime = currentMiliSecondTime == 0 ? getString(R.string.unknown_time) : format.format(new Date(currentMiliSecondTime));


        view.setText(getString(R.string.request_summary, reqTitle, reqHour.length() == 0 ? 0 : reqHour, reqKarma, currentRealTime));
        findViewById(R.id.summary_card).setVisibility(View.VISIBLE);
    }

    @Override
    public void sendData() {
        makeRequest();
    }

    private class MyTextWatcher implements TextWatcher {


        private int minLength;
        private int maxLength;

        MyTextWatcher(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        public MyTextWatcher() {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (minLength != 0 || maxLength != 0)
                checkLength(minLength, maxLength, s.length());

            else {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.parseInt(s.toString()) > 0)
                        stepper.setActiveStepAsCompleted();
                } else
                    stepper.setActiveStepAsUncompleted("give at least 1 karma");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }


    }

    void checkLength(int minLength, int maxLength, int length) {
        if (length >= minLength && length < maxLength) {
            stepper.setActiveStepAsCompleted();
        } else {
            stepper.setActiveStepAsUncompleted("Should be between " + minLength + " and " + maxLength + " characters");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(REQUEST_TITLE, titleEd.getText().toString());
        savedInstanceState.putString(REQUEST_DESC, descEd.getText().toString());
        savedInstanceState.putString(KARMA, karmaEd.getText().toString().isEmpty() ? "" : hourEd.getText().toString());
        savedInstanceState.putString(HOUR_TIME, hourEd.getText().toString().isEmpty() ? "" : hourEd.getText().toString());
        savedInstanceState.putString(REQUEST_EXP, expDate.getText().toString());
        savedInstanceState.putDouble(DUE_DATE, dueDate);

        super.onSaveInstanceState(savedInstanceState);
    }

}
