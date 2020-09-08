package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView email;
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        mAuth = FirebaseAuth.getInstance();

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
            }
        });

        FirebaseUser user = mAuth.getCurrentUser();
        email = (TextView) findViewById(R.id.name);
        if(user != null)
        email.setText(user.getEmail());
    }
}