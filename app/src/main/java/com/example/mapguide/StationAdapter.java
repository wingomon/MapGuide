package com.example.mapguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<Station> stationList;
    private Context mContext;

    public class StationViewHolder extends RecyclerView.ViewHolder{

        public TextView number, title,  description;
        public ImageView img;
        public ImageView deleteIcon;
        public ImageView editIcon;




        public StationViewHolder(View view){
            super(view);
            number = (TextView) view.findViewById(R.id.textView);
            title = (TextView) view.findViewById(R.id.textViewTitle);
            description = (TextView) view.findViewById(R.id.textViewDescription);
            deleteIcon = (ImageView) view.findViewById(R.id.delete);
            editIcon = (ImageView) view.findViewById(R.id.edit);

        }


    }

    public StationAdapter(List<Station> stationList, Context mContext){
        this.stationList = stationList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.station_list_row, parent,false);

        return new StationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        Station station = stationList.get(position);
        holder.number.setText(Integer.toString(position+1));
        holder.title.setText(station.getTitle());
        holder.description.setText(station.getDescription());

        holder.editIcon.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                Activity origin = (Activity) mContext;
                Intent intent = new Intent(mContext, StationEdit_Activity.class);
                intent.putExtra("station",(Parcelable)stationList.get(position));
                origin.startActivityForResult(intent,1);
            }

        });


        holder.deleteIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                stationList.remove(position);
                updateStationNumbers();
                notifyDataSetChanged();
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
