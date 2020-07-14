package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//Wichtig: Das wird nicht die MainActivity sein!
//ADD Guide Name

public class MainActivity extends AppCompatActivity {

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cloud Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }


}