package com.example.mapguide;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class MultimediaguideViewHolder extends RecyclerView.ViewHolder {

    View mView;
    ImageView imageView;
    ImageView editButton;

    public MultimediaguideViewHolder(@NonNull View itemView) {
        super(itemView);
        mView=itemView;
        imageView = (ImageView) mView.findViewById(R.id.imageView);
        editButton = (ImageView) mView.findViewById(R.id.edit);
    }

    public void setImage(String image)
    {
        ImageView guideImg = (ImageView) mView.findViewById(R.id.imageView);
        Picasso.get().load(image).into(guideImg);

    }

    public void setTitle (String title)
    {
        TextView guideTitle = (TextView) mView.findViewById(R.id.textViewTitle);
        guideTitle.setText(title);
        Log.i("Myactivity","Title is set to" + title);
    }


    public void setDescription (String description)
    {
        TextView guideDescription = (TextView) mView.findViewById(R.id.textViewDescription);
        if(guideDescription != null)
        guideDescription.setText(description);
    }


    public void setKilometer (int km)
    {
        TextView guideDescription = (TextView) mView.findViewById(R.id.textViewKm);
        guideDescription.setText(Integer.toString(km)+" km");
        Log.i("Myactivity","Km is set to" + km);
    }

    public void setLocation (String loc)
    {
        TextView location = (TextView) mView.findViewById(R.id.textViewLocation);
        location.setText(loc);
    }


    public View getmView() {
        return mView;
    }

    public void setmView(View mView) {
        this.mView = mView;
    }
}
