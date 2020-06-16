package com.example.wequest.wequest.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.wequest.wequest.mainFragments.MyNeedRequestFragment;
import com.example.wequest.wequest.mainFragments.NeedRequestsChosenForFragment;

import java.util.ArrayList;

/**
 * Created by miran on 12/13/2017.
 */

public class RequestStatusFragmentAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment> fragments;
    private ArrayList<String> names;

    public RequestStatusFragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        fragments = new ArrayList<>();
        names = new ArrayList<>();
        fragments.add(new MyNeedRequestFragment());
        fragments.add(new NeedRequestsChosenForFragment());

        names.add("My Requests");
        names.add("From Others");
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names.get(position);

    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}