package com.example.mapguide;

public class Station {

    public int number;
    public double longitude, latitude;
    public String title;
    public String audioSrcPath;
    public String imgSrcPath;
    public String description;

    public Station(int number, double longitude, double latitude, String title, String audioSrcPath, String imgSrcPath, String description) {
        this.number = number;
        this.longitude = longitude;
        this.latitude = latitude;
        this.title = title;
        this.audioSrcPath = audioSrcPath;
        this.imgSrcPath = imgSrcPath;
        this.description = description;
    }


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
}
