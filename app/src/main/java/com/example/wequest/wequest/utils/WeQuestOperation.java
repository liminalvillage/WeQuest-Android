package com.example.wequest.wequest.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.wequest.wequest.basicActivities.MainActivity;
import com.example.wequest.wequest.interfaces.OnRequestListener;
import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.notifications.NotificationOpenHandler;
import com.example.wequest.wequest.notifications.NotificationSender;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_REQUEST_TO_ME_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.GOTO_REQUEST_FRAGMENT;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NOT_STARTED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_FRAGMENT_INDEX;

/**
 * Created by mohammed on 30/12/17.
 */

public class WeQuestOperation {
    public static final int DATA_VALUE_INCREMENT = 1;
    public static final int DATA_VALUE_DECREMENT = 2;
    public final static int ALL_REQUEST_NEEDS = 3;
    public final static int ONE_REQUEST_NEEDS = 4;

    private int numberOfRequestsFetched;
    private Activity context;
    private String needKey;
    private OnUserListener userListener;
    private String firebasePath;

    public WeQuestOperation(Activity context, String subscribeKey) {
        this.context = context;
        this.needKey = subscribeKey;
    }

    public WeQuestOperation() {
        this.numberOfRequestsFetched = 0;

    }

    public static void updateDataCountNumber(DatabaseReference dataRef, final int updateType) {
        dataRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                    return Transaction.success(mutableData);
                }


                int newDataValue = 0;
                int dataValue = mutableData.getValue(Integer.class);

                if (updateType == DATA_VALUE_INCREMENT)
                    newDataValue = dataValue + 1;
                else if (updateType == DATA_VALUE_DECREMENT)
                    newDataValue = dataValue - 1;

