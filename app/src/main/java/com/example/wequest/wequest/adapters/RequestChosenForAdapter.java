package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.wequest.wequest.basicActivities.ChooseSupplierActivity;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.mapActivities.SupplierMapActivity;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.R;
import com.example.wequest.wequest.utils.ChatControllerUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.ACCEPTED;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.PENDING;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.SUPPLIER_NAME;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_PROVIDER;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_REQUEST_PATH;

/**
 * Created by mohammed on 10/01/18.
 */

public class RequestChosenForAdapter extends ArrayAdapter<Request> {

    private Context context;
    private ArrayList<Request> requests;
    private int resource;

    public RequestChosenForAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Request> requests) {
        super(context, resource, requests);
        this.requests = requests;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(resource, null);


        final Request request = requests.get(position);

        TextView requestStatus = convertView.findViewById(R.id.status);
        TextView requestName = convertView.findViewById(R.id.request_name);
        ImageButton requesterLocation = convertView.findViewById(R.id.requester_location);

        switch (request.getStatus()) {
            case PENDING:
                requestStatus.setText("Pending");
                requestStatus.setTextColor(ContextCompat.getColor(context,R.color.secondary_text));
                break;

            case ACCEPTED:
                requestStatus.setTextColor(ContextCompat.getColor(context,R.color.accent));
                requestStatus.setText("Accepted");
                break;
        }

        requestName.setText(request.getNeedTitle());

        requesterLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,SupplierMapActivity.class);
                LatLng latLng= new LatLng(request.getLatitude(),request.getLongitude());
                intent.putExtra("location",latLng );
                context.startActivity(intent);
            }
        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ChooseSupplierActivity.class);
                intent.putExtra(USER_REQUEST_PATH, request.getUid()+"/"+request.getNeedKey());
                intent.putExtra(USER_PROVIDER,FirebaseAuth.getInstance().getUid());
                intent.putExtra(ChatControllerUtil.IS_USER_SUPPLIER, true);
                intent.putExtra(SUPPLIER_NAME, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                context.startActivity(intent);
            }
        });


        return convertView;
    }

}
