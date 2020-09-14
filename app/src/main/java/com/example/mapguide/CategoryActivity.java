package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class CategoryActivity extends AppCompatActivity {


    // Database variables
    private StorageReference mStorageRef;
    DatabaseReference ref;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder> firebaseRecyclerAdapter;
    private FirebaseAuth mAuth;

    String category;
    ImageView titleImg;
    TextView textCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mAuth = FirebaseAuth.getInstance();


        //Database connection
        ref= FirebaseDatabase.getInstance().getReference().child("guides");
        ref.keepSynced(true);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        category = getIntent().getStringExtra("category");

       // titleImg = (ImageView) findViewById(R.id.titleImg);
        textCategory = (TextView) findViewById(R.id.textCategory);

        //Setting ImageView/TextView to given category
        if(category.equals("city")){
            textCategory.setText("Städte");
        } else if (category.equals("nature")){
            textCategory.setText("Natur");
        } else if(category.equals("museum")){
            textCategory.setText("Museum");
        } else if(category.equals("adventure")){
            textCategory.setText("Sonstige");
        } else {
            textCategory.setText("Sonstige");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query q = ref.orderByChild("category").equalTo(textCategory.getText().toString());

        FirebaseRecyclerOptions<Multimediaguide> options = new FirebaseRecyclerOptions.Builder<Multimediaguide>()
                .setQuery(q, Multimediaguide.class)
                .build();

        firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Multimediaguide, MultimediaguideViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull MultimediaguideViewHolder holder, int position, @NonNull Multimediaguide model) {

                /**
                String desc = model.getDescription();
                if(desc.length() > 50){
                    desc= desc.substring(0,47) + "...";
                }**/
                holder.setImage(model.getImgPath());
                //holder.setDescription(desc);
                holder.setTitle(model.getName());
                holder.setKilometer(model.getKm());

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), GuideViewActivity.class);
                        intent.putExtra("Multimediaguide",(Serializable) model);
                        startActivity(intent);
                        Log.d("--KLICK--","Multimediaguide wurde geklickt");
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