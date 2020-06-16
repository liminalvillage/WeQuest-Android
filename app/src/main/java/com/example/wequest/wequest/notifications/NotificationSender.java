package com.example.wequest.wequest.notifications;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.example.wequest.wequest.models.FiltersBean;
import com.example.wequest.wequest.models.Notification;
import com.example.wequest.wequest.utils.FireBaseHelper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NotificationSender {

    public static void notifyNewSupplier(String uid, String requestKey, String title) {

        /*
            1- first we should get the user id for the requester
            2- to send the onesignal notification, we need the email of the user
            3- fetch the user's email through th uid
            4- send the onesignal notification to that email
         */

        FireBaseHelper.getEmailFromUid(uid, email -> {

            // sending the notification to the requester
            ArrayList<String> emailList = new ArrayList<>();

            emailList.add(email);


            sendNotification("New Supplier", uid, requestKey, emailList, NotificationOpenHandler.TYPE_NEW_SUPPLIER, title);

        });


    }


    public static void sendNotification(String subTitle, final String uid, final String requestNumber, final ArrayList<String> emails, int type, String title) {
        AsyncTask.execute(() -> {

            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);

                //This is a Simple Logic to Send Notification different Device Programmatically...

                String send_email;
                for (int i = 0; i < emails.size(); i++) {

                    send_email = emails.get(i);
                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);


                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic YTQ5MGYwMzctNDYyYi00YWJmLWFlZTUtODU1NzZlMjg4ZDE1");
                        con.setRequestMethod("POST");


                        Notification notification = new Notification();
                        List<FiltersBean> filtersBeans = new ArrayList<>();
                        FiltersBean filter = new FiltersBean();
                        filter.setField("tag");
                        filter.setKey("email");
                        filter.setRelation("=");
                        filter.setValue(send_email);

                        filtersBeans.add(filter);
                        notification.setFilters(filtersBeans);


                        String strJsonBody = "{"
                                + "\"app_id\": \"f3efcaa3-03d9-48cb-b9ee-744a40c0034d\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"email\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"uid\":\"" + uid + "\",\"key\":\"" + requestNumber + "\",\"type\":\"" + type + "\"},"
                                + "\"contents\": {\"en\":\"" + subTitle + "\"},"
                                + "\"headings\":{\"en\":\"" + title + "\"}"
                                + "}";

                        Log.e("BODY", strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();

                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }


                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }


            }
        });

    }

}
