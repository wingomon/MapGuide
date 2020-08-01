package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    // Database variables
    private StorageReference mStorageRef;
    DatabaseReference ref;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);


        /** Push Data into Database!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Cloud Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference().child("guides");
        Multimediaguide m1 = new Multimediaguide("Rom bei Nacht", "Erleben Sie Roms wunderschönes Nachtleben", "https://www.travelbook.de/data/uploads/2017/06/17978759_95907d892c.jpg", 5, "Sightseeing");
        ref.push().setValue(m1);
        **/


        //Database connection
        ref= FirebaseDatabase.getInstance().getReference().child("guides");
        ref.keepSynced(true);

        //getting the recyclerview from xml
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));










        //Navigation related
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.guidesNear:
                        startActivity(new Intent(getApplicationContext(), TopGuides.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.info:
                        startActivity(new Intent(getApplicationContext(), GuideViewActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.guideAdd:
                        startActivity(new Intent(getApplicationContext(), StartCreateGuide.class));
                        overridePendingTransition(0,0);
                        return true;

                }

                return false;
            }


        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Multimediaguide> options = new FirebaseRecyclerOptions.Builder<Multimediaguide>()
                .setQuery(ref, Multimediaguide.class)
                .build();

        firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull MultimediaguideViewHolder holder, int position, @NonNull Multimediaguide model) {

                String desc = model.getDescription();
                if(desc.length() > 50){
                    desc= desc.substring(0,47) + "...";
                }
                holder.setImage(model.getImgPath());
                holder.setDescription(desc);
                holder.setTitle(model.getName());
                holder.setKilometer(model.getKm());
            }

            @NonNull
            @Override
            public MultimediaguideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_cardguideview,parent,false);
                return new MultimediaguideViewHolder(view);
            }

        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);


    }

}