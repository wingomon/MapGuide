package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StationViewActivity extends AppCompatActivity {

    TextView title, description, number;
    ImageView image, cardIcon;
    Button playButton, nextButton, backButton;
    private Context context;

    //Für Audio-Player
    private SeekBar seekbar;
    private static MediaPlayer mPlayer;
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
    List<Station> stationList = new ArrayList<>();
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context =getApplicationContext();
        setContentView(R.layout.activity_station_view);
        number = (TextView) findViewById(R.id.textViewStationNumber);
        title = (TextView) findViewById(R.id.textViewStationTitle);
        description = (TextView) findViewById(R.id.textViewDescription);
        image = (ImageView) findViewById(R.id.img);

        linearLayout = (LinearLayout) findViewById(R.id.add_mediaContent);

        cardIcon = (ImageView) findViewById(R.id.imageViewCardIcon);

        cardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StationMapView.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("station",(Parcelable)station);
                intent.putExtra("stationList", getIntent().getSerializableExtra("stationList"));
                startActivity(intent);
            }
        });


        //StationList from previous Activity
        stationList = (List<Station>) getIntent().getSerializableExtra("stationList");

        //Stations-Objekt aus vorheriger Activity entnehmen
        station = getIntent().getExtras().getParcelable("station");

        tempAudioPath = station.getAudioSrcPath();

        title.setText(station.getTitle());
        description.setText(station.getDescription());
        number.setText(Integer.toString(station.getNumber()));

        if(station.getImgSrcPath() != null || !(station.getImgSrcPath().equals("null"))) {
            Picasso.get().load(station.getImgSrcPath()).into(image);
        }

        loadMediaContent();
        mPlayer = MediaPlayerSingle.getInstance();

        playButton= (Button) findViewById(R.id.controlPlay);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        mHandler = new Handler();
        seekbar.setMax(100);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //MediaPlayer
                if(mPlayer.isPlaying()){
                    mHandler.removeCallbacks(updater);
                    mPlayer.pause();
                    //playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else {

                    try {
                        mPlayer.reset();
                        mPlayer.setDataSource(context, Uri.parse(tempAudioPath));
                        mPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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


        //Back und Next Button Control to previous or next Station
        backButton = (Button) findViewById(R.id.backButton);
        nextButton = (Button) findViewById(R.id.nextButton);

        //Set Visibility of Back and Next Control accordingly to current Station
        setVisibilityOfControl();

        //Set onClickListener to Back and Next Control to navigating to desired Station via Control
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                station = stationList.get(station.getNumber()-2);
                Intent intent = new Intent(context, StationViewActivity.class);
                intent.putExtra("station",(Parcelable)station);
                intent.putExtra("stationList",(Serializable) stationList);
                startActivity(intent);
                finish();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                station = stationList.get(station.getNumber());
                Intent intent = new Intent(context, StationViewActivity.class);
                intent.putExtra("station",(Parcelable)station);
                intent.putExtra("stationList",(Serializable) stationList);
                startActivity(intent);
                finish();

            }
        });
    }//end onCreate()


    private void setVisibilityOfControl(){
        if(station.getNumber()==1){
            backButton.setVisibility(View.INVISIBLE);
            backButton.setEnabled(false);
        } else {
            backButton.setVisibility(View.VISIBLE);
            backButton.setEnabled(true);
        }

        if(station.getNumber() == stationList.size()){
            nextButton.setVisibility(View.INVISIBLE);
            nextButton.setEnabled(false);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setEnabled(true);
        }
    }

    private void loadMediaContent(){

        if(station != null){
            if(station.getMediaElementList() != null){
                if(station.getMediaElementList().size() > 0){

                    //Set Header "Weitere Medien" if media files exists
                    TextView textViewMedia = (TextView) findViewById(R.id.textViewMedia);
                    textViewMedia.setVisibility(View.VISIBLE);

                    for(int i=0; i<station.getMediaElementList().size(); i++){
                        if(station.getMediaElementList().get(i).getType().equals("IMAGE")){
                            //Create new ImageView programmatically with picked image
                            String imagePath = station.getMediaElementList().get(i).getStore();
                            Log.d("--ImageFilePicker-Path:--",imagePath);
                            Uri imgUri = Uri.parse(imagePath);
                            ImageView imgView = new ImageView(StationViewActivity.this);
                            final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500);
                            layoutParams1.setMargins(20,20,20,20);
                            imgView.setLayoutParams(layoutParams1);
                            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            linearLayout.addView(imgView);
                            Picasso.get().load(imagePath).into(imgView);
                        }
                        else {
                            String text = station.getMediaElementList().get(i).getStore();
                            if (text != null && !(text.equals("null"))){
                                TextView textfield = new TextView(getBaseContext());
                                final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                textfield.setLayoutParams(layoutParams);
                                textfield.setText(text);
                                textfield.setTextColor(getResources().getColor(R.color.colorPrimary));
                                textfield.setPadding(10,5,10,5);
                                textfield.setGravity(Gravity.CENTER);
                                Typeface type = ResourcesCompat.getFont(getApplicationContext(),R.font.airbnbcereallight);
                                textfield.setTypeface(type);
                                textfield.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
                                linearLayout.addView(textfield);
                            }

                        }

                    }
                }
            }
        }

    }

    private  void prepareMediaPlayer(){
        try {
            if(station.getAudioSrcPath() == null || station.getAudioSrcPath().equals("null")){
                mPlayer.setDataSource(context, Uri.parse(tempAudioPath));
            } else {
                mPlayer.setDataSource(context, Uri.parse(station.getAudioSrcPath()));
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