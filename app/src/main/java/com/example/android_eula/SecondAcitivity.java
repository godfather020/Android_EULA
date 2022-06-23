package com.example.android_eula;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SecondAcitivity extends AppCompatActivity {

    private Button btn_call_third_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_acitivity);
        btn_call_third_activity=(Button) findViewById(R.id.btn_call_third_activity);
        btn_call_third_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SecondAcitivity.this,ThirdActivity.class);
                startActivity(intent);
            }
        });
    }
}
