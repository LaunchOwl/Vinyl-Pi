package io.vinylpi.app;

import android.os.Parcel;
import android.os.Parcelable;

public class PiDevice implements Parcelable {
    private String mIpAddress;
    private String mDeviceName;
    private int mConnections;

    public PiDevice() {}

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
        mIpAddress = in.readString();
        mDeviceName = in.readString();
        mConnections = in.readInt();
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIpAddress);
        dest.writeString(mDeviceName);
        dest.writeInt(mConnections);
    }

    public void setIpAddress(String ipAddress) {
        this.mIpAddress = ipAddress;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public void setConnections(int connections) {
        this.mConnections = connections;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public String getIpAddress() {
        return this.mIpAddress;
    }

    public int getConnections() {
        return this.mConnections;
    }
}