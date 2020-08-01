package com.example.mapguide;

import android.content.Context;
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

    public MultimediaguideViewHolder(@NonNull View itemView) {
        super(itemView);
        mView=itemView;
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
        guideDescription.setText(description);
    }


    public void setKilometer (int km)
    {
        TextView guideDescription = (TextView) mView.findViewById(R.id.textViewKm);
        guideDescription.setText(Integer.toString(km));
        Log.i("Myactivity","Km is set to" + km);
    }


}
