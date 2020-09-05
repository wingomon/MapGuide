package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuideViewActivity extends AppCompatActivity {

    Multimediaguide multimediaguide;
    TextView guideTitle;
    TextView guideDescription;
    ImageView guideImage;

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
        guideDescription = (TextView) findViewById(R.id.guideDescription);
        guideDescription.setText(multimediaguide.getDescription());
        guideImage = (ImageView) findViewById(R.id.guideImage);

        if(multimediaguide.getImgPath() != null || !(multimediaguide.getImgPath().equals("null"))) {

            String imgPath = multimediaguide.getImgPath();
           Picasso.get().load(imgPath).into(guideImage);

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

    }
}