package com.example.wequest.wequest.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.wequest.wequest.mainFragments.NeedsFragment;
import com.example.wequest.wequest.mainFragments.RequestsFragment;
import com.example.wequest.wequest.mainFragments.UserDashboardFragment;

import java.util.ArrayList;

/**
 * Created by miran on 12/13/2017.
 */

public class NeedRequestFragmentAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment> fragments;
    private ArrayList<String> names;

    public NeedRequestFragmentAdapter(FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<>();
        names = new ArrayList<>();
        fragments.add(new NeedsFragment());
        fragments.add(new RequestsFragment());
        fragments.add(new UserDashboardFragment());

        names.add("Request");
        names.add("Status");
        names.add("Me");
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