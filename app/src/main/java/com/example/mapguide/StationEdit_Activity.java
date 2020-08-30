package com.example.mapguide;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StationEdit_Activity extends AppCompatActivity {

    TextView stationnumber;
    EditText description;
    EditText title;
    Station station;
    ImageView addimageicon;
    TextView addimagetext;
    String currentPhotoPath;
    Uri currentUri;
    Button saveButton;
    ImageView uploadButton;
    ImageView stationImage;
    ImageView recordButton;
    boolean isRecording = false;
    private int PERMISSION_CODE=11;

    List<View> mediaViewList;

    //Für Audio-Recorder
    private MediaRecorder mediarecorder;
    private Chronometer timer;
    private String recordFile;

    //Für Audio-Player
    private SeekBar seekbar;
    private ImageView playButton;
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
    private Context context;

   AudioPicker audioPicker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_edit_);

        context = getApplicationContext();

        audioPicker = new AudioPicker(this);
        uploadButton = (ImageView) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                audioPicker.setAudioPickerCallback(new AudioPickerCallback() {
                    @Override
                    public void onAudiosChosen(List<ChosenAudio> list) {
                        ChosenAudio c1 = list.get(0);
                        tempAudioPath = c1.getOriginalPath();
                        Log.d("--AUDIPFAD FILEPICKER--",c1.getOriginalPath());
                        tempAudioUri = Uri.parse(tempAudioPath);

                    }

                    @Override
                    public void onError(String s) {
                        Log.d("Error","Error picking audio");
                    }
                });

                audioPicker.pickAudio();


            }
        });


        //Station Werte aus Intent rausholen und in die "View" einfügen
        station = getIntent().getExtras().getParcelable("station");

        //Speichern und Stations Werte setzen, dann zurück zur anderen Activity springen
        saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                station.setImgSrcPath(currentPhotoPath);
                station.setTitle(title.getText().toString());
                station.setDescription(description.getText().toString());

                if(station.getAudioSrcPath() != null) {
                    station.setAudioSrcPath(tempAudioPath);
                    Log.d("--AUDIO--", "Saved Audiosourcepfad:" + tempAudioPath);
                    Log.d("--AUDIO--", "Saved Audiosourcepfad:" + tempAudioUri);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("station", (Parcelable) station);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        stationnumber = (TextView) findViewById(R.id.textViewStationNumber);
        stationnumber.setText(Integer.toString(station.getNumber()));

        description = (EditText) findViewById(R.id.stationDescription);
        description.setText(station.getDescription());

        title = (EditText) findViewById(R.id.stationTitle);
        title.setText(station.getTitle());



        stationImage = (ImageView) findViewById(R.id.stationImage);
        if(station.getImgSrcPath() != null){
            stationImage.setImageBitmap(BitmapFactory.decodeFile(station.getImgSrcPath()));
            currentPhotoPath = station.getImgSrcPath();
        }

        if(station.getAudioSrcPath() != null){
            tempAudioPath = station.getAudioSrcPath();
            tempAudioUri = Uri.parse(tempAudioPath);
        }

        addimageicon = (ImageView) findViewById(R.id.addimageicon);
        addimageicon.setOnClickListener(this::onClick);
        addimagetext = (TextView) findViewById(R.id.addImageText);
        if(addimageicon.getDrawable() == null){ addimageicon.setAlpha(0f); addimagetext.setAlpha(0f);}

        //Recorder
        recordButton = (ImageView)findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this::onClick);
        timer = findViewById(R.id.record_timer);

        //MediaPlayer
        mPlayer = new MediaPlayer();
        playButton = (ImageView) findViewById(R.id.playButton);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        mHandler = new Handler();
        seekbar.setMax(100);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPlayer.isPlaying()){
                    mHandler.removeCallbacks(updater);
                    mPlayer.pause();
                    playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else {
                    // Initialize media player with new audio
                    if(station.getAudioSrcPath() == null || station.getAudioSrcPath().equals("null")) {
                        mPlayer = MediaPlayer.create(context, Uri.fromFile(new File(tempAudioPath)));
                    } else{
                        mPlayer = MediaPlayer.create(context, Uri.fromFile(new File(station.getAudioSrcPath())));
                    }
                    // Start the media player
                    mPlayer.start();

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            seekbar.setProgress(0);
                            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                            mPlayer.reset();
                            prepareMediaPlayer();
                            Log.d("MediaPlayer", "Completed");

                        }
                    });

                    playButton.setImageResource(R.drawable.ic_baseline_pause_24);
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


    }// Ende OnCreate()


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

    //Button Kontroller
    public void onClick(View v){
        switch(v.getId()){
            case R.id.addimageicon:
                selectImage(StationEdit_Activity.this);
                break;

            case R.id.recordButton:
                if(isRecording){

                    //Stop Recording
                    stopRecording();
                    //Wenn er aufnimmt, dann Button blinkend anzeigen
                    recordButton.setImageResource(R.drawable.microphone_off);
                    isRecording=false;
                }
                else {
                    //StartRecording
                    if(checkPermissions()){
                        startRecording();
                        recordButton.setImageResource(R.drawable.microphone_on);
                        isRecording=true;
                    }
                }
                break;

        }
    }//ende onClick()


    private void stopRecording(){
        timer.stop();
        mediarecorder.stop();
        mediarecorder.release();
        mediarecorder = null;
        Log.d("MediaRecorder", "Stopped");
    }

    private void startRecording(){

        //!!!!!!!!!!!!!!Wenn man eine Aufnahme startet, soll Seekbar & Playbutton disabled werden!!

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        String recordPath = this.getExternalFilesDir("/").getAbsolutePath();

       // SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_ss", Locale.GERMAN);
        //Date now = new Date();
        recordFile = "MAPGUIDE_REC_STATION_"+Integer.toString(station.getNumber())+".3gp";
        mediarecorder = new MediaRecorder();
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediarecorder.setOutputFile(recordPath+"/"+recordFile);
        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Setze Pfad/Uri, damit man danach auf die Datei zugreifen kann
        tempAudioPath = recordPath + "/" +  recordFile;
        tempAudioUri = Uri.fromFile(new File(tempAudioPath));
        Log.d("RecordPath","RecordPath is" + tempAudioPath);

        try {
            mediarecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediarecorder.start();

        Log.d("MediaRecorder", "Started");

    }

    private boolean checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            return false;
        }
    }


    private void selectImage(Context context) {
        final CharSequence[] options = { "Foto aufnehmen", "Aus Bibliothek auswählen","Abbrechen" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setTitle("Füge ein Foto hinzu");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Foto aufnehmen")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //  startActivityForResult(takePicture, 0);
                    Log.i("------You-------","------------Clicked on TAKE PHOTO---------" + currentPhotoPath);

                    if (takePicture.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            Log.i("---TRy---","---TO CREATE IMAGE");
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.i("---FAIL---","FILE COULD NOT BE CREATED");
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    BuildConfig.APPLICATION_ID+".provider",
                                    photoFile);
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                            //----THIS HAS BEEN ADDED
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                takePicture.setClipData(ClipData.newRawUri("", photoURI));
                                takePicture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }


                            Log.i("------YOOO---",currentPhotoPath);

                            startActivityForResult(takePicture, 0);
                        }
                    }

                } else if (options[item].equals("Aus Bibliothek auswählen")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Abbrechen")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("-------111111HAKLO----","---RESULTCODE:" + resultCode + "-------REQUESTCODE:" + requestCode+"------DATA"+data+ "-DAS BILD SOLLTE AUF DEN PFAD GESETZT WERDEN:" + currentPhotoPath );

        if (requestCode == Picker.PICK_AUDIO && resultCode == RESULT_OK) {
            audioPicker.submit(data);
        }

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {

                        /**

                        //Wenn Bild ausgewählt wurde, dann aktiviere den "Weiter"-Button
                        next = (Button) findViewById(R.id.button4);
                        next.setEnabled(true);
                        next.setAlpha(1f);
                         **/
                        // und blende das "Hinzufügen" Icon aus
                        addimageicon.setAlpha(0f);
                        addimagetext.setAlpha(0f);

                        //Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        //String imgPath = createImageFromBitmap(selectedImage);
                        //imageView.setImageBitmap(selectedImage);
                        stationImage.setImageURI(Uri.parse(currentPhotoPath));
                        Log.i("---HAKLO----","-----------------------DAS BILD SOLLTE AUF DEN PFAD GESETZT WERDEN:" + currentPhotoPath);
                        /**
                         Bundle extras = data.getExtras();
                         Bitmap imageBitmap = (Bitmap) extras.get("data");
                         imageView.setImageBitmap(imageBitmap);**/

                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        currentUri = selectedImage;
                        Log.i("----FilePath----",getRealPathFromURI(currentUri));

                        /**
                        //Wenn Bild ausgewählt wurde, dann aktiviere den "Weiter"-Button
                        next = (Button) findViewById(R.id.button4);
                        next.setEnabled(true);
                        next.setAlpha(1f);
                         **/
                        // und blende das "Hinzufügen" Icon aus
                        addimageicon.setAlpha(0f);
                        addimagetext.setAlpha(0f);

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);

                            //Wenn Bild ausgewählt ist, dann speichere den Pfad in die Variable "currentPhotoPath"
                            currentPhotoPath=getRealPathFromURI(currentUri);



                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);


                                //Berechtigungen für Zugriff auf den Speicher

                                stationImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "TakeCameraPhoto";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

}