package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class UserPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView email;
    Button logout;
    private Context context;

    //Database Variables
    private StorageReference mStorageRef;
    DatabaseReference ref;

    //Recycler View variables
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Multimediaguide,MultimediaguideViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        context = getApplicationContext();

        //Set RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //Firebase Authorization Initialization
        mAuth = FirebaseAuth.getInstance();

        //Database connection - Retrieve only guides of logged in user
        ref = FirebaseDatabase.getInstance().getReference().child("guides");

        //Set Email of current user on GUI
        FirebaseUser user = mAuth.getCurrentUser();
        email = (TextView) findViewById(R.id.name);
        if(user != null)
            email.setText(user.getEmail());


        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(context, HomeScreen.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();

            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        String userId = mAuth.getCurrentUser().getUid();
        Log.d("--UserId is--",userId);
        Query query = ref.orderByChild("userId").equalTo(userId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerOptions<Multimediaguide> options = new FirebaseRecyclerOptions.Builder<Multimediaguide>()
                .setQuery(query, Multimediaguide.class)
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

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), GuideViewActivity.class);
                        intent.putExtra("Multimediaguide",(Serializable) model);
                        startActivity(intent);
                        Log.d("--KLICK--","Multimediaguide wurde geklickt");
                    }
                });

                /**
                holder.editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), StartCreateGuide_Overview.class);
                        intent.putExtra("type","edit");
                        intent.putExtra("Multimediaguide",(Serializable) model);
                        startActivity(intent);
                        Log.d("--KLICK--","Multimediaguide wurde geklickt");
                    }
                });**/

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



    } //end onStart()
}