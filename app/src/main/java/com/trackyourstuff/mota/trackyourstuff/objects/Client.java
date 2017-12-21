package com.trackyourstuff.mota.trackyourstuff.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mota on 10/18/17.
 */

public class Client implements Parcelable {
    private String id;
    private String username;
    private String name;
    private String address;

    public Client(String id, String username, String name, String address) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
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
        out.writeString(address);
    }

    public static final Parcelable.Creator<Client> CREATOR = new Parcelable.Creator<Client>() {
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    public Client(Parcel in) {
        id = in.readString();
        username = in.readString();
        name = in.readString();
        address = in.readString();
    }
}
