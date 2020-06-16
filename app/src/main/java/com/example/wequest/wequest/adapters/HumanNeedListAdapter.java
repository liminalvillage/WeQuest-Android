package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.R.id;
import com.example.wequest.wequest.interfaces.CustomItemClickListener;
import com.example.wequest.wequest.models.HumanNeed;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by miran on 12/13/2017.
 */

public class HumanNeedListAdapter extends RecyclerView.Adapter<HumanNeedListAdapter.MyViewHolder> {

     private ArrayList<HumanNeed> objects;
    private CustomItemClickListener customItemClickListener;

    private int needIcons[] =
            {R.drawable.subsistence,R.drawable.protection,R.drawable.affection,R.drawable.understanding,R.drawable.participation,
                    R.drawable.leisure,R.drawable.creation,R.drawable.identity,R.drawable.freedom};
    private  String [] needColorList;

            /*
             {R.color.list1,R.color.list2,R.color.list3,R.color.list4,R.color.list5,R.color.list6,R.color.list7,
            R.color.list8,R.color.list9};
             */


    public HumanNeedListAdapter(@NonNull Context context, @NonNull ArrayList<HumanNeed> objects, CustomItemClickListener customItemClickListener) {

         needColorList = context.getResources().getStringArray(R.array.list_color);
        this.customItemClickListener = customItemClickListener;
        this.objects = objects;
     }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_needs,parent,false);
        final MyViewHolder myViewHolder = new MyViewHolder(view);
        view.setOnClickListener(v ->
                customItemClickListener.onItemClick(view,myViewHolder.getAdapterPosition()));

        return  myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setBackgroundResource(needIcons[position]);
            holder.listColor.setBackgroundColor(Color.parseColor(needColorList[position]));
            holder.name.setText(objects.get(position).getName());


    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView listColor;
        CircleImageView image;
        ProgressBar progressBar;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(id.item_name);
            listColor = itemView.findViewById(id.background_itemlist);
            image = itemView.findViewById(R.id.needImage);
            progressBar = itemView.findViewById(id.needRequestProgress);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
