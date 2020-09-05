package com.example.mapguide;

import android.app.Application;
import android.media.MediaPlayer;

public final class MediaPlayerSingle extends MediaPlayer {

    static MediaPlayerSingle instance;

    public MediaPlayerSingle(){}

    public static MediaPlayerSingle getInstance(){
        if(instance==null){
            instance = new MediaPlayerSingle();
        }
        return instance;
    }

}
