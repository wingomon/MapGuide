package com.example.mapguide;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
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
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StationEdit_Activity extends AppCompatActivity {

    TextView stationnumber;
    TextView description;
    Station station;
    ImageView addimageicon;
    String currentPhotoPath;
    Uri currentUri;
    ImageView stationImage;
    ImageView recordButton;
    boolean isRecording = false;
    private int PERMISSION_CODE=11;

    //Für Audio-Recorder
    private MediaRecorder mediarecorder;
    private Chronometer timer;
    private String recordFile;

    //Für Audio-Player
    private SeekBar seekbar;
    private ImageView playButton;
    private MediaPlayer mPlayer;
    private Handler mHandler;
    private Runnable mRunnable;

    private String tempAudioPath;
    private Uri tempAudioUri;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_edit_);

        context = getApplicationContext();

        //Station Werte aus Intent rausholen und in die "View" einfügen
        station = getIntent().getExtras().getParcelable("station");

        stationnumber = (TextView) findViewById(R.id.textViewStationNumber);
        stationnumber.setText(Integer.toString(station.getNumber()));

        description = (TextView) findViewById(R.id.stationDescription);
        description.setText(station.getDescription());

        stationImage = (ImageView) findViewById(R.id.stationImage);

        //Recorder
        recordButton = (ImageView)findViewById(R.id.recordButton);
        addimageicon = (ImageView) findViewById(R.id.addimageicon);
        addimageicon.setOnClickListener(this::onClick);
        recordButton.setOnClickListener(this::onClick);
        timer = findViewById(R.id.record_timer);

        //MediaPlayer
        playButton = (ImageView) findViewById(R.id.playButton);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        mHandler = new Handler();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Log.d("MediaPlayer", "Playbutton pressed.");
                // If media player another instance already running then stop it first
                stopPlaying();

                // Initialize media player
                mPlayer = MediaPlayer.create(context, tempAudioUri);

                // Start the media player
                mPlayer.start();
                // Get the current audio stats
                getAudioStats();
                // Initialize the seek bar
                initializeSeekBar();
            }
        });

        //Listener für die Seekbar initialisieren
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setMax(mPlayer.getDuration() / 1000);
                if(mPlayer!=null && fromUser){
                    mPlayer.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
                    recordButton.setBackgroundResource(R.drawable.microphone_off);
                    isRecording=false;
                }
                else {
                    //StartRecording
                    if(checkPermissions()){
                        startRecording();
                        recordButton.setBackgroundResource(R.drawable.microphone_on);
                        isRecording=true;
                    }
                }
                break;

        }
    }//ende onClick()

    protected void stopPlaying(){
        // If media player is not null then try to stop it
        if(mPlayer!=null){
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            if(mHandler!=null){
                mHandler.removeCallbacks(mRunnable);
            }
        }
    }

    protected void getAudioStats(){
        int duration  = mPlayer.getDuration()/1000; // In milliseconds
        int due = (mPlayer.getDuration() - mPlayer.getCurrentPosition())/1000;
        int pass = duration - due;

        /**
        mPass.setText("" + pass + " seconds");
        mDuration.setText("" + duration + " seconds");
        mDue.setText("" + due + " seconds");
         **/
    }

    protected void initializeSeekBar(){
        seekbar.setMax(mPlayer.getDuration()/1000);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(mPlayer!=null){
                    int mCurrentPosition = mPlayer.getCurrentPosition()/1000; // In milliseconds
                    seekbar.setProgress(mCurrentPosition);
                    getAudioStats();
                }
                mHandler.postDelayed(mRunnable,1000);
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }


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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_ss", Locale.GERMAN);
        Date now = new Date();
        recordFile = "MAPGUIDE_REC"+formatter.format(now)+".3gp";
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