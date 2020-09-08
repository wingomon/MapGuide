package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StarCreateGuideLoginControl extends AppCompatActivity {


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_create_guide_login_control);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            //Jump Activity "Start Create Guide"
            Intent intent = new Intent(getBaseContext(), StartCreateGuide.class);
            startActivity(intent);

        } else {
            //Jump to Login Screen
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);

        }
    }
}