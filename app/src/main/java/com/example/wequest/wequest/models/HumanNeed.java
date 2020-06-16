package com.example.wequest.wequest.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Mohammed on 3/2/2018.
 */

public class HumanNeed implements Parcelable {
    private String name;

    public HumanNeed() {


    }

    public HumanNeed(String name, ArrayList<SubHumanNeed> subHumanNeeds) {
        this.name = name;
        this.needs = subHumanNeeds;
    }

    protected HumanNeed(Parcel in) {
        name = in.readString();
        needs = in.createTypedArrayList(SubHumanNeed.CREATOR);
    }

    public static final Creator<HumanNeed> CREATOR = new Creator<HumanNeed>() {
        @Override
        public HumanNeed createFromParcel(Parcel in) {
            return new HumanNeed(in);
        }

        @Override
        public HumanNeed[] newArray(int size) {
            return new HumanNeed[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SubHumanNeed> getNeeds() {
        return needs;
    }

    public void setNeeds(ArrayList<SubHumanNeed> needs) {
        this.needs = needs;
    }

    private ArrayList<SubHumanNeed> needs;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(needs);
    }
}