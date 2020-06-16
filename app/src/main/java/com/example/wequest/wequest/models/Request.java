package com.example.wequest.wequest.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by miran on 12/13/2017.
 */

public class Request implements Parcelable {


    private double latitude;
    private double longitude;
    private String needTitle;
    private String uid;
    private String needKey;
    private double hour;

    protected Request(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        needTitle = in.readString();
        uid = in.readString();
        needKey = in.readString();
        hour = in.readDouble();
        requestInformation = in.readString();
        status = in.readInt();
        karma = in.readInt();
        providers = in.createStringArrayList();
        chosenProvider = in.readString();
        needCategoryName = in.readString();
        dueDate = in.readDouble();
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNeedTitle() {
        return needTitle;
    }

    public void setNeedTitle(String needTitle) {
        this.needTitle = needTitle;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNeedKey() {
        return needKey;
    }

    public void setNeedKey(String needKey) {
        this.needKey = needKey;
    }

    public double getHour() {
        return hour;
    }

    public void setHour(double hour) {
        this.hour = hour;
    }

    public String getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(String requestInformation) {
        this.requestInformation = requestInformation;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public ArrayList<String> getProviders() {
        return providers;
    }

    public void setProviders(ArrayList<String> providers) {
        this.providers = providers;
    }

    public String getChosenProvider() {
        return chosenProvider;
    }

    public void setChosenProvider(String chosenProvider) {
        this.chosenProvider = chosenProvider;
    }

    public String getNeedCategoryName() {
        return needCategoryName;
    }

    public void setNeedCategoryName(String needCategoryName) {
        this.needCategoryName = needCategoryName;
    }

    public double getDueDate() {
        return dueDate;
    }

    public void setDueDate(double dueDate) {
        this.dueDate = dueDate;
    }

    private String requestInformation;
    private int status;
    private int karma;
    private ArrayList<String> providers;
    private String chosenProvider;
    private String needCategoryName;
    private double dueDate;


    public Request() {
        //empty constructor
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(needTitle);
        dest.writeString(uid);
        dest.writeString(needKey);
        dest.writeDouble(hour);
        dest.writeString(requestInformation);
        dest.writeInt(status);
        dest.writeInt(karma);
        dest.writeStringList(providers);
        dest.writeString(chosenProvider);
        dest.writeString(needCategoryName);
        dest.writeDouble(dueDate);
    }
}
