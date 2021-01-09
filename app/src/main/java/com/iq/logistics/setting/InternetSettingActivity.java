package com.iq.logistics.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.iq.logistics.common.GlobalVariable;
import com.iq.logistics.R;
import com.iq.logistics.common.SaveSharedPreference;


public class InternetSettingActivity extends AppCompatActivity {
    private EditText ipAddrText, portText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internetconfig);

        ipAddrText = findViewById(R.id.server_ip);
        portText = findViewById(R.id.server_port);

        ipAddrText.setText(GlobalVariable.SERVER_IP);
        portText.setText(String.valueOf(GlobalVariable.PORT));
    }

    public void saveServerInfo(View view) {
        GlobalVariable.SERVER_IP = ipAddrText.getText().toString();
        GlobalVariable.PORT = Integer.parseInt(portText.getText().toString());

        SaveSharedPreference.setSavedInternet(getApplicationContext(), GlobalVariable.SERVER_IP, GlobalVariable.PORT);
        finish();
    }
}