                // Set value and report transaction success
                mutableData.setValue(newDataValue);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(getClass().getSimpleName(), "postTransaction:onComplete:" + databaseError);
            }
        });
    }


    public WeQuestOperation(String firebasePath) {
        this.firebasePath = firebasePath;
    }


    public static void fetchNeedRequests(int requestFetchType, final String needKey, final OnRequestListener requestListener, final int currentSeekerDistanceValue, final LatLng supplierLL) {
        // here we retrieve all the requests for a specific needs, but first only the UID of that request so that i can then
        // use to to retrieve all the data of that request in the request path
        // then...........
        DatabaseReference requestToMePath = FirebaseDatabase.getInstance().getReference(FIREBASE_REQUEST_TO_ME_PATH).child(needKey);

        requestToMePath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> userIDs = new ArrayList<>();
                // this is where the the UID of the requests are received

                if (dataSnapshot.exists()) {


                    if (requestFetchType == ALL_REQUEST_NEEDS) {


                        for (DataSnapshot needSnapshots : dataSnapshot.getChildren())
                            itterateThroughtNeedRequests(needSnapshots, supplierLL, currentSeekerDistanceValue, userIDs, needKey);
                    } else {
                        itterateThroughtNeedRequests(dataSnapshot, supplierLL, currentSeekerDistanceValue, userIDs, needKey);
                    }

                    /// ......... then this is where i will use all the recieved UIDs are use it to retrieve the request data
                    // not that each time the FB path will be changed in the loop,

                    // in the method, put this snippet of the for loop, so we only creating one handler object not MANY inside the loop
                    if (userIDs.isEmpty())
                        requestListener.onRequestFetched(new ArrayList<>());
                    else {
                        WeQuestOperation operation = new WeQuestOperation();
                        operation.setOnRequestListener(userIDs, requestListener, needKey);

                    }

                } else
                    requestListener.onRequestFetched(new ArrayList<>());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private static void itterateThroughtNeedRequests(DataSnapshot needSnapshots, LatLng supplierLL, int currentSeekerDistanceValue, List<String> userIDs, String needKey) {

        for (DataSnapshot currentTestRequest : needSnapshots.getChildren()) {

            if (currentTestRequest.child("LAT").getValue(Double.class) == null || currentTestRequest.child("LONG").getValue(Double.class) == null)
                return;

            if (shouldAddRequest(currentTestRequest, supplierLL, currentSeekerDistanceValue))
                if (needKey.isEmpty())
                    userIDs.add(currentTestRequest.getKey() + "/" + needSnapshots.getKey());
                else
                    userIDs.add(currentTestRequest.getKey());


        }
    }

    private static boolean shouldAddRequest(DataSnapshot currentTestRequest, LatLng supplierLL, int currentSeekerDistanceValue) {
        double lattitude = currentTestRequest.child("LAT").getValue(Double.class);
        double logtitude = currentTestRequest.child("LONG").getValue(Double.class);
        int status = currentTestRequest.child("STATUS").getValue(Integer.class);
        LatLng requestLatlng = new LatLng(lattitude, logtitude);

        return status == NOT_STARTED_STATUS && isUserInRadiusRange(requestLatlng, supplierLL, currentSeekerDistanceValue);

    }

    private static boolean isUserInRadiusRange(LatLng user1, LatLng user2, int conditionalDistance) {
        double distance = SphericalUtil.computeDistanceBetween(user1, user2);

        return distance <= conditionalDistance;
    }

    private void finishAndGotoRequestFragment() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(GOTO_REQUEST_FRAGMENT, REQUEST_FRAGMENT_INDEX);
        context.startActivity(intent);
        context.finish();
    }

    public static void subscribe(Context context, final Location location, final String needKey) {

        String uid = FirebaseAuth.getInstance().getUid();


        final DatabaseReference subReference = FirebaseDatabase.getInstance().getReference("wequest").child("subscribers/" + needKey).child(uid);

        subReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm Subscription");
                    builder.setMessage("Are sure you would like to subscribe to this need?\n You will be notified when someone near you will request this category");

                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        Map<String, String> firstNeedSub = new HashMap<>();
                        firstNeedSub.put("lat", String.valueOf(location.getLatitude()));
                        firstNeedSub.put("long", String.valueOf(location.getLongitude()));
                        firstNeedSub.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        subReference.setValue(firstNeedSub);
                        MDToast.makeText(context, "Successfully Subscribed", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS);
                        dialog.dismiss();

                    }).setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cancel Subscription");
                    builder.setMessage("You have subscribed to this need,\nDo you want to cancel?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        dataSnapshot.getRef().removeValue();
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {
                        dialog.cancel();
                    });
                    builder.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void updateRequestStatus(String requestPath, int status) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH
                + requestPath).child("status");
        statusRef.setValue(status);


        DatabaseReference requestPublicRef = FireBaseReferenceUtils.getRequestPublicRef(requestPath);
        requestPublicRef.child("STATUS").setValue(status);

    }


    public static void decreaseUserKarma() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DatabaseReference karmaRef;
            karmaRef = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH);
            karmaRef = karmaRef.child(uid).child("karma");
            WeQuestOperation.updateDataCountNumber(karmaRef, DATA_VALUE_DECREMENT);
        }
    }


    public void setOnUserFetchedListener(OnUserListener userListener) {
        this.userListener = userListener;
        fetchUser();
    }

    private void fetchUser() {
        DatabaseReference userFetched = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH + firebasePath);
        userFetched.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userListener.onUserFetched(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void cancelSupplier(String requestPath) {
        // setting the status back to not_started for the user's request and the public request

        WeQuestOperation.updateRequestStatus(requestPath, NOT_STARTED_STATUS);

        DatabaseReference chosenProviderRef = FireBaseReferenceUtils.getRequestChosenProviderRef(requestPath);
        chosenProviderRef.removeValue();

//        DatabaseReference requestPublicRef = FireBaseReferenceUtils.getRequestPublicRef(requestPath);
//        requestPublicRef.child("STATUS").setValue(NOT_STARTED_STATUS);

    }

    public static void saveCurrentServerTime(Context context) {
        DatabaseReference timeRef = FirebaseDatabase.getInstance().getReference("wequest").child("time");
        timeRef.setValue(ServerValue.TIMESTAMP);


        timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong("TIME", dataSnapshot.getValue(Long.class));
                    editor.apply();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void SendNotficationToSubs(final LoadToast transactionToast, final LatLng latLng) {
        final DatabaseReference subReference;
        subReference = FirebaseDatabase.getInstance().getReference("wequest/subscribers/" + needKey);

        final ArrayList<String> subcriberEmails = new ArrayList<>();

        subReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> subscribers = (Map<String, Object>) dataSnapshot.getValue();
                    LatLng subscriberLocation;

                    for (Map.Entry<String, Object> subscriber : subscribers.entrySet()) {

                        //Get fetchUser map
                        Map<String, String> valueSubscriber = (Map<String, String>) subscriber.getValue();


                        double lat = Double.parseDouble(valueSubscriber.get("lat"));
                        double log = Double.parseDouble(valueSubscriber.get("long"));
                        String email = valueSubscriber.get("email");

                        subscriberLocation = new LatLng(lat, log);


                        // if there the user is in the radius then add it to emails;

                        //todo , this conditional distance should be defined by the user

                        if (isUserInRadiusRange(latLng, subscriberLocation, WeQuestConstants.CONDITIONAL_DISTANCE))
                            subcriberEmails.add(email);
                    }

                    subcriberEmails.remove(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                    NotificationSender.sendNotification("New Request For Your", FirebaseAuth.getInstance().getUid(), needKey, subcriberEmails, NotificationOpenHandler.TYPE_NEW_REQUEST, "Hello There");
                    transactionToast.success();
                    finishAndGotoRequestFragment();

                } else {
                    transactionToast.success();
                    finishAndGotoRequestFragment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setOnRequestListener(final ArrayList<String> userIDs, final OnRequestListener requestListener, String needKey) {

        DatabaseReference currentReq = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH);


        final ArrayList<Request> requests = new ArrayList<>();

        for (int i = 0; i < userIDs.size(); i++) {


            currentReq.child(userIDs.get(i)).
                    child(needKey).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            numberOfRequestsFetched++;
                            if (dataSnapshot.exists()) {
                                Request request = dataSnapshot.getValue(Request.class);
                                requests.add(request);

//                                LatLng requesterLL = new LatLng(request.getLatitude(), request.getLongitude());

                                // todo setting the optional how much the request notification goes
//                                if (isUserInRadiusRange(requesterLL, supplierLL)) {
                                // if (true) {
                                //   if (request.getStatus() == NOT_STARTED_STATUS) {
                                //    }
                                //    }
                                if (numberOfRequestsFetched == userIDs.size()) {

                                    requestListener.onRequestFetched(requests);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    //    public void setOnNeedKarmaListener(final int karmaRequestDetails, final OnNeedKarma onNeedKarma) {
//        final DatabaseReference needRef = FirebaseDatabase.getInstance().getReference(FIREBASE_NEED_KARMAS_PATH).child(firebasePath);
//
//        needRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                int karmaForRequest;
//                if (snapshot.exists()) {
//                    karmaForRequest = snapshot.getValue(Integer.class);
//                    if (karmaRequestDetails != KARMA_REQUEST_DETAILS)
//                        karmaForRequest++;
//                    else
//                        onNeedKarma.onNeedKarmaFetched(karmaForRequest);
//
//                } else
//                    karmaForRequest = 1;
//
//                needRef.setValue(karmaForRequest);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

}