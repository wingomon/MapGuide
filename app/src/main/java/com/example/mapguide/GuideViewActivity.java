package com.example.mapguide;

import androidx.appcompat.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuideViewActivity extends AppCompatActivity {

    Multimediaguide multimediaguide;
    TextView guideTitle;
    TextView guideDescription;
    TextView stations, location;
    ImageView guideImage;
    Button startTour;

    //Liste der Stationen | RecyclerView Variablen
    List<Station> stationList;
    RecyclerView recyclerView;
    StationAdapter_noEdit_withImg stationAdapter;
    Station tempStation;
    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_view);
        multimediaguide = (Multimediaguide) getIntent().getSerializableExtra("Multimediaguide");

        List<Station> stationListTemp = new ArrayList<>();

        if(multimediaguide.getStationList() != null) {
            stationListTemp = multimediaguide.getStationList();
        }
        guideTitle = (TextView) findViewById(R.id.guideTitle);
        guideTitle.setText(multimediaguide.getName());
        stations = (TextView) findViewById(R.id.textViewStationAnzahl);
        if(stationListTemp != null) {
            if(stationListTemp.size()==1){
                stations.setText("1 Station");
            } else {
                stations.setText(stationListTemp.size() + " Stationen");
            }
        }
        location = (TextView) findViewById(R.id.textViewLocation);
        if(location != null) {
            location.setText(multimediaguide.getLocation());
        } else{
            location.setVisibility(View.GONE);
        }
        guideDescription = (TextView) findViewById(R.id.guideDescription);
        guideDescription.setText(multimediaguide.getDescription());
        guideImage = (ImageView) findViewById(R.id.guideImage);

        if(multimediaguide.getImgPath() != null || !(multimediaguide.getImgPath().equals("null"))) {

            String imgPath = multimediaguide.getImgPath();
           Picasso.get().load(imgPath).into(guideImage);

            guideImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Leite Image Path an die Photo View Activity weiter, um sie dort anzuzeigen
                    Intent intent = new Intent(getBaseContext(), PhotoViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("imagePath",imgPath);
                    startActivity(intent);
                }
            });

        }
        Log.d("--IMG--","imagepath is "+multimediaguide.getImgPath());
        //guideImage.setImageURI(Uri.parse(multimediaguide.getImgPath()));

        stationList = new ArrayList<Station>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewStation);
        stationAdapter =new StationAdapter_noEdit_withImg(stationList,this);
        RecyclerView.LayoutManager sLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(sLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(stationAdapter);

        if(stationList.size()>0){
            stationList.clear();
        }

        if(stationListTemp != null) {
            for (Station s : stationListTemp) {
                stationList.add(s);
            }
        }
        stationAdapter.notifyDataSetChanged();

        startTour = (Button) findViewById(R.id.startButton);

        startTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(stationList != null){
                    if(stationList.size()>0){
                        Intent intent = new Intent(getApplicationContext(), StationMapView.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("station",(Parcelable)stationList.get(0));
                        intent.putExtra("stationList",(Serializable) stationList);
                        intent.putExtra("start","true");
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(GuideViewActivity.this, "Dieser Guide beinhaltet keine Stationen und kann daher nicht gestartet werden.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


    }
}