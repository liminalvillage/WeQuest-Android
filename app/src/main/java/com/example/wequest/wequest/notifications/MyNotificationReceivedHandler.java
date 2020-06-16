package com.example.wequest.wequest.notifications;

import android.content.Context;

import com.example.wequest.wequest.utils.FireBaseHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.google.firebase.database.FirebaseDatabase.getInstance;


//This will be called when a notification is received while your app is running.
public class MyNotificationReceivedHandler  implements OneSignal.NotificationReceivedHandler {
    private Context context;
    MyNotificationReceivedHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void notificationReceived(OSNotification notification) {
        final JSONObject additionalData = notification.payload.additionalData;


        //todo if its my own notification, we should proceed it
        if (additionalData != null) {
            try {
                String uid  = additionalData.getString("uid");
                String needKey = additionalData.getString("key");


                DatabaseReference requestNotificationPathForMe = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH).
                        child(FireBaseHelper.getUid()+"/requeststome").child(uid).child(needKey);

                requestNotificationPathForMe.setValue(true);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}