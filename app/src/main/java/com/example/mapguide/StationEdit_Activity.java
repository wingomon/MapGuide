package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class StationEdit_Activity extends AppCompatActivity {

    TextView stationnumber;
    Station station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_edit_);

        //Station Werte aus Intent rausholen und in die "View" einfügen
        station = getIntent().getExtras().getParcelable("station");

        stationnumber = (TextView) findViewById(R.id.textViewStationNumber);
        stationnumber.setText(Integer.toString(station.getNumber()));
    }
}