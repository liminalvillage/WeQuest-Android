package com.example.wequest.wequest.interfaces;

/**
 * Created by mohammed on 25/12/17.
 */

public interface WeQuestConstants {


    String root = "wequest";
    String FIREBASE_HUMAN_NEED_PATH = root + "/humanneeds";

    int HUMAN_NEED_STATUS = 1;
    int HUMAN_SUB_CAT_STATUS = 2;

    int NOT_STARTED_STATUS = 1;
    int ON_PROGRESS_STATUS = 2;
    int FINISHED_STATUS = 3;
    int EXPIRED_STATUS = 4;

    int FINISH_ACTIVITY = 1000;
    int REQUEST_FRAGMENT_INDEX = 1;
    String GOTO_REQUEST_FRAGMENT = "goto_req_frag";

    String NEED_REQUESTS_NUMBER = root+"/needRequestNo";
    String NEED_KEY = "needID";
    int LOCATION_REQ_PERMISSION = 3123;
    String NEED_NAME = "needName";
    String USER_PROVIDER = "providerID";
    String FIREBASE_USER_REQUESTS_PATH = root+"/requests/";
    String FIREBASE_REQUEST_TO_ME_PATH = root+"/requests2/";

    String USER_REQUEST_PATH = "requestPath";
    String REQUEST_OBJECT = "user_profile";
    String FIREBASE_USER_PROFILE_PATH = root+"/userProfiles/";
    String FIREBASE_USER_FEEDBACK_PATH = root+"/feedbacks/";

    String FIREBASE_NEED_KARMAS_PATH = root+"/needkarmas/";

    int CONDITIONAL_DISTANCE = 3000;
    int KARMA_REQUEST_DETAILS = 1;
    String SUPPLIER_NAME = "supplierName";
    String SUPPLIER_PICTURE_URL = "picUrl";
    String REQUESTS_CHOSEN_FOR = root+"/requestsUserChosenFor";
    String CHAT_REF = root+"/chats";

    int PENDING = 0;
    int ACCEPTED = 1;
    String UNIQUE_ID = root+"/uniqueid";
}
