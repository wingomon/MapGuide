package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StartCreateGuide_Overview extends AppCompatActivity {

    ImageView imageView;
    String imgPath;
    EditText title;
    EditText description;
    Button save;
    ImageView addStation_overview;
    ImageView editButton;
    private Spinner spinner;
    private static final String[] category = {"Sonstige","Städte","Natur","Museum", "Abenteuer"};

    String urlNoUploadPrefix = "https://firebasestorage.googleapis.com/";

    private FirebaseAuth mAuth;


    BitmapResizer bitmapResizer;
    int maxwidth = 1000;
    int maxHeight = 1000;

    ImagePicker imagePicker;
    CameraImagePicker cameraImagePicker;


    //Liste der Stationen | RecyclerView Variablen
    List<Station> stationList;
    RecyclerView recyclerView;
    StationAdapter_noEdit stationAdapter;
    Station tempStation;
    Context mContext;

    String location;
    Multimediaguide tempGuide;

    //Database
    DatabaseReference ref;
    private StorageReference mStorageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__overview);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Database connection
        ref= FirebaseDatabase.getInstance().getReference().child("guides");
        ref.keepSynced(true);

        mContext = (Context) this;


        //BitmaResizer initialisieren
        bitmapResizer = new BitmapResizer(maxwidth,maxHeight);

        //StationListe initialisieren
        stationList = new ArrayList<Station>();

        recyclerView = (RecyclerView) findViewById(R.id.stationRecyclerView1);
        stationAdapter =new StationAdapter_noEdit(stationList,this);
        RecyclerView.LayoutManager sLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(sLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(stationAdapter);

       //Spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StartCreateGuide_Overview.this,
                android.R.layout.simple_spinner_item,category);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);



        editButton = (ImageView) findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        //Bekomme Daten aus vorheriger Activity
        //Dabei gibt es zwei Möglichkeiten: User kommt von "Create"-Activity oder möchte seinen Guide bearbeiten und kommt von der UserPage
        // type kann "create" oder "edit" sein

        String type= getIntent().getStringExtra("type");
        title = (EditText) findViewById(R.id.etTitle);
        description = (EditText) findViewById(R.id.etDescription);
        imageView = (ImageView) findViewById(R.id.imageViewTitle);


        if(type.equals("create")) {

            title.setText(getIntent().getStringExtra("name"));
            description.setText(getIntent().getStringExtra("description"));
            imgPath = getIntent().getStringExtra("imgPath");
            imageView.setImageURI(Uri.parse(imgPath));

        } else if (type.equals("edit")){
            tempGuide = (Multimediaguide) getIntent().getSerializableExtra("Multimediaguide");
            title.setText(tempGuide.getName());
            description.setText(tempGuide.getDescription());

            if(tempGuide.getImgPath() != null || !(tempGuide.getImgPath().equals("null"))) {

                imgPath = tempGuide.getImgPath();
                Picasso.get().load(imgPath).into(imageView);

            }

            //StationList übertragen
            if(tempGuide.getStationList() != null)
                if(tempGuide.getStationList().size()>0) {
                    for (Station s : tempGuide.getStationList()) {
                        stationList.add(s);
                    }

                    stationAdapter.notifyDataSetChanged();
                }

        }



        //On Button Click to the "Create Station" Activity
        addStation_overview = (ImageView) findViewById(R.id.button_add_station_overview);
        addStation_overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Activity origin = (Activity)mContext;
                Intent intent = new Intent(mContext, StartCreateGuide_AddStationOverview.class);
                if(stationList != null){
                    if(stationList.size() > 0) {
                        intent.putExtra("stationList", (Serializable) stationList);
                    }
                }
                origin.startActivityForResult(intent,1);

            }
        });



        //On Button Click to the Save Multimediaguide
        save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ALertDialog for Loading with Loading circle
                AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(StartCreateGuide_Overview.this, R.style.CustomAlertDialog);
                loadingBuilder.setView(R.layout.my_progress_view);
                loadingBuilder.show();


                for(Station s: stationList) {
                    //Upload of Picture and Audio of Station
                    if(!(s.getImgSrcPath().contains(urlNoUploadPrefix))) {
                        uploadStationFilesToFirebaseStorage(s.getImgSrcPath(), s, "image");
                    }
                    if(!(s.getAudioSrcPath().contains(urlNoUploadPrefix))) {
                        uploadStationFilesToFirebaseStorage(s.getAudioSrcPath(), s, "audio");
                    }

                    //Upload of "More Media"-List Images
                    if(s.getMediaElementList() != null) {
                        if (s.getMediaElementList().size() > 0) {
                            for (int i = 0; i < s.getMediaElementList().size(); i++) {
                                if (s.getMediaElementList().get(i).getType().equals("IMAGE")) {
                                    if(!(s.getMediaElementList().get(i).getStore().contains(urlNoUploadPrefix))) {
                                        uploadStationMediaListImagesToFirebaseStorage(s, i);
                                    }
                                }
                            }
                        }
                    }

                }

                String title_ = title.getText().toString();
                String description_ = description.getText().toString();


                //Upload picture to FirebaseStorage
                //But compressing before
                //But ONLY if imgPath does not contain "https://firebasestorage.googleapis.com/" --> no upload needed then
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userId = currentUser.getUid();
                String id = ref.push().getKey();

                if(!(imgPath.contains(urlNoUploadPrefix))) {

                    File origfile = new File(imgPath);
                    try {
                        // Bitmap bitmap = BitmapFactory.decodeFile (origfile.getPath());
                        Bitmap bitmap = bitmapResizer.transform(BitmapFactory.decodeFile(origfile.getPath()));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(origfile));
                    } catch (Throwable t) {
                        Log.e("--ERROR--", "Error compressing file." + t.toString());
                        t.printStackTrace();
                    }

                    Uri file = Uri.fromFile(origfile);
                    Log.d("--DOWNLOAD--", "imgpath:" + imgPath);
                    StorageReference riversRef = mStorageRef.child("images").child(System.currentTimeMillis() + ".jpg");

                    riversRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    if (currentUser != null) {

                                                        Multimediaguide m = new Multimediaguide(id, title_, description_, uri.toString(), 5, spinner.getSelectedItem().toString(), stationList, userId, location);
                                                        ref.push().setValue(m);

                                                        Intent intent = new Intent(getApplicationContext(), UserPage.class);
                                                        startActivity(intent);
                                                        finish();

                                                        Toast.makeText(StartCreateGuide_Overview.this, "Dein Guide wurde erfolgreich hochgeladen.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }


                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Toast.makeText(StartCreateGuide_Overview.this, "Beim Upload deines Guides ist etwas schiefgelaufen. Bitte versuche es nochmal.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d("--DOWNLOAD--", "FAILED");
                                }
                            });
                } else if(imgPath.contains(urlNoUploadPrefix)){
                    if(tempGuide != null) {
                        ref.child(id).setValue(new Multimediaguide(id, title_, description_, tempGuide.getImgPath(), 5, spinner.getSelectedItem().toString(), stationList, userId, location));
                    }
                }
            }//End onClick
        });//End onClickListener of Save Button

    }

    private void uploadStationFilesToFirebaseStorage(String filePath, Station station, String type){


        StorageReference riversRef = mStorageRef.child("Files").child(System.currentTimeMillis()+".jpg");

        switch(type){
            case "image":  riversRef = mStorageRef.child("StationImages").child(System.currentTimeMillis()+".jpg");
                            break;
            case "audio": riversRef = mStorageRef.child("StationAudio").child(System.currentTimeMillis()+".3gp");
                        break;

        }

        File origfile = new File(filePath);

        if(type.equals("IMAGE")) {

            try {
                // Bitmap bitmap = BitmapFactory.decodeFile (origfile.getPath());
                Bitmap bitmap = bitmapResizer.transform(BitmapFactory.decodeFile(origfile.getPath()));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(origfile));
            } catch (Throwable t) {
                Log.e("--ERROR--", "Error compressing file." + t.toString());
                t.printStackTrace();
            }

        }

        Uri file = Uri.fromFile(origfile);
        Log.d("--UPLOAD STATION--","filepath:"+filePath);
        Log.d("--UPLOAD STATION--","uri" + file.toString());


        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //uri = downloadUrl.getResult().toString();
                                        switch(type){
                                            case "image":
                                                station.setImgSrcPath(uri.toString());
                                                break;
                                            case "audio":
                                                station.setAudioSrcPath(uri.toString());
                                                break;
                                        }
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("--DOWNLOAD--","FAILED for Station" + station.getNumber() + " TYPE " +type);
                    }
                });


    }


    private void uploadStationMediaListImagesToFirebaseStorage(Station station, int indexMediaFile){


        StorageReference riversRef = mStorageRef.child("Files").child(System.currentTimeMillis()+".jpg");
        riversRef = mStorageRef.child("StationImages").child(System.currentTimeMillis()+".jpg");

        File origfile = new File(station.getMediaElementList().get(indexMediaFile).getStore());

        try {
            // Bitmap bitmap = BitmapFactory.decodeFile (origfile.getPath());
            Bitmap bitmap = bitmapResizer.transform(BitmapFactory.decodeFile(origfile.getPath()));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(origfile));
        } catch (Throwable t) {
            Log.e("--ERROR--", "Error compressing file." + t.toString());
            t.printStackTrace();
        }

        Uri file = Uri.fromFile(origfile);
        Log.d("--UPLOAD STATION Media--","filepath:"+station.getMediaElementList().get(indexMediaFile).getStore());
        Log.d("--UPLOAD STATION Media--","uri" + file.toString());


        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                                //Replace URL with uploaded URL
                                                station.setMediaElementListStorage(indexMediaFile, uri.toString());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("--DOWNLOAD--","FAILED for Station Media List Element" + station.getNumber());
                    }
                });


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        //---------Get Stations as Result
        if(requestCode == 1){
            if (resultCode == RESULT_OK){

                List<Station> arrayList = (List<Station>) data.getSerializableExtra("stationList");
                Log.d("--MAPGUIDE--OverviewLoadStationsToOverview","Folgende Stationen wurden der nächsten Activity übergeben"+ arrayList.toString());

                if(stationList != null){ stationList.clear();}

                for(Station s : arrayList){
                    if(stationList != null){stationList.add(s); }
                }
                stationAdapter.notifyDataSetChanged();

                if(stationList != null){
                    if (stationList.size() > 0){
                        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                                .accessToken(getString(R.string.mapbox_access_token))
                                .query(Point.fromLngLat(stationList.get(0).getLongitude(), stationList.get(0).getLatitude()))
                                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                                .build();


                        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
                            @Override
                            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                                List<CarmenFeature> results = response.body().features();

                                if (results.size() > 0) {

                                    location = results.get(0).placeName();

                                } else {


                                }
                            }

                            @Override
                            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
                    }
                }
            }
        }

        //---------ImagePicker
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
            else if(requestCode == Picker.PICK_IMAGE_CAMERA) {
                cameraImagePicker.submit(data);
            }
        }




    }


    private void selectImage(){
        final CharSequence[] mediaOptions = {"Aus Bibliothek auswählen", "Foto aufnehmen","Abbrechen"};
        AlertDialog.Builder builder = new AlertDialog.Builder(StartCreateGuide_Overview.this, R.style.CustomAlertDialog);
        builder.setTitle("Füge ein Foto hinzu");

        builder.setItems(mediaOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(mediaOptions[which].equals("Aus Bibliothek auswählen")) {

                    imagePicker = new ImagePicker(StartCreateGuide_Overview.this);
                    imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                           @Override
                                                           public void onImagesChosen(List<ChosenImage> images) {
                                                               // Adapt picture to imageView
                                                               imgPath = images.get(0).getOriginalPath();
                                                               imageView.setImageBitmap(BitmapFactory.decodeFile(imgPath));
                                                           }

                                                           @Override
                                                           public void onError(String message) {
                                                               // Do error handling
                                                           }
                                                       }
                    );
                    imagePicker.pickImage();

                } else if(mediaOptions[which].equals("Foto aufnehmen")){
                    cameraImagePicker = new CameraImagePicker(StartCreateGuide_Overview.this);
                    cameraImagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                           @Override
                                                           public void onImagesChosen(List<ChosenImage> images) {
                                                               // Display images
                                                               // Adapt picture to imageView
                                                               String imagePath = images.get(0).getOriginalPath();
                                                               imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                                                           }

                                                           @Override
                                                           public void onError(String message) {
                                                               // Do error handling
                                                           }
                                                       }
                    );
                    // imagePicker.shouldGenerateMetadata(false); // Default is true
                    // imagePicker.shouldGenerateThumbnails(false); // Default is true
                    imgPath = cameraImagePicker.pickImage();
                } else if(mediaOptions[which].equals("Abbrechen")){
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed(){
        Log.d("Station_edit_Activity","Back Button was pressed");
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder1.setTitle("Änderungen verwerfen?");
        builder1.setMessage("Wenn du jetzt zurückgehst, wird dein Guide verworfen.");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Änderungen verwerfen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                StartCreateGuide_Overview.super.onBackPressed();
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