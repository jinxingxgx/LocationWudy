package com.wudy.locationwudy.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.wudy.locationwudy.adapter.LocationListAdapter;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by xgx on 2019/4/10 for LocationWudy
 */
@Entity
public class MapLocationBean implements MultiItemEntity, Serializable, Parcelable {
    private static final long serialVersionUID = -7916135203896142543L;
    @Property
    private int id;
    @Property
    public String indexId;
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
        indexId = in.readString();
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

    @Generated(hash = 1688090212)
    public MapLocationBean(int id, String indexId, double latitude, double longitude,
                           float speed, float direction, float accuracy, int satellitesNum, String address,
                           String city, String time) {
        this.id = id;
        this.indexId = indexId;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(indexId);
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

    @Override
    public int describeContents() {
        return 0;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public int getSatellitesNum() {
        return satellitesNum;
    }

    public void setSatellitesNum(int satellitesNum) {
        this.satellitesNum = satellitesNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int getItemType() {
        return LocationListAdapter.CONTENT;
    }
}
