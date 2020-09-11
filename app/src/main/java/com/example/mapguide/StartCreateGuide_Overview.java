package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StartCreateGuide_Overview extends AppCompatActivity {

    ImageView imageView;
    String imgPath;
    Uri imgUri;
    EditText title;
    EditText description;
    Button save;
    ImageView addStation_overview;
    String downloadImgUrl;


    private FirebaseAuth mAuth;


    BitmapResizer bitmapResizer;
    int maxwidth = 1000;
    int maxHeight = 1000;



    //Liste der Stationen | RecyclerView Variablen
    List<Station> stationList;
    RecyclerView recyclerView;
    StationAdapter_noEdit stationAdapter;
    Station tempStation;
    Context mContext;

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

       // tempStation = new Station(stationList.size()+1, 6.982851545508879, 51.02212194428202 ,"Titel der Station","null","null","Beschreibung der Station");
       // stationList.add(tempStation);
        //stationAdapter.notifyDataSetChanged();



        //Bekomme Daten aus vorheriger Activity

        title = (EditText) findViewById(R.id.etTitle);
        title.setText(getIntent().getStringExtra("name"));

        description = (EditText) findViewById(R.id.etDescription);
        description.setText(getIntent().getStringExtra("description"));


        imgPath = getIntent().getStringExtra("imgPath");
        imgUri = Uri.parse(getIntent().getStringExtra("imgUri"));
        imageView = (ImageView) findViewById(R.id.imageViewTitle);
        imageView.setImageURI(Uri.parse(imgPath));

       // stationList = (List<Station>) getIntent().getSerializableExtra("stationList");



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

                for(Station s: stationList) {
                    //Upload of Picture and Audio of Station
                    uploadStationFilesToFirebaseStorage(s.getImgSrcPath(),s,"image");
                    uploadStationFilesToFirebaseStorage(s.getAudioSrcPath(),s,"audio");
                    //Upload of "More Media"-List Images
                    if(s.getMediaElementList() != null) {
                        if (s.getMediaElementList().size() > 0) {
                            for (int i = 0; i < s.getMediaElementList().size(); i++) {
                                if (s.getMediaElementList().get(i).getType().equals("IMAGE")) {
                                    uploadStationMediaListImagesToFirebaseStorage(s, i);
                                }
                            }
                        }
                    }

                }

                Log.d("--DOWNLOAD--"," SAVE BUTTON WAS CLICKED");
                String title_ = title.getText().toString();
                String description_ = description.getText().toString();



                //Upload picture to FirebaseStorage
                //But compressing before
                File origfile = new File(imgPath);

                try {
                   // Bitmap bitmap = BitmapFactory.decodeFile (origfile.getPath());
                    Bitmap bitmap = bitmapResizer.transform(BitmapFactory.decodeFile(origfile.getPath()));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(origfile));
                }
                catch (Throwable t) {
                    Log.e("--ERROR--", "Error compressing file." + t.toString ());
                    t.printStackTrace ();
                }

                Uri file = Uri.fromFile(origfile);
                Log.d("--DOWNLOAD--","imgpath:"+imgPath);
                StorageReference riversRef = mStorageRef.child("images").child(System.currentTimeMillis()+".jpg");

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
                                                FirebaseUser currentUser = mAuth.getCurrentUser();

                                                if(currentUser != null) {

                                                    String userId = currentUser.getUid();
                                                    Multimediaguide m = new Multimediaguide(title_, description_, uri.toString(), 5, "Beispiel_Kategorie", stationList, userId);
                                                    Log.d("--DOWNLOAD URI",uri.toString());
                                                    Log.d("--DOWNLOAD URI",stationList.toString());
                                                    ref.push().setValue(m);
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
                                Log.d("--DOWNLOAD--","FAILED");
                            }
                        });

            }
        });

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

        if(requestCode == 1){
            if (resultCode == RESULT_OK){

                List<Station> arrayList = (List<Station>) data.getSerializableExtra("stationList");
                Log.d("--MAPGUIDE--OverviewLoadStationsToOverview","Folgende Stationen wurden der nächsten Activity übergeben"+ arrayList.toString());

                if(stationList != null){ stationList.clear();}

                for(Station s : arrayList){
                    if(stationList != null){stationList.add(s); }
                }
                stationAdapter.notifyDataSetChanged();

            }
        }

    }
}