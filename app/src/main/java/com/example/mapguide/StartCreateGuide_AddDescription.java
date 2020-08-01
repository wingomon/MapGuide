package com.example.mapguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class StartCreateGuide_AddDescription extends AppCompatActivity {

    TextView t1;
    String name;
    Button button;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_create_guide__add_description);

        TextView t1 = (TextView)findViewById(R.id.textView3);
        name=getIntent().getStringExtra("name");
        t1.setText(name);

        button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description= (EditText)findViewById(R.id.etDescription);
                Intent intent = new Intent (getBaseContext(),StartCreateGuide_AddImage.class);
                intent.putExtra("name",name);
                intent.putExtra("description",description.getText().toString());
                startActivity(intent);

            }
        });
    }
}