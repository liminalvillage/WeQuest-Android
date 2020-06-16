package com.example.wequest.wequest.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.wequest.wequest.mapActivities.RequesterSeeSupplierMapActivity;
import com.example.wequest.wequest.mapActivities.SupplierMapActivity;
import com.example.wequest.wequest.models.Request;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NEED_KEY;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.google.firebase.database.FirebaseDatabase.getInstance;

/**
 * Created by mohammed on 26/12/17.
 */

public class NotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
    private Context context;
    public static final String NOTIFICATION_TYPE = "type";
    public static final int TYPE_NEW_REQUEST = 1;
    public static final int TYPE_NEW_SUPPLIER = 2;


    NotificationOpenHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;

        final JSONObject additionalData = result.notification.payload.additionalData;

        if (additionalData != null) {
            try {
                String uid = additionalData.getString("uid");
                String needKey = additionalData.getString("key");
                //like this
                int type = additionalData.getInt(NOTIFICATION_TYPE);

                DatabaseReference requestRef = getInstance().getReference(FIREBASE_USER_REQUESTS_PATH).
                        child(uid).child(needKey);


                requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //so here we have to check the type of the notification type
                        //then

                        Intent intent = new Intent();
                        Request request = dataSnapshot.getValue(Request.class);

                        String requestKey = request.getNeedKey();

                        intent.putExtra(NEED_KEY, requestKey);
                        intent.putExtra(REQUEST_OBJECT, request);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);


                        switch (type) {
                            case TYPE_NEW_SUPPLIER:
                                intent.setClass(context, RequesterSeeSupplierMapActivity.class);
                                break;
                            case TYPE_NEW_REQUEST:
                                intent.setClass(context, SupplierMapActivity.class);
                                break;
                        }

                        context.startActivity(intent);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
