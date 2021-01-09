package com.iq.logistics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.iq.logistics.common.GlobalVariable;
import com.iq.logistics.common.SaveSharedPreference;


public class Splash extends AppCompatActivity {

    private static final int splash = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Context context = getApplicationContext();
        if (SaveSharedPreference.getSavedStatus(context)) {
            GlobalVariable.SERVER_IP = SaveSharedPreference.getSavedIp(context);
            GlobalVariable.PORT = SaveSharedPreference.getSavedPort(context);
            GlobalVariable.MAC_ADDRESS = SaveSharedPreference.getSavedMac(context);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();

            }
        }, splash);
    }
}
