package com.example.mapguide;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StationEdit_Activity extends AppCompatActivity {

    TextView stationnumber, missingImg;
    EditText description;
    EditText title;
    Station station;
    ImageView addimageicon;
    String currentPhotoPath;
    Uri currentUri;
    Button saveButton;
    ImageView uploadButton;
    ImageView stationImage;
    ImageView recordButton;
    boolean isRecording = false;
    private int PERMISSION_CODE=11;


    BitmapResizer bitmapResizer;
    int bitmapMaxWidth = 1000;
    int bitmapMaxHeight = 1000;

    //Delete Icon at Media Content in px
    int deleteIconWidth=100;
    int deleteIconHeight=100;

    //Add more media content
    ImageView addMedia;
    List<View> viewList;
    List<MediaElement> mediaElementList;
    LinearLayout linearLayout;
    ImagePicker imagePicker;
    CameraImagePicker cameraImagePicker;


    //Für Audio-Recorder
    private MediaRecorder mediarecorder;
    private Chronometer timer;
    private String recordFile;

    //Für Audio-Player
    private SeekBar seekbar;
    private ImageView playButton;
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
    private Context context;

    //Picking Audio from Gallery, Drive Variable
   AudioPicker audioPicker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_edit_);

        context = getApplicationContext();
        imagePicker = new ImagePicker(StationEdit_Activity.this);
        bitmapResizer = new BitmapResizer(bitmapMaxWidth,bitmapMaxHeight);


        missingImg = (TextView) findViewById(R.id.missingImg);
        mediaElementList = new ArrayList<>();
        linearLayout = (LinearLayout) findViewById(R.id.add_mediaContent);
        addMedia = (ImageView) findViewById(R.id.addmedia);
        addMedia.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                    addNewMedia(StationEdit_Activity.this);
            }
        });

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

                        //Enabling Click-Button
                        if(tempAudioPath == null || !(tempAudioPath.equals("null"))){
                            playButton.setAlpha(1f);
                            playButton.setClickable(true);
                            playButton.setEnabled(true);
                        }

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

                //Validation of EditText fields
                if (title.length() == 0 || description.length() == 0) {

                    if (title.length() == 0) {
                        title.setError("Dieses Feld darf nicht leer sein");
                    }

                    if (description.length() == 0) {
                        description.setError("Dieses Feld darf nicht leer sein");
                    }

                } else {
                    //Set Station Values to pass to next intent
                    station.setImgSrcPath(currentPhotoPath);
                    station.setTitle(title.getText().toString());
                    station.setDescription(description.getText().toString());

                    if (station.getAudioSrcPath() != null) {
                        station.setAudioSrcPath(tempAudioPath);
                        Log.d("--AUDIO--", "Saved Audiosourcepfad:" + tempAudioPath);
                        Log.d("--AUDIO--", "Saved Audiosourcepfad:" + tempAudioUri);
                    }

                    //Iterating through the "More Media Content"-View (linearLayout with id addmediacontent) to get all Views and save them in a list
                    if (mediaElementList.size() > 0) {
                        mediaElementList.clear();
                    }

                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                        ViewGroup vg = (ViewGroup) linearLayout.getChildAt(i);
                        //The first Element of the respective linearLayout has ImageView or EditText as first child
                        if (vg.getChildAt(0) instanceof EditText) {

                            //Get Text of EditText-Element
                            String text = ((EditText) vg.getChildAt(0)).getText().toString();
                            MediaElement mText = new MediaElement("TEXT", text);
                            mediaElementList.add(mText);
                        }
                        //If it is not a EditText-Element, it has to be an "IMAGE"
                        else {
                            String imgPath = ((ImageView) vg.getChildAt(0)).getTag().toString();
                            MediaElement mImage = new MediaElement("IMAGE", imgPath);
                            mediaElementList.add(mImage);
                            Log.d("--FOR-LOOP--MediaElements", "Saved ImagePath is:" + imgPath);
                        }
                    }

                    if (mediaElementList.size() > 0) {
                        station.setMediaElementList(mediaElementList);
                    }


                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("station", (Parcelable) station);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        stationnumber = (TextView) findViewById(R.id.textViewStationNumber);
        stationnumber.setText(Integer.toString(station.getNumber()));

        description = (EditText) findViewById(R.id.stationDescription);
        description.setText(station.getDescription());

        title = (EditText) findViewById(R.id.stationTitle);
        title.setText(station.getTitle());



        stationImage = (ImageView) findViewById(R.id.stationImage);

        if(station.getImgSrcPath() != null && !(station.getImgSrcPath().equals("null"))){
            //stationImage.setImageBitmap(BitmapFactory.decodeFile(station.getImgSrcPath()));
            stationImage.setImageBitmap(bitmapResizer.transform(BitmapFactory.decodeFile(station.getImgSrcPath())));
            currentPhotoPath = station.getImgSrcPath();
            missingImg.setVisibility(View.INVISIBLE);
        }

        if(station.getAudioSrcPath() != null){
            tempAudioPath = station.getAudioSrcPath();
            tempAudioUri = Uri.parse(tempAudioPath);
        }

        if(station.getMediaElementList() != null) {
            if(station.getMediaElementList().size()>0)
            initMediaContentView(station.getMediaElementList());
        }

        addimageicon = (ImageView) findViewById(R.id.addimageicon);
        addimageicon.setOnClickListener(this::onClick);


        //Recorder
        recordButton = (ImageView)findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this::onClick);
        timer = findViewById(R.id.record_timer);

        //MediaPlayer
        mPlayer = MediaPlayerSingle.getInstance();
        playButton = (ImageView) findViewById(R.id.playButton);
        if(station.getAudioSrcPath() == null || station.getAudioSrcPath().equals("null")){
            playButton.setAlpha(0.5f);
            playButton.setClickable(false);
            playButton.setEnabled(false);
        }
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
            @SuppressLint("ClickableViewAccessibility")
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
                selectImage();
                break;

            case R.id.recordButton:
                if(isRecording){

                    //Stop Recording
                    stopRecording();
                    //Wenn er aufnimmt, dann Button blinkend anzeigen
                    recordButton.setImageResource(R.drawable.microphone_off);
                    isRecording=false;

                    //Enabling PlayButton
                    if(tempAudioPath == null || !(tempAudioPath.equals("null"))){
                        playButton.setAlpha(1f);
                        playButton.setClickable(true);
                        playButton.setEnabled(true);
                    }
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


    /**
     * Stop the Recording
     */
    private void stopRecording(){
        timer.stop();
        mediarecorder.stop();
        mediarecorder.release();
        mediarecorder = null;
        Log.d("MediaRecorder", "Stopped");
    }

    /**
     * Start Recording
     */

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

    /**
     * Check Permissions for Recording Audio
     * @return
     */
    private boolean checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            return false;
        }
    }

    /**
     * Add multiple Media Views to the Station
     * It is possible to add an EditText View or Image View by
     * selecting the corresponding option
     * @param context
     */

    private void addNewMedia(Context context){
        final CharSequence[] mediaOptions = {"Text", "Foto aus Bibliothek auswählen", "Foto aufnehmen"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setTitle("Füge weitere Medien hinzu");

        builder.setItems(mediaOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int viewId = ViewCompat.generateViewId();
                Log.d("--Generate View ID--",Integer.toString(viewId));

                if(mediaOptions[which].equals("Text")){
                    //Create a EditText programmatically as Child of a Linear Layout
                    RelativeLayout relativeLayoutText = new RelativeLayout(context);
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    relativeLayoutText.setLayoutParams(rlp);

                    EditText textfield = new EditText(getBaseContext());
                    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,300);
                    layoutParams.setMargins(0,0,0,10);
                    textfield.setLayoutParams(layoutParams);
                    textfield.setVerticalScrollBarEnabled(true);
                    textfield.setSingleLine(false);
                    textfield.setHint("Füge einen Text hinzu");
                    textfield.setId(R.id.edittext);
                    textfield.setTextColor(getResources().getColor(R.color.colorPrimary));
                    textfield.setBackgroundResource(R.drawable.edit_text_rounded);
                    textfield.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    textfield.setPadding(20,20,20,20);
                    textfield.setGravity(Gravity.START);
                    Typeface type = ResourcesCompat.getFont(getApplicationContext(),R.font.airbnbcereallight);
                    textfield.setTypeface(type);
                    textfield.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
                    relativeLayoutText.addView(textfield);
                    linearLayout.addView(relativeLayoutText);

                    //Also add a Button to delete this EditText View again
                    ImageView btnDelete = new ImageView(getBaseContext());
                    RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(deleteIconWidth,deleteIconHeight);
                    buttonParam.setMargins(10,10,10,10);
                    buttonParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    btnDelete.setLayoutParams(buttonParam);
                    btnDelete.setClickable(true);
                    relativeLayoutText.addView(btnDelete);
                    Picasso.get().load(R.drawable.icons8_cancel_100).into(btnDelete);
                    // btnDelete.setImageResource(R.drawable.icons8_cancel_100);
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            linearLayout.removeView(relativeLayoutText);
                        }
                    });

                    //Add this View to the ViewList
                  //  MediaElement m = new MediaElement(viewId, "TEXT", null);

                } else if(mediaOptions[which].equals("Foto aus Bibliothek auswählen")){
                    imagePicker = new ImagePicker(StationEdit_Activity.this);
                    imagePicker.setImagePickerCallback(new ImagePickerCallback() {
                        @Override
                        public void onImagesChosen(List<ChosenImage> list) {
                            RelativeLayout relativeLayoutImage = new RelativeLayout(context);
                            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            relativeLayoutImage.setLayoutParams(rlp);
                            //Create new ImageView programmatically with picked image
                            ChosenImage c1 = list.get(0);
                            String imagePath = c1.getOriginalPath();
                            ImageView imgView = new ImageView(StationEdit_Activity.this);
                            imgView.setTag(imagePath);
                            final RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500);
                            layoutParams1.setMargins(0,0,0,10);
                            imgView.setLayoutParams(layoutParams1);
                            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            relativeLayoutImage.addView(imgView);
                            linearLayout.addView(relativeLayoutImage);
                            /**
                            Picasso.get().load(new File(imagePath))
                                    .transform(new BitmapResizer(bitmapMaxWidth,bitmapMaxHeight))
                                    .into(imgView);**/
                            imgView.setImageBitmap(bitmapResizer.transform(BitmapFactory.decodeFile(imagePath)));
                            //Also add a Button to delete this EditText View again
                            ImageView btnDelete = new ImageView(getBaseContext());
                            RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(deleteIconWidth,deleteIconHeight);
                            buttonParam.setMargins(10,10,10,10);
                            buttonParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            btnDelete.setLayoutParams(buttonParam);
                            btnDelete.setClickable(true);
                            relativeLayoutImage.addView(btnDelete);
                            Picasso.get().load(R.drawable.icons8_cancel_100).into(btnDelete);
                            //btnDelete.setImageResource(R.drawable.icons8_cancel_100);
                            btnDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    linearLayout.removeView(relativeLayoutImage);
                                }
                            });
                        }

                        @Override
                        public void onError(String s) {

                        }
                    });
                    imagePicker.pickImage();
                } else {
                    cameraImagePicker = new CameraImagePicker(StationEdit_Activity.this);
                    cameraImagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                                 @Override
                                                                 public void onImagesChosen(List<ChosenImage> list) {
                                                                     RelativeLayout relativeLayoutImage = new RelativeLayout(context);
                                                                     RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                     relativeLayoutImage.setLayoutParams(rlp);
                                                                     //Create new ImageView programmatically with picked image
                                                                     ChosenImage c1 = list.get(0);
                                                                     String imagePath = c1.getOriginalPath();
                                                                     ImageView imgView = new ImageView(StationEdit_Activity.this);
                                                                     imgView.setTag(imagePath);
                                                                     final RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500);
                                                                     layoutParams1.setMargins(0,0,0,10);
                                                                     imgView.setLayoutParams(layoutParams1);
                                                                     imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                                     relativeLayoutImage.addView(imgView);
                                                                     linearLayout.addView(relativeLayoutImage);
                                                                     Picasso.get().load(new File(imagePath))
                                                                             .transform(new BitmapResizer(bitmapMaxWidth,bitmapMaxHeight))
                                                                             .into(imgView);
                                                                     //Also add a Button to delete this EditText View again
                                                                     ImageView btnDelete = new ImageView(getBaseContext());
                                                                     RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(deleteIconWidth,deleteIconHeight);
                                                                     buttonParam.setMargins(10,10,10,10);
                                                                     buttonParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                                                     btnDelete.setLayoutParams(buttonParam);
                                                                     btnDelete.setClickable(true);
                                                                     relativeLayoutImage.addView(btnDelete);
                                                                     Picasso.get().load(R.drawable.icons8_cancel_100).into(btnDelete);
                                                                     //btnDelete.setImageResource(R.drawable.icons8_cancel_100);
                                                                     btnDelete.setOnClickListener(new View.OnClickListener() {
                                                                         @Override
                                                                         public void onClick(View v) {
                                                                             linearLayout.removeView(relativeLayoutImage);
                                                                         }
                                                                     });
                                                                 }

                                                                 @Override
                                                                 public void onError(String message) {
                                                                     // Do error handling
                                                                 }
                                                             }
                    );
                    // imagePicker.shouldGenerateMetadata(false); // Default is true
                    // imagePicker.shouldGenerateThumbnails(false); // Default is true
                    cameraImagePicker.pickImage();
                }

            }
        });
        builder.show();
    }


    /**
     * Select Image
     * Possible to choose between
     * Gallery or Camera
     *
     */

    private void selectImage(){
        final CharSequence[] mediaOptions = {"Aus Bibliothek auswählen", "Foto aufnehmen","Abbrechen"};
        AlertDialog.Builder builder = new AlertDialog.Builder(StationEdit_Activity.this, R.style.CustomAlertDialog);
        builder.setTitle("Füge ein Foto hinzu");

        builder.setItems(mediaOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(mediaOptions[which].equals("Aus Bibliothek auswählen")) {

                    imagePicker = new ImagePicker(StationEdit_Activity.this);
                    imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                           @Override
                                                           public void onImagesChosen(List<ChosenImage> images) {
                                                               // Adapt picture to imageView
                                                               currentPhotoPath = images.get(0).getOriginalPath();
                                                               //stationImage.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                                                               stationImage.setImageBitmap(bitmapResizer.transform(BitmapFactory.decodeFile(currentPhotoPath)));
                                                               missingImg.setVisibility(View.GONE);
                                                           }

                                                           @Override
                                                           public void onError(String message) {
                                                               // Do error handling
                                                           }
                                                       }
                    );
                    imagePicker.pickImage();

                } else if(mediaOptions[which].equals("Foto aufnehmen")){
                    cameraImagePicker = new CameraImagePicker(StationEdit_Activity.this);
                    cameraImagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                                 @Override
                                                                 public void onImagesChosen(List<ChosenImage> images) {
                                                                     // Display images
                                                                     // Adapt picture to imageView
                                                                     currentPhotoPath = images.get(0).getOriginalPath();
                                                                     stationImage.setImageBitmap(bitmapResizer.transform(BitmapFactory.decodeFile(currentPhotoPath)));
                                                                     missingImg.setVisibility(View.GONE);
                                                                 }

                                                                 @Override
                                                                 public void onError(String message) {
                                                                     // Do error handling
                                                                 }
                                                             }
                    );
                    // imagePicker.shouldGenerateMetadata(false); // Default is true
                    // imagePicker.shouldGenerateThumbnails(false); // Default is true
                    currentPhotoPath = cameraImagePicker.pickImage();
                } else if(mediaOptions[which].equals("Abbrechen")){
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }



    /**
     * On Activity Result
     * After e.g. Picking Audio or Image return to current Activity and
     * submit data
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("-------111111HAKLO----","---RESULTCODE:" + resultCode + "-------REQUESTCODE:" + requestCode+"------DATA"+data+ "-DAS BILD SOLLTE AUF DEN PFAD GESETZT WERDEN:" + currentPhotoPath );

        if (requestCode == Picker.PICK_AUDIO && resultCode == RESULT_OK) {
            audioPicker.submit(data);
        }

         else if (requestCode == Picker.PICK_IMAGE_DEVICE && resultCode == RESULT_OK){
            imagePicker.submit(data);
        }
        else if(requestCode == Picker.PICK_IMAGE_CAMERA) {
            cameraImagePicker.submit(data);
        }

    }



    private void initMediaContentView(List<MediaElement> mediaElementList){
        for(MediaElement m : mediaElementList){
            if(m.getType().equals("TEXT")){
                RelativeLayout relativeLayoutText = new RelativeLayout(this);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(10,10,10,10);
                relativeLayoutText.setLayoutParams(rlp);

                EditText textfield = new EditText(getBaseContext());
                final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,300);
                textfield.setLayoutParams(layoutParams);
                textfield.setVerticalScrollBarEnabled(true);
                textfield.setSingleLine(false);
                textfield.setHint("Füge einen Text hinzu");
                textfield.setId(R.id.edittext);
                textfield.setText(m.getStore());
                textfield.setTextColor(getResources().getColor(R.color.colorPrimary));
                textfield.setBackgroundResource(R.drawable.edit_text_rounded);
                textfield.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                textfield.setPadding(10,10,20,10);
                textfield.setGravity(Gravity.START);
                Typeface type = ResourcesCompat.getFont(getApplicationContext(),R.font.airbnbcereallight);
                textfield.setTypeface(type);
                textfield.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
                relativeLayoutText.addView(textfield);
                linearLayout.addView(relativeLayoutText);

                //Also add a Button to delete this EditText View again
                ImageView btnDelete = new ImageView(getBaseContext());
                RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(deleteIconWidth,deleteIconHeight);
                buttonParam.setMargins(10,10,10,10);
                buttonParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                btnDelete.setLayoutParams(buttonParam);
                btnDelete.setClickable(true);
                relativeLayoutText.addView(btnDelete);
                Picasso.get().load(R.drawable.icons8_cancel_100).into(btnDelete);
               // btnDelete.setImageResource(R.drawable.icons8_cancel_100);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linearLayout.removeView(relativeLayoutText);
                    }
                });
            } else {

                RelativeLayout relativeLayoutImage = new RelativeLayout(this);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(10,10,10,10);
                relativeLayoutImage.setLayoutParams(rlp);
                //Create new ImageView programmatically with picked image
                String imagePath = m.getStore();
                ImageView imgView = new ImageView(StationEdit_Activity.this);
                imgView.setTag(imagePath);
                final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500);
                imgView.setLayoutParams(layoutParams1);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                relativeLayoutImage.addView(imgView);
                linearLayout.addView(relativeLayoutImage);
                //Picasso.get().load(new File(imagePath)).into(imgView);
                imgView.setImageBitmap(bitmapResizer.transform(BitmapFactory.decodeFile(imagePath)));
                //Also add a Button to delete this EditText View again
                ImageView btnDelete = new ImageView(getBaseContext());
                RelativeLayout.LayoutParams buttonParam = new RelativeLayout.LayoutParams(deleteIconWidth,deleteIconHeight);
                buttonParam.setMargins(10,10,10,10);
                buttonParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                btnDelete.setLayoutParams(buttonParam);
                btnDelete.setClickable(true);
                relativeLayoutImage.addView(btnDelete);
                Picasso.get().load(R.drawable.icons8_cancel_100).into(btnDelete);
                //btnDelete.setImageResource(R.drawable.icons8_cancel_100);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linearLayout.removeView(relativeLayoutImage);
                    }
                });

            }
        }
    }



    @Override
    public void onBackPressed(){
        Log.d("Station_edit_Activity","Back Button was pressed");
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder1.setTitle("Änderungen verwerfen?");
        builder1.setMessage("Wenn du jetzt zurückgehst, verlierst du deine Änderungen.");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Änderungen verwerfen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                StationEdit_Activity.super.onBackPressed();
            }
        });
        builder1.setNeutralButton("Weiter bearbeiten",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }



}