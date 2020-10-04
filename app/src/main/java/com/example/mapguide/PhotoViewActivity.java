package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class PhotoViewActivity extends AppCompatActivity {

    ImageView cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        String imagePath =getIntent().getStringExtra("imagePath");

        if(imagePath != null) {
            PhotoView photoView = (PhotoView)
                    findViewById(R.id.photo_view);
            Picasso.get().load(imagePath).placeholder(R.drawable.image_progress).into(photoView);

            cancel = findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    PhotoViewActivity.super.onBackPressed();
                }
            });
        }

    }
}