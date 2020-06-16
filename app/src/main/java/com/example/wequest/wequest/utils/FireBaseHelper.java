package com.example.wequest.wequest.utils;


import androidx.annotation.NonNull;

import com.example.wequest.wequest.interfaces.OnFireBaseValueListener;
import com.example.wequest.wequest.interfaces.OnUserEmail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by mohammed on 09/01/18.
 */

public class FireBaseHelper {

    private static final FireBaseHelper ourInstance = new FireBaseHelper();
    private OnFireBaseValueListener valueListener;


    public static FireBaseHelper getInstance() {

        return new FireBaseHelper();
}

    private FireBaseHelper() {

    }


    public synchronized void setFireBaseValueListener(DatabaseReference path, OnFireBaseValueListener valueListener) {

        this.valueListener = valueListener;
        path.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FireBaseHelper.this.valueListener.onValueFetched(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void getEmailFromUid(String uid, OnUserEmail onUserEmail) {

        DatabaseReference userMailRef = FireBaseReferenceUtils.getUserEmailRef(uid);

        userMailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = null;
                if (dataSnapshot.exists()) {
                    email = dataSnapshot.getValue(String.class);
                    onUserEmail.onFetch(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
