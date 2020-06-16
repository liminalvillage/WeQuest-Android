package com.example.wequest.wequest.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohammed on 25/04/18.
 */

public class SubHumanNeed implements Parcelable {

    private String name;
    private int numberOfRequests;
    private int id;


    public SubHumanNeed(){

    }
    protected SubHumanNeed(Parcel in) {
        name = in.readString();
        numberOfRequests = in.readInt();
        id = in.readInt();
    }

    public static final Creator<SubHumanNeed> CREATOR = new Creator<SubHumanNeed>() {
        @Override
        public SubHumanNeed createFromParcel(Parcel in) {
            return new SubHumanNeed(in);
        }

        @Override
        public SubHumanNeed[] newArray(int size) {
            return new SubHumanNeed[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfRequests(int numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }


    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(numberOfRequests);
        dest.writeInt(id);
    }
}
