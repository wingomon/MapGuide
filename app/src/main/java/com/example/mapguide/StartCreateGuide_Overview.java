package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

public class StartCreateGuide_Overview extends AppCompatActivity {

    ImageView imageView;
    String imgPath;
    EditText title;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__overview);

        //Bekomme Daten aus vorheriger Activity


        title = (EditText) findViewById(R.id.etTitle);
        title.setText(getIntent().getStringExtra("name"));

        description = (EditText) findViewById(R.id.etDescription);
        description.setText(getIntent().getStringExtra("description"));



        imgPath = getIntent().getStringExtra("imgPath");
        Log.i("This is the image Path:", imgPath);
        imageView = (ImageView) findViewById(R.id.imageViewTitle);
        imageView.setImageURI(Uri.parse(imgPath));




    }
}