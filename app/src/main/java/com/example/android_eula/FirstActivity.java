package com.example.android_eula;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {

    private Button btn_call_second_activity;
    public com.example.android_eula.DashboardActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


        btn_call_second_activity=(Button) findViewById(R.id.btn_call_second_activity);
        btn_call_second_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(FirstActivity.this, SecondAcitivity.class);
                startActivity(intent);

            }
        });
    }
}
