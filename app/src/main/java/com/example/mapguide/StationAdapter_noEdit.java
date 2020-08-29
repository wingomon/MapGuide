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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StationAdapter_noEdit extends RecyclerView.Adapter<StationAdapter_noEdit.StationViewHolder> {

    private List<Station> stationList;
    private Context mContext;

    public class StationViewHolder extends RecyclerView.ViewHolder{

        public TextView number, title,  description;
        public ImageView img;


        public StationViewHolder(View view){
            super(view);
            number = (TextView) view.findViewById(R.id.textView);
            title = (TextView) view.findViewById(R.id.textViewTitle);
            description = (TextView) view.findViewById(R.id.textViewDescription);
        }


    }

    public StationAdapter_noEdit(List<Station> stationList, Context mContext){
        this.stationList = stationList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.station_list_row_noedit, parent,false);

        return new StationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        Station station = stationList.get(position);
        holder.number.setText(Integer.toString(position+1));
        holder.title.setText(station.getTitle());
        holder.description.setText(station.getDescription());
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
