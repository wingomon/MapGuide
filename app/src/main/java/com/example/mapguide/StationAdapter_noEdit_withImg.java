package com.example.mapguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StationAdapter_noEdit_withImg extends RecyclerView.Adapter<StationAdapter_noEdit_withImg.StationViewHolder> {

    private List<Station> stationList;
    private Context mContext;

    public class StationViewHolder extends RecyclerView.ViewHolder{

        public TextView number, title,  description;
        public ImageView img;
        public LinearLayout ly;


        public StationViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.textViewTitle);
            description = (TextView) view.findViewById(R.id.textViewDescription);
            ly = (LinearLayout) view.findViewById(R.id.textView);
        }


    }

    public StationAdapter_noEdit_withImg(List<Station> stationList, Context mContext){
        this.stationList = stationList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.station_list_row_noedit_withimg, parent,false);

        return new StationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        Station station = stationList.get(position);
        holder.title.setText(Integer.toString(position+1)+". " + station.getTitle());
        holder.description.setText(station.getDescription());

        holder.ly.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                Activity origin = (Activity) mContext;
                Intent intent = new Intent(mContext, StationViewActivity.class);
                intent.putExtra("station",(Parcelable)stationList.get(position));
                intent.putExtra("stationList",(Serializable) stationList);
                origin.startActivity(intent);
            }

        });
    }

    public void updateStationNumbers(){
        for (Station s : stationList){
            s.setNumber(stationList.indexOf(s)+1);
            //s.setNumber(position+1);
        }
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }



}
