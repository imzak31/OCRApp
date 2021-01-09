package com.iq.logistics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.iq.logistics.operate.MainActivity;
import com.iq.logistics.operate.NumbersOnlyActivity;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void allModeClicked(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    public void inputOnlyModeClicked(View view) {
        startActivity(new Intent(getApplicationContext(), NumbersOnlyActivity.class));
    }
}
