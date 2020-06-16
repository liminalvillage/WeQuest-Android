package com.example.wequest.wequest.utils;

import android.util.Log;
import android.widget.SeekBar;

public class DistanceScrollBarUtilies {


    public interface OnScrollDistanceSetListener {

        void onDistanceChanged(int currentSeekbarDistanceValue);
    }


    public static void setListeners(SeekBar seekBar, OnScrollDistanceSetListener distanceSetListener) {

        seekBar.setOnSeekBarChangeListener(new ScrollChangeBarListener(distanceSetListener));
    }

    private static class ScrollChangeBarListener implements SeekBar.OnSeekBarChangeListener {
        private int currentSeekbarDistanceValue;
        private OnScrollDistanceSetListener distanceSetListener;

        ScrollChangeBarListener(OnScrollDistanceSetListener distanceSetListener) {
            this.distanceSetListener = distanceSetListener;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            currentSeekbarDistanceValue = 500 * (progress + 1);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            distanceSetListener.onDistanceChanged(currentSeekbarDistanceValue);
        }

    }
}
