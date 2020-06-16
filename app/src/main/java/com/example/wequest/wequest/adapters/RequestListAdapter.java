package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.FinishTransactionActivity;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.mapActivities.RequesterSeeSupplierMapActivity;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.EXPIRED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FINISHED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_REQUEST_TO_ME_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.NOT_STARTED_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.ON_PROGRESS_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.REQUEST_OBJECT;
import static com.example.wequest.wequest.utils.WeQuestOperation.DATA_VALUE_DECREMENT;

/**
 * Created by mohammed on 10/01/18.
 */

public class RequestListAdapter extends ArrayAdapter<Request> {

    private Context context;
    private ArrayList<Request> requests;
    private int resource;
    private long timeServer;

    public RequestListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Request> requests, long timeServer) {
        super(context, resource, requests);
        this.requests = requests;
        this.context = context;
        this.resource = resource;
        this.timeServer = timeServer;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(resource, null);


        final Request request = requests.get(position);
        int pos = position + 1;
        Button transfer = convertView.findViewById(R.id.transfere);
        Button removeRequest = convertView.findViewById(R.id.remove_request);
        TextView supplierFound = convertView.findViewById(R.id.suppliers_found);

        supplierFound.setText(context.getString(R.string.suppliers_found,
                request.getProviders() != null ? request.getProviders().size() : 0));

        removeRequest.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Cancel Request");
            final boolean isSupplierChosen;
            if (request.getChosenProvider() != null && request.getStatus() == ON_PROGRESS_STATUS) {
                isSupplierChosen = true;
                builder.setMessage("There is a chosen Supplier For this Request,\nCanceling it will Cost you 1 karma.Are you Sure?");
            } else {
                isSupplierChosen = false;
                builder.setMessage("Are you sure?");
            }


            builder.setPositiveButton("Yes", (dialog, which) -> {


                // and also check weather this request is No Started or On Gonging then decrease
                if (request.getStatus() == NOT_STARTED_STATUS || request.getStatus() == ON_PROGRESS_STATUS) {


                    /// decreasing the the number of the need request
                    DatabaseReference needRequestNumberRef;
                    needRequestNumberRef = FireBaseReferenceUtils.getNeedRequestNumberRef(request.getNeedKey());
                    WeQuestOperation.updateDataCountNumber(needRequestNumberRef, DATA_VALUE_DECREMENT);

                    // deceasing the number of karma
                    WeQuestOperation.updateDataCountNumber(FireBaseReferenceUtils.getNeedKarmaRef(request.getNeedKey()),
                            DATA_VALUE_DECREMENT);
                }

                if (isSupplierChosen)
                    WeQuestOperation.decreaseUserKarma();

                // removing the request in the sections of the user requests
                DatabaseReference removeReq = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH)
                        .child(FireBaseHelper.getUid()).child(request.getNeedKey());
                removeReq.removeValue();


                // todo just fix the screen of the supplier
                // removing the chat history between the two one

                DatabaseReference chatRef;
                chatRef = FireBaseReferenceUtils.getChatRefForMe(request.getUid() + "/" + request.getNeedKey(), "");

                chatRef.removeValue();

                // removing the request from the supplier's side

                if(request.getProviders() != null) {
                    DatabaseReference requestsChosenForRef;

                    for (String provider : request.getProviders()) {
                        requestsChosenForRef = FireBaseReferenceUtils.getRequestsChosenForRef(provider)
                                .child(request.getUid()).child(request.getNeedKey());
                        requestsChosenForRef.removeValue();
                    }
                }


                // removing the requests in the section of the requests to to the suppliers
                removeReq = FirebaseDatabase.getInstance().getReference(FIREBASE_REQUEST_TO_ME_PATH)
                        .child(request.getNeedKey()).child(FireBaseHelper.getUid());
                removeReq.removeValue();

//                removeReq.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            if (snapshot.getValue(String.class).equals(FireBaseHelper.getUid())) {
//                                snapshot.getRef().removeValue();
//                                break;
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });

                requests.remove(position);
                notifyDataSetChanged();
                dialog.dismiss();
            });

            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        });


        // TODO: 21/07/18 should make this as a dispached job schedular

        if (request.getStatus() != EXPIRED_STATUS && (request.getDueDate() != 0 && request.getDueDate() < timeServer)) {
            // expiring the request status
            request.setStatus(EXPIRED_STATUS);
            WeQuestOperation.updateRequestStatus(request.getUid() + "/" + request.getNeedKey(), EXPIRED_STATUS);


            DatabaseReference needKarmaPath;
            needKarmaPath = FireBaseReferenceUtils.getNeedKarmaRef(request.getNeedKey());

            WeQuestOperation.updateDataCountNumber(needKarmaPath, DATA_VALUE_DECREMENT);
            //updating the Karma needed for this need


            // updating the number of requests for this need
            WeQuestOperation.updateDataCountNumber(FirebaseDatabase.getInstance().
                    getReference(WeQuestConstants.NEED_REQUESTS_NUMBER)
                    .child(String.valueOf(request.getNeedKey().charAt(0))).
                            child(String.valueOf(request.getNeedKey().charAt(1))), DATA_VALUE_DECREMENT);
        }

        final TextView status = convertView.findViewById(R.id.status);
        status.setText(request.getNeedTitle());
        TextView title = convertView.findViewById(R.id.needTitle);
        title.setText(context.getString(R.string.request_title, request.getNeedCategoryName(), request.getNeedTitle()));

        switch (request.getStatus()) {
            case ON_PROGRESS_STATUS:
                status.setText(R.string.status_ongoing);
                status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
//                transfer.setEnabled(true);
                break;
            case NOT_STARTED_STATUS:
                status.setText(R.string.status_not_start);
                status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
//                transfer.setEnabled(false);
                break;
            case FINISHED_STATUS:
                status.setText(R.string.status_finish);
                status.setTextColor(Color.rgb(86, 153, 38));
//                transfer.setEnabled(false);
                break;
            case EXPIRED_STATUS:
                status.setText(R.string.status_expired);
                status.setTextColor(Color.rgb(184, 41, 38));
//                transfer.setEnabled(false);
                break;


        }
        TextView listCount = convertView.findViewById(R.id.list_count);
        listCount.setText(String.valueOf(pos));

        transfer.setEnabled(request.getStatus() == ON_PROGRESS_STATUS);


        transfer.setOnClickListener(view -> {
            Intent intent = new Intent(context, FinishTransactionActivity.class);
            intent.putExtra(REQUEST_OBJECT, request);
            context.startActivity(intent);
        });



              /*
            opening the request on the map and showing the suppliers
         */
        supplierFound.setOnClickListener(view -> {


            Intent intent = new Intent(getContext(), RequesterSeeSupplierMapActivity.class);
            intent.putExtra(REQUEST_OBJECT, request);
            context.startActivity(intent);
        });


        return convertView;
    }

//    @Override
//    public boolean areAllItemsEnabled() {
//        return false;
//    }

//    @Override
//    public boolean isEnabled(int position) {
//
//        return (requests.get(position).getStatus() != FINISHED_STATUS) &&
//                (((requests.get(position).getDueDate()) == 0) ||
//                        (requests.get(position).getDueDate() > timeServer));
//
//    }
}
