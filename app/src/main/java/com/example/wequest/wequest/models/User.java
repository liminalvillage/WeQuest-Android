package com.example.wequest.wequest.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by mohammed on 06/01/18.
 */

public class User implements Parcelable {

    public User(){

    }

    protected User(Parcel in) {
        phoneNo = in.readString();
        photoUrl = in.readString();
        email = in.readString();
        rating = in.readDouble();
        username = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        uid = in.readString();
        karma = in.readInt();
        feedbacks = in.createStringArrayList();
        timeCredit = in.readDouble();
        bio = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRating() {
        return this.rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    private String phoneNo;

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    private String photoUrl;

    public String getPhoneNo() {
        return this.phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;
    private double rating;
    private String username;
    private double latitude;
    private double longitude;
    private String uid;
    private int karma;
    private String bio;

    public ArrayList<String> getFeedbacks() {
        return this.feedbacks;
    }

    public void setFeedbacks(ArrayList<String> feedbacks) {
        this.feedbacks = feedbacks;
    }

    private ArrayList<String> feedbacks;


    public ArrayList<Integer> getRatings() {
        return this.ratings;
    }

    public void setRatings(ArrayList<Integer> ratings) {
        this.ratings = ratings;
    }

    private ArrayList<Integer> ratings;

    public int getKarma() {
        return this.karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public double getTimeCredit() {
        return this.timeCredit;
    }

    public void setTimeCredit(double timeCredit) {
        this.timeCredit = timeCredit;
    }

    private double timeCredit;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(phoneNo);
        parcel.writeString(photoUrl);
        parcel.writeString(email);
        parcel.writeDouble(rating);
        parcel.writeString(username);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(uid);
        parcel.writeInt(karma);
        parcel.writeStringList(feedbacks);
        parcel.writeDouble(timeCredit);
        parcel.writeString(bio);
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
