package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class StationViewActivity extends AppCompatActivity {

    TextView title, description, number;
    ImageView image, cardIcon;
    Button playButton;
    private Context context;

    //Für Audio-Player
    private SeekBar seekbar;
    private MediaPlayer mPlayer;
    private Handler mHandler = new Handler();
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration = mPlayer.getCurrentPosition();
            //Text hier setzen
            //textCurrentTime.setText(milliSecondsToTimer(currentDuration));
        }
    };

    private String tempAudioPath;
    private Uri tempAudioUri;

    Station station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context =getApplicationContext();
        setContentView(R.layout.activity_station_view);
        number = (TextView) findViewById(R.id.textViewStationNumber);
        title = (TextView) findViewById(R.id.textViewStationTitle);
        description = (TextView) findViewById(R.id.textViewDescription);
        image = (ImageView) findViewById(R.id.img);
        cardIcon = (ImageView) findViewById(R.id.imageViewCardIcon);

        cardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StationMapView.class);
                intent.putExtra("station",(Parcelable)station);
                intent.putExtra("stationList", getIntent().getSerializableExtra("stationList"));
                startActivity(intent);
            }
        });


        //Stations-Objekt aus vorheriger Activity entnehmen
        station = getIntent().getExtras().getParcelable("station");
        tempAudioPath = station.getAudioSrcPath();

        title.setText(station.getTitle());
        description.setText(station.getDescription());
        number.setText(Integer.toString(station.getNumber()));

        if(station.getImgSrcPath() != null || !(station.getImgSrcPath().equals("null"))) {
            Picasso.get().load(station.getImgSrcPath()).into(image);
        }


        //MediaPlayer
        mPlayer = new MediaPlayer();
        playButton= (Button) findViewById(R.id.controlPlay);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        mHandler = new Handler();
        seekbar.setMax(100);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPlayer.isPlaying()){
                    mHandler.removeCallbacks(updater);
                    mPlayer.pause();
                    //playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else {
                    mPlayer = MediaPlayer.create(context, Uri.parse(tempAudioPath));
                    mPlayer.start();

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            seekbar.setProgress(0);
                           // playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                            mPlayer.reset();
                            prepareMediaPlayer();
                            Log.d("MediaPlayer", "Completed");

                        }
                    });

                    //playButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    updateSeekBar();
                }
            }
        });

        //Listener für die Seekbar initialisieren
        seekbar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mPlayer.getDuration()/100) * seekBar.getProgress();
                mPlayer.seekTo((playPosition));
                return false;
            }
        });

        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                seekbar.setSecondaryProgress(percent);
            }
        });

    }


    private  void prepareMediaPlayer(){
        try {
            if(station.getAudioSrcPath() == null || station.getAudioSrcPath().equals("null")){
                mPlayer.setDataSource(context, Uri.fromFile(new File(tempAudioPath)));
            } else {
                mPlayer.setDataSource(context, Uri.fromFile(new File(station.getAudioSrcPath())));
            }
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSeekBar(){
        if(mPlayer.isPlaying()){
            seekbar.setProgress((int) (((float) mPlayer.getCurrentPosition() / mPlayer.getDuration())*100));
            mHandler.postDelayed(updater, 1000);
        }
    }

}