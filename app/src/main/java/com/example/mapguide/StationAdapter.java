package com.example.mapguide;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<Station> stationList;

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

    public StationAdapter(List<Station> stationList){
        this.stationList = stationList;
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
        holder.number.setText(Integer.toString(station.getNumber()));
        holder.title.setText(station.getTitle());
        holder.description.setText(station.getDescription());

    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }
}
