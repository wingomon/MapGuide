package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class StartCreateGuide_AddName extends AppCompatActivity {

    Button button;
    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__add_name);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=(EditText)findViewById(R.id.etName);
                Intent intent = new Intent (getBaseContext(),StartCreateGuide_AddDescription.class);
                intent.putExtra("name",name.getText().toString());
                startActivity(intent);

            }
        });


    }
}