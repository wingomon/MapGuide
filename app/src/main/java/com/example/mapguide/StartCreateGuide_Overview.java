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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartCreateGuide_Overview extends AppCompatActivity {

    ImageView imageView;
    String imgPath;
    EditText title;
    EditText description;
    Button save;
    ImageView addStation_overview;
    String downloadImgUrl;



    //Liste der Stationen | RecyclerView Variablen
    List<Station> stationList;
    RecyclerView recyclerView;
    StationAdapter stationAdapter;
    Station tempStation;
    Context mContext;

    //Database
    DatabaseReference ref;
    private StorageReference mStorageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__overview);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Database connection
        ref= FirebaseDatabase.getInstance().getReference().child("guides");
        ref.keepSynced(true);

        mContext = (Context) this;



        //StationListe initialisieren
        stationList = new ArrayList<Station>();

        recyclerView = (RecyclerView) findViewById(R.id.stationRecyclerView1);
        stationAdapter =new StationAdapter(stationList,this);
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
        imageView = (ImageView) findViewById(R.id.imageViewTitle);
        imageView.setImageURI(Uri.parse(imgPath));



        //On Button Click to the "Create Station" Activity
        addStation_overview = (ImageView) findViewById(R.id.button_add_station_overview);
        addStation_overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                Intent intent= new Intent(getBaseContext(), StartCreateGuide_AddStationOverview.class);
                startActivity(intent);
                 **/

                Activity origin = (Activity)mContext;
                Intent intent = new Intent(mContext, StartCreateGuide_AddStationOverview.class);
                origin.startActivityForResult(intent,1);

            }
        });



        //On Button Click to the Save Multimediaguide
        save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title_ = title.getText().toString();
                String description_ = description.getText().toString();

                //Upload picture to FirebaseStorage
                Uri file = Uri.fromFile(new File(imgPath));
                StorageReference riversRef = mStorageRef.child("images");

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                                downloadImgUrl = downloadUrl.getResult().toString();
                                Log.d("--DOWNLOAD--",downloadImgUrl);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });

                Multimediaguide m = new Multimediaguide(title_, description_, downloadImgUrl, 5, "Beispiel_Kategorie");
                ref.push().setValue(m);
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if (resultCode == RESULT_OK){

                List<Station> arrayList = (List<Station>) data.getSerializableExtra("stationList");

                stationList.clear();

                for(Station s : arrayList){
                    stationList.add(s);
                }
                stationAdapter.notifyDataSetChanged();
                /**

                stationList = (List<Station>) data.getSerializableExtra("stationList");
                Log.d("----MAPGUIDE--Guide_Overview",stationList.toString());
               stationAdapter.notifyDataSetChanged();
                Log.d("----MAPGUIDE--Guide_Overview","NOTIFY DATA SET CHANGED");
                tempStation = new Station(stationList.size()+1, 6.982851545508879, 51.02212194428202 ,"Titel der Station","null","null","Beschreibung der Station");
                stationList.add(tempStation);
                Log.d("----MAPGUIDE--Guide_Overview",stationList.get(0).getTitle().toString());
                stationAdapter.notifyDataSetChanged();
                 **/
            }
        }

    }
}