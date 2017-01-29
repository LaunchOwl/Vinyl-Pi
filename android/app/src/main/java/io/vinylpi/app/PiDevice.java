package io.vinylpi.app;

import android.os.Parcel;
import android.os.Parcelable;

public class PiDevice implements Parcelable {
    private String mDeviceName;
    private int mConnections;

    public PiDevice() {}

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public PiDevice(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<PiDevice> CREATOR = new Parcelable.Creator<PiDevice>() {
        public PiDevice createFromParcel(Parcel in) {
            return new PiDevice(in);
        }

        public PiDevice[] newArray(int size) {

            return new PiDevice[size];
        }

    };

    public void readFromParcel(Parcel in) {
        mDeviceName = in.readString();
        mConnections = in.readInt();
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDeviceName);
        dest.writeInt(mConnections);
    }

    public void setConnections(int connections) {
        this.mConnections = connections;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public int getConnections() {
        return this.mConnections;
    }


}