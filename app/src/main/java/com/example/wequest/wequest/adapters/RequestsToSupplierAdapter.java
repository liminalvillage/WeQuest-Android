package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRatingBar;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.utils.WeQuestOperation;

import java.util.ArrayList;

/**
 * Created by miran on 12/13/2017.
 */

public class RequestsToSupplierAdapter extends ArrayAdapter<Request> {

    private Context context;
    private int resource;
    private ArrayList<Request> requests;


    public RequestsToSupplierAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Request> requests) {
        super(context, resource, requests);
        this.context = context;
        this.resource = resource;
        this.requests = requests;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(resource, null);
        }
        Request request = requests.get(position);

        final AppCompatRatingBar ratingBar = convertView.findViewById(R.id.reputation);
        TextView requestTitle = convertView.findViewById(R.id.request_title);
        final TextView pos = convertView.findViewById(R.id.pos);
        TextView requestDesc = convertView.findViewById(R.id.request_desc);

        requestDesc.setText(request.getRequestInformation());
        requestTitle.setText(request.getNeedTitle());

        WeQuestOperation userProfile = new WeQuestOperation(requests.get(position).getUid());

        userProfile.setOnUserFetchedListener(user -> {
            ratingBar.setRating((float) user.getRating());
            pos.setText(String.valueOf(position + 1));
        });

//
//        choose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, ChooseSupplierActivity.class);
//                intent.putExtra(USER_PROVIDER, providersUIDs.get(position));
//                intent.putExtra("SP",user_profile.getUsername());
//                intent.putExtra(USER_REQUEST_PATH, userRequest);
//                context.startActivity(intent);
//            }
//        });

        return convertView;
    }

    public void swapItems(ArrayList<Request> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }
}
