package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRatingBar;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.ChooseSupplierActivity;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.User;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_PROVIDER;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_REQUEST_PATH;

/**
 * Created by miran on 12/13/2017.
 */

public class SupplierListAdapter extends ArrayAdapter<User> {

    private Context context;
    private int resource;
    private ArrayList<User> suppliers;
    private ArrayList<String> providersUIDs;
    private String userRequest;


    public SupplierListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<User> suppliers, String userRequest) {
        super(context, resource, suppliers);
        this.context = context;
        this.resource = resource;
        this.suppliers = suppliers;
        this.userRequest = userRequest;
    }

    public void setProvidersUIDs(ArrayList<String> providersUIDs) {
        this.providersUIDs = providersUIDs;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(resource, null);
        }


        final User user = suppliers.get(position);


        AppCompatRatingBar ratingBar = convertView.findViewById(R.id.reputation);
        final TextView supplierName = convertView.findViewById(R.id.supplierName);
        TextView pos = convertView.findViewById(R.id.pos);
        TextView bio = convertView.findViewById(R.id.bio);
        ImageView choose = convertView.findViewById(R.id.choose);

        bio.setText(user.getBio());
        supplierName.setText(user.getUsername());
        ratingBar.setRating((float) user.getRating());
        pos.setText(String.valueOf(position + 1));


        choose.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChooseSupplierActivity.class);
            intent.putExtra(USER_PROVIDER, providersUIDs.get(position));
            intent.putExtra(USER_REQUEST_PATH, userRequest);
            intent.putExtra(WeQuestConstants.SUPPLIER_NAME, user.getUsername());
            context.startActivity(intent);
        });
        return convertView;
    }
}
