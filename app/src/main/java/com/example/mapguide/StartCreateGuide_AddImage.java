package com.example.mapguide;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StartCreateGuide_AddImage extends AppCompatActivity {

    private ImageView imageView;
    Bitmap bmp;
    Button button;
    Button next;
    String name;
    String description;
    String currentPhotoPath;
    Uri currentUri;
    ImageView addimageicon;

    ContentValues values;
    Uri imageUri;

    ImagePicker imagePicker;
    CameraImagePicker cameraImagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__add_image);

        imageView = (ImageView) findViewById(R.id.imageView2);
        next = (Button) findViewById(R.id.button4);
        addimageicon = (ImageView)findViewById(R.id.addimageicon);
        addimageicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               selectImage();
            }
        });


        //Weitergabe der Informationen (Name, Description, Image) an die nächste Activity beim Klicken des "Weiter"-Buttons
        next = (Button)findViewById(R.id.button4);
        next.setAlpha(.5f);
        next.setEnabled(false);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = getIntent().getStringExtra("name");
                description = getIntent().getStringExtra("description");

                Intent intent= new Intent(getBaseContext(), StartCreateGuide_Overview.class);

                intent.putExtra("name",name);
                intent.putExtra("description",description);
                intent.putExtra("imgPath",currentPhotoPath);
                startActivity(intent);
                Log.i("hallo","ich wurde geklickt" + intent.getDataString());
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
            else if(requestCode == Picker.PICK_IMAGE_CAMERA) {
                cameraImagePicker.submit(data);
            }
        }

    }

    /**
     * Select Image
     * Possible to choose between
     * Gallery or Camera
     *
     */

    private void selectImage(){
        final CharSequence[] mediaOptions = {"Aus Bibliothek auswählen", "Foto aufnehmen","Abbrechen"};
        AlertDialog.Builder builder = new AlertDialog.Builder(StartCreateGuide_AddImage.this, R.style.CustomAlertDialog);
        builder.setTitle("Füge ein Foto hinzu");

        builder.setItems(mediaOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(mediaOptions[which].equals("Aus Bibliothek auswählen")) {

                    imagePicker = new ImagePicker(StartCreateGuide_AddImage.this);
                    imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                           @Override
                                                           public void onImagesChosen(List<ChosenImage> images) {
                                                               // Adapt picture to imageView
                                                               currentPhotoPath = images.get(0).getOriginalPath();
                                                               imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                                                               addimageicon.setAlpha(0f);
                                                               next.setEnabled(true);
                                                               next.setAlpha(1f);
                                                           }

                                                           @Override
                                                           public void onError(String message) {
                                                               // Do error handling
                                                           }
                                                       }
                    );
                    imagePicker.pickImage();

                } else if(mediaOptions[which].equals("Foto aufnehmen")){
                    cameraImagePicker = new CameraImagePicker(StartCreateGuide_AddImage.this);
                    cameraImagePicker.setImagePickerCallback(new ImagePickerCallback(){
                                                                 @Override
                                                                 public void onImagesChosen(List<ChosenImage> images) {
                                                                     // Display images
                                                                     // Adapt picture to imageView
                                                                     currentPhotoPath = images.get(0).getOriginalPath();
                                                                     imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                                                                     addimageicon.setAlpha(0f);
                                                                     next.setEnabled(true);
                                                                     next.setAlpha(1f);

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





}