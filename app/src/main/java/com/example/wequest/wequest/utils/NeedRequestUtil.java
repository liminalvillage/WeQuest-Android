package com.example.wequest.wequest.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import androidx.core.graphics.drawable.DrawableCompat;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.models.SubHumanNeed;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mohammed on 25/04/18.
 */

public class NeedRequestUtil {


    public static long getTimeServer(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long timeServer = preferences.getLong("TIME", 0);

        return timeServer;
    }

    public static void sortNeedsByRequest(ArrayList<SubHumanNeed> newSubCatNeeds) {
        Collections.sort(newSubCatNeeds, (o1, o2) ->
                o2.getNumberOfRequests() - o1.getNumberOfRequests());
    }

    public static int getTotalNeedRequests(ArrayList<SubHumanNeed> humanSubNeeds) {
        int totalRequests = 0;
        for (SubHumanNeed humanSubNeed : humanSubNeeds) {
            totalRequests += humanSubNeed.getNumberOfRequests();
        }
        return totalRequests;
    }

    public static void setRequestProgressColor(ProgressBar progressBar, String currentColor, int position, int itemCount) {

        LayerDrawable drawable = (LayerDrawable) progressBar.getProgressDrawable();


        Drawable currentdrawable = drawable.findDrawableByLayerId(R.id.process_drawable);


        int newAlphaColor = Math.max(getCurrentColor(currentColor, position, itemCount), 16);

        String newHexColor = "#" + Integer.toHexString(newAlphaColor) + currentColor.substring(1);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            currentdrawable.setTint(Color.parseColor(newHexColor));

            progressBar.setBackground(currentdrawable);


        } else {

            Drawable mutatedCurrentDrawable = DrawableCompat.wrap(currentdrawable.mutate());

            mutatedCurrentDrawable.setColorFilter(Color.parseColor(newHexColor), PorterDuff.Mode.SRC_IN);
            progressBar.setProgressDrawable(drawable);


        }
    }

    private static int getCurrentColor(String currentColor, int position, int itemCount) {
        int newCurrentColor;

        newCurrentColor = Color.alpha(Color.parseColor(currentColor));

        newCurrentColor = newCurrentColor - (int) (((double) position / itemCount) * newCurrentColor);

        return newCurrentColor;
    }






    public interface OnSupplierCanceling{

       /*
       @return the boolean indicating a confirmation of canceling or not
        */

        void onCancel(boolean isCanceled);
    }
}