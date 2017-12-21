package com.trackyourstuff.mota.trackyourstuff.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by mota on 10/18/17.
 */

public class Transporter implements Parcelable {
    private String name;
    private String username;
    private String id;
    private ArrayList<String> gonnaPick_ids;
    private ArrayList<String> gonnaDeliver_ids;

    public Transporter(String id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.gonnaPick_ids = new ArrayList<String>();
        this.gonnaDeliver_ids = new ArrayList<String>();
    }

    public ArrayList<String> getGonnaPick_ids() {
        return gonnaPick_ids;
    }

    public void setGonnaPick_ids(ArrayList<String> gonnaPick_ids) {
        this.gonnaPick_ids = gonnaPick_ids;
    }

    public ArrayList<String> getGonnaDeliver_ids() {
        return gonnaDeliver_ids;
    }

    public void setGonnaDeliver_ids(ArrayList<String> gonnaDeliver_ids) {
        this.gonnaDeliver_ids = gonnaDeliver_ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(username);
        out.writeString(name);
    }

    public static final Parcelable.Creator<Transporter> CREATOR = new Parcelable.Creator<Transporter>() {
        public Transporter createFromParcel(Parcel in) {
            return new Transporter(in);
        }

        public Transporter[] newArray(int size) {
            return new Transporter[size];
        }
    };

    public Transporter(Parcel in) {
        id = in.readString();
        username = in.readString();
        name = in.readString();
    }
}
