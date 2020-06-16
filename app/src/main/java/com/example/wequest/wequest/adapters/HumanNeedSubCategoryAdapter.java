package com.example.wequest.wequest.adapters;

import android.content.Context;
 import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.interfaces.CustomItemClickListener;
import com.example.wequest.wequest.models.SubHumanNeed;
import com.example.wequest.wequest.utils.NeedRequestUtil;

import java.util.ArrayList;

/**
 * Created by miran on 12/13/2017.
 */

public class HumanNeedSubCategoryAdapter  extends RecyclerView.Adapter<HumanNeedSubCategoryAdapter.MyViewHolder> {

    private int CURRENT_SATUS;
    private final Context context;
    private ArrayList<SubHumanNeed> humanSubNeeds;

    private String[] needColorList;

    /*
     {R.color.list1,R.color.list2,R.color.list3,R.color.list4,R.color.list5,R.color.list6,R.color.list7,
    R.color.list8,R.color.list9};
     */
    private int selectedHumanNeed;
    private CustomItemClickListener customItemClickListener;
    private int totalNeedRequest;
    private String currentColor;

    public HumanNeedSubCategoryAdapter(@NonNull Context context, @NonNull ArrayList<SubHumanNeed> humanSubNeeds, int selectedHumanNeed, CustomItemClickListener customItemClickListener) {
        currentColor = context.getResources().getStringArray(R.array.list_color)[selectedHumanNeed];
        this.context = context;
        this.humanSubNeeds = humanSubNeeds;
        this.selectedHumanNeed = selectedHumanNeed;
        this.customItemClickListener = customItemClickListener;
        totalNeedRequest = NeedRequestUtil.getTotalNeedRequests(humanSubNeeds);
    }



    @Override
    public HumanNeedSubCategoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_needs,parent,false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        view.setOnClickListener(v ->
                customItemClickListener.onItemClick(v, viewHolder.getAdapterPosition())
        );

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.name.setText(humanSubNeeds.get(holder.getAdapterPosition()).getName());

        final int currentNeedRequest = humanSubNeeds.get(holder.getAdapterPosition()).getNumberOfRequests();


        holder.progressBar.setProgress(0);

        NeedRequestUtil.setRequestProgressColor(holder.progressBar, currentColor, position, getItemCount());


        holder.progressBar.post(() -> {

            if (currentNeedRequest != 0) {


                double newProgress = ((double) currentNeedRequest / totalNeedRequest) * totalNeedRequest;
                holder.progressBar.setMax(totalNeedRequest);
                holder.progressBar.setProgress((int) newProgress);
            } else {
                holder.progressBar.setProgress(totalNeedRequest); // call these two methods before setting progress.
                holder.progressBar.setProgress(0);
            }
        });


    }

    @Override
    public int getItemCount() {
        return humanSubNeeds.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ProgressBar progressBar;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            progressBar = itemView.findViewById(R.id.needRequestProgress);

        }
    }
}
