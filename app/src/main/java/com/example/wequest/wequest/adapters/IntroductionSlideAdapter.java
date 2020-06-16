package com.example.wequest.wequest.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.wequest.wequest.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ad on 12/28/2017.
 */

public class IntroductionSlideAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private CircleImageView circleImageView;

    public IntroductionSlideAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.rocket_ship,
            R.drawable.handshake,
            R.drawable.time,
            R.drawable.map

    };
    public String[] slide_headings = {
            "What is Wequest?",
            "Smart Barter",
            "Time matters",
            "Easy to find"
    };

    public String[] slide_bodies = {
            "Bringing communities together through shared time and skills.",
            "Exchange possessions, skills, and time instead of money.",
            "The more time you give others, the more help you’ll receive from others.",
            "A geolocalized marketplace helps keep everything local."
    };

    public int[] background_color = {
            Color.parseColor("#FFCE9A3A"),
            Color.parseColor("#FFB93C51"),
            Color.parseColor("#FF806B9F"),
            Color.parseColor("#FF375C83"),
    };
    public int[] CircleIV_color = {
            R.color.darkYellow,
            R.color.darkPink,
            R.color.lightPurple,
            R.color.darkBlue
    };


    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.introduction_slide, container, false);
        relativeLayout = view.findViewById(R.id.relative_layout);
        ImageView iconIV = view.findViewById(R.id.icon_image);
        TextView headingTV = view.findViewById(R.id.heading_tv);
        TextView bodyTV = view.findViewById(R.id.body_tv);
        circleImageView = view.findViewById(R.id.circleImageView);
        relativeLayout.setBackgroundColor(background_color[position]);
        circleImageView.setImageResource(CircleIV_color[position]);
        iconIV.setImageResource(slide_images[position]);
        headingTV.setText(slide_headings[position]);
        bodyTV.setText(slide_bodies[position]);
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
