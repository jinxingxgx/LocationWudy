package com.wudy.locationwudy.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by xgx on 2019/4/10 for LocationWudy
 */
@Entity
public class MapLocationBean implements Serializable, Parcelable {
    private static final long serialVersionUID = -7916135203896142543L;
    @Property
    private int id;
    @Property
    public double latitude;
    @Property
    public double longitude;
    @Property
    public float speed;
    @Property
    public float direction;
    @Property
    public float accuracy;
    @Property
    public int satellitesNum;
    @Property
    public String address;
    @Property
    public String city;
    @Property
    public String time;

    protected MapLocationBean(Parcel in) {
        id = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        speed = in.readFloat();
        direction = in.readFloat();
        accuracy = in.readFloat();
        satellitesNum = in.readInt();
        address = in.readString();
        city = in.readString();
        time = in.readString();
    }

    @Generated(hash = 1862973611)
    public MapLocationBean(int id, double latitude, double longitude, float speed,
            float direction, float accuracy, int satellitesNum, String address, String city,
            String time) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.direction = direction;
        this.accuracy = accuracy;
        this.satellitesNum = satellitesNum;
        this.address = address;
        this.city = city;
        this.time = time;
    }

    @Generated(hash = 706918558)
    public MapLocationBean() {
    }

    public static final Creator<MapLocationBean> CREATOR = new Creator<MapLocationBean>() {
        @Override
        public MapLocationBean createFromParcel(Parcel in) {
            return new MapLocationBean(in);
        }

        @Override
        public MapLocationBean[] newArray(int size) {
            return new MapLocationBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeFloat(speed);
        dest.writeFloat(direction);
        dest.writeFloat(accuracy);
        dest.writeInt(satellitesNum);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(time);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
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

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDirection() {
        return this.direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public float getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public int getSatellitesNum() {
        return this.satellitesNum;
    }

    public void setSatellitesNum(int satellitesNum) {
        this.satellitesNum = satellitesNum;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
