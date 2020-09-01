package com.example.mapguide;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Station implements Parcelable, Serializable {

    public int number;
    public double longitude;
    public double latitude;
    public String title;
    public String audioSrcPath;
    public String imgSrcPath;
    public String description;
    private List<MediaElement> mediaElementList = new ArrayList<>();

    public Station(int number, double longitude, double latitude, String title, String audioSrcPath, String imgSrcPath, String description, List<MediaElement> mediaElementList) {
        this.number = number;
        this.longitude = longitude;
        this.latitude = latitude;
        this.title = title;
        this.audioSrcPath = audioSrcPath;
        this.imgSrcPath = imgSrcPath;
        this.description = description;
        this.mediaElementList = mediaElementList;
    }

    public Station(){}

    public int getNumber() {
        return number;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getTitle() {
        return title;
    }

    public String getAudioSrcPath() {
        return audioSrcPath;
    }

    public String getImgSrcPath() {
        return imgSrcPath;
    }

    public String getDescription() {
        return description;
    }


    public void setNumber(int number) {
        this.number = number;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAudioSrcPath(String audioSrcPath) {
        this.audioSrcPath = audioSrcPath;
    }

    public void setImgSrcPath(String imgSrcPath) {
        this.imgSrcPath = imgSrcPath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MediaElement> getMediaElementList() {
        return mediaElementList;
    }

    public void setMediaElementList(List<MediaElement> mediaElementList) {
        this.mediaElementList = mediaElementList;
    }

    protected Station(Parcel in) {
        number = in.readInt();
        longitude = in.readDouble();
        latitude = in.readDouble();
        title = in.readString();
        audioSrcPath = in.readString();
        imgSrcPath = in.readString();
        description = in.readString();
        if (in.readByte() == 0x01) {
            mediaElementList = new ArrayList<MediaElement>();
            in.readList(mediaElementList, MediaElement.class.getClassLoader());
        } else {
            mediaElementList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(title);
        dest.writeString(audioSrcPath);
        dest.writeString(imgSrcPath);
        dest.writeString(description);
        if (mediaElementList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mediaElementList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };


}
