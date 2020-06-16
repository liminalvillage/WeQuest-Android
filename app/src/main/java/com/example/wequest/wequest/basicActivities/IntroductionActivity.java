package com.example.wequest.wequest.basicActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.adapters.IntroductionSlideAdapter;

public class IntroductionActivity extends AppCompatActivity {

    public static final String IS_FROM_ABOUT = "fromAbout";
    private LinearLayout mLinearLayout;
    private Button skipBT;
    private ImageButton nextBT;
    private ViewPager mViewPager;
    private IntroductionSlideAdapter introductionSlideAdapater;
    private int mCurrentPage;
    private TextView[] mDots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        mLinearLayout = findViewById(R.id.linear_layout);
        skipBT = findViewById(R.id.skip_btn);
        nextBT = findViewById(R.id.next_btn);
        mViewPager = findViewById(R.id.view_pager);
        introductionSlideAdapater = new IntroductionSlideAdapter(this);
        mViewPager.setAdapter(introductionSlideAdapater);
        mViewPager.addOnPageChangeListener(viewListener);

        addDots(0);
        nextBT.setOnClickListener(view -> mViewPager.setCurrentItem(mCurrentPage + 1));

        skipBT.setOnClickListener(view -> {
            finishIntroActivity();

        });
    }

    private void finishIntroActivity() {
        boolean isFromAbout = getIntent().getBooleanExtra(IS_FROM_ABOUT, false);
        if (!isFromAbout)
            startActivity(new Intent(IntroductionActivity.this, MainActivity.class));

        finish();
    }


    public void addDots(int position) {
        mDots = new TextView[4];
        mLinearLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.transparentColor));
            mLinearLayout.addView(mDots[i]);
        }
        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.darkGray));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int i) {
            addDots(i);
            mCurrentPage = i;

            if (i == 0) {
                nextBT.setEnabled(true);
                nextBT.setImageResource(R.drawable.right_arrow_icon);
            } else if (i == mDots.length - 1) {
                nextBT.setEnabled(true);
                nextBT.setImageResource(R.drawable.check_icon);

                nextBT.setOnClickListener(view -> {
                    finishIntroActivity();

                });
            } else {
                nextBT.setEnabled(true);
                nextBT.setImageResource(R.drawable.right_arrow_icon);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
