package com.example.wequest.wequest.utils;

import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_REQUEST_TO_ME_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_PROFILE_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUESTS_CHOSEN_FOR;

public class FireBaseReferenceUtils {

    public static DatabaseReference getRequestsChosenForRef(String providerID) {

        return FirebaseDatabase.getInstance().getReference(
                REQUESTS_CHOSEN_FOR).child(providerID);
    }

    public static DatabaseReference getRequestChosenProviderRef(String requestPath) {


        return FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH
                + requestPath).child("chosenProvider" );

    }

    public static Query getChatQueryForMe(String requestPath, String providerID) {

       Query myChatRef = FirebaseDatabase.getInstance().getReference(WeQuestConstants.CHAT_REF)
                .child(requestPath)
                .child(providerID);
       return myChatRef;
    }

    public static DatabaseReference getChatRefForMe(String requestPath,String providerID) {


        DatabaseReference myChatRef = FirebaseDatabase.getInstance().getReference(WeQuestConstants.CHAT_REF)
                .child(requestPath)
                .child(providerID);
        return myChatRef;
    }


    public static DatabaseReference getQrCodeTransactionRef(String uid) {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_USER_PROFILE_PATH).child(uid)
                .child("qrcode");
    }

    public static DatabaseReference getNeedRequestNumberRef(String needKey) {
        return FirebaseDatabase.getInstance().
                getReference(WeQuestConstants.NEED_REQUESTS_NUMBER)
                .child(String.valueOf(needKey.charAt(0))).
                child(String.valueOf(needKey.charAt(1)));

    }

    public static DatabaseReference getNeedKarmaRef(String needKey) {

        return FirebaseDatabase.getInstance().getReference(WeQuestConstants.FIREBASE_NEED_KARMAS_PATH)
                .child(needKey);
    }


    public static DatabaseReference getUserRef(String uid) {
        return FirebaseDatabase.getInstance().getReference(WeQuestConstants.FIREBASE_USER_PROFILE_PATH)
                .child(uid);
    }


    public static DatabaseReference getUserBioRef(String uid) {
        return getUserRef(uid).child("bio");
    }

    public static DatabaseReference getUserEmailRef(String uid) {
        return getUserRef(uid).child("email");
    }

    public static DatabaseReference getRequestPublicRef(String requestPath) {
        String[] requestPathSplited = requestPath.split("/");


        return FirebaseDatabase.getInstance().getReference(FIREBASE_REQUEST_TO_ME_PATH)
                .child(requestPathSplited[1]).child(requestPathSplited[0]);
    }
}
