package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Splash Screen variables
    private static int SPLASH_TIME_OUT = 1000;

    // Database variables
    private StorageReference mStorageRef;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Delete all Files from Directory (preventing that out of Memory is happening)
        File dirMusic = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MapGuide/MapGuide Music");

        if(dirMusic.isDirectory()){
            String[] children = dirMusic.list();
            if(children != null) {
                for (int i = 0; i < children.length; i++) {
                    new File(dirMusic, children[i]).delete();
                    Log.d("--Delete Storage of MapGuide","Music Mediafile deleted.");
                }
            }
        }

        File dirPhoto = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MapGuide/MapGuide Pictures");

        if(dirPhoto.isDirectory()){
            String[] children = dirPhoto.list();
            if(children != null) {
                for (int i = 0; i < children.length; i++) {
                    new File(dirPhoto, children[i]).delete();
                    Log.d("--Delete Storage of MapGuide","Picture Mediafile deleted.");
                }
            }
        }


        //Splash Screen Code

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashScreenIntent = new Intent (MainActivity.this, HomeScreen.class);
                startActivity(splashScreenIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }


}