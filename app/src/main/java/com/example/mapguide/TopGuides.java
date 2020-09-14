package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TopGuides extends AppCompatActivity {

    List<Multimediaguide> multimediaguideList;
    RecyclerView recyclerView;


    //Database
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_guides);

        //Database connection
        ref= FirebaseDatabase.getInstance().getReference().child("guides");
        ref.keepSynced(true);

        //getting the recyclerview from xml
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initializing the productlist
        multimediaguideList = new ArrayList<>();

       // multimediaguideList.add(new Multimediaguide("Rom bei Nacht", "Erleben Sie Roms wunderschönes Nachtleben", "https://www.travelbook.de/data/uploads/2017/06/17978759_95907d892c.jpg", 5, "Sightseeing"));

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Multimediaguide> options = new FirebaseRecyclerOptions.Builder<Multimediaguide>()
                .setQuery(ref, Multimediaguide.class)
                .build();

       FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder>(options) {

           @Override
           protected void onBindViewHolder(@NonNull MultimediaguideViewHolder holder, int position, @NonNull Multimediaguide model) {

               String desc = model.getDescription();
               if(desc.length() > 50){
                   desc= desc.substring(0,47) + "...";
               }
               holder.setImage(model.getImgPath());
               //holder.setDescription(desc);
               holder.setTitle(model.getName());
               holder.setKilometer(model.getKm());
               holder.getmView().setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent intent = new Intent(getApplicationContext(), GuideViewActivity.class);
                       intent.putExtra("Multimediaguide",(Serializable) model);
                       startActivity(intent);
                   }
               });
           }

           @NonNull
           @Override
           public MultimediaguideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext())
                       .inflate(R.layout.layout_cardview_big,parent,false);
               return new MultimediaguideViewHolder(view);
           }
       };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}