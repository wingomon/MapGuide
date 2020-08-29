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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__add_image);

        imageView = (ImageView) findViewById(R.id.imageView2);
        addimageicon = (ImageView)findViewById(R.id.addimageicon);
        addimageicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               selectImage(StartCreateGuide_AddImage.this);
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
                intent.putExtra("imgUri",currentUri.toString());
                startActivity(intent);
                Log.i("hallo","ich wurde geklickt" + intent.getDataString());
            }
        });

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

                        //Wenn Bild ausgewählt wurde, dann aktiviere den "Weiter"-Button
                        next = (Button) findViewById(R.id.button4);
                        next.setEnabled(true);
                        next.setAlpha(1f);
                        // und blende das "Hinzufügen" Icon aus
                        addimageicon.setAlpha(0f);


                        //Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                       //String imgPath = createImageFromBitmap(selectedImage);
                        //imageView.setImageBitmap(selectedImage);
                        imageView.setImageURI(Uri.parse(currentPhotoPath));
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

                        //Wenn Bild ausgewählt wurde, dann aktiviere den "Weiter"-Button
                        next = (Button) findViewById(R.id.button4);
                        next.setEnabled(true);
                        next.setAlpha(1f);
                        // und blende das "Hinzufügen" Icon aus
                        addimageicon.setAlpha(0f);

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);

                            //Wenn Bild ausgewählt ist, dann speichere den Pfad in die Variable "currentPhotoPath"
                            currentPhotoPath=getRealPathFromURI(currentUri);
                            Log.d("--DOWNLOAD URI--",currentUri.toString());


                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);


                                //Berechtigungen für Zugriff auf den Speicher

                                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
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
        currentUri = Uri.parse(image.getAbsolutePath());
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