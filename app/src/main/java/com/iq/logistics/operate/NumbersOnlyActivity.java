package com.iq.logistics.operate;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.iq.logistics.common.GlobalVariable;
import com.iq.logistics.common.JsonUtil;
import com.iq.logistics.model.PackagesInfo;
import com.iq.logistics.R;
import com.iq.logistics.setting.BluetoothConfig;
import com.iq.logistics.setting.InternetSettingActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class NumbersOnlyActivity extends AppCompatActivity {
    public static final int REQUEST_ENABLE_BT = 1;

    private static final String LOG_TAG = NumbersOnlyActivity.class.getSimpleName();

    private String lockerStr = "", trackStr = "", descriptionStr = "";
    private EditText lockerText, trackText, descriptionText;
    private RelativeLayout bluetoothBtn, connectBtn;

    private ProgressDialog progressDialog;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twonumber);

        lockerText = findViewById(R.id.locker_text);
        trackText = findViewById(R.id.tracking_text);
        descriptionText = findViewById(R.id.description_text);

        bluetoothBtn = findViewById(R.id.btn_bluetooth);
        connectBtn = findViewById(R.id.btn_connect);

        initState();
    }

    public void initState() {
        lockerText.setText("");
        trackText.setText("");
        descriptionText.setText("");
        trackText.setFocusable(true);

        if (btSocket == null) {
            connectBtn.setEnabled(true);
            bluetoothBtn.setVisibility(View.VISIBLE);

            bluetoothBtn.setEnabled(false);
            bluetoothBtn.setVisibility(View.GONE);
        }
    }

    public void connectWithBT(View view) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.does_not_have_bluetooth, Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntentBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntentBluetooth, REQUEST_ENABLE_BT);
        } else {
            if (GlobalVariable.MAC_ADDRESS == null)
                makeToast("Please set the Bluetooth config");
            else new ConnectBT().execute();
        }
    }

    public void sendWithBT(View view) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lockerStr = lockerText.getText().toString();
        trackStr = trackText.getText().toString();
        descriptionStr = descriptionText.getText().toString();

        PackagesInfo packageInfo = new PackagesInfo(lockerStr, trackStr, descriptionStr);
        String JSONString = '[' + JsonUtil.toNumbersOnlyJSon(packageInfo) + ']';
        sendDataBT(JSONString);
    }

    public void sendWithInternet(View view) {
        ConnectPyTask task = new ConnectPyTask();

        lockerStr = lockerText.getText().toString();
        trackStr = trackText.getText().toString();
        descriptionStr = descriptionText.getText().toString();

        PackagesInfo packageInfo = new PackagesInfo(lockerStr, trackStr, descriptionStr);
        String JSONString = '[' + JsonUtil.toNumbersOnlyJSon(packageInfo) + ']';

        task.execute(JSONString);
    }

    private class ConnectPyTask extends AsyncTask<String, Void, String> {
        private boolean connectSuccess = true;

        @Override
        protected String doInBackground(String... data) {
            try {
                Socket socket = new Socket(GlobalVariable.SERVER_IP, GlobalVariable.PORT); //Server IP and PORT
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                printWriter.write(data[0]); // Send Data
                printWriter.flush();
            } catch (Exception e){
                Log.d("Exception", e.toString());

                connectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (connectSuccess) {
                makeToast("Send via Internet Successfully.");
                initState();
            }
            else {
                makeToast("Internet Connection Failed.");
            }
        }
    }

    private void sendDataBT(String data) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(data.getBytes());

                makeToast("Send via Bluetooth Successfully");
                initState();
            } catch (IOException e) {
                makeToast("Sending Data via Bluetooth Error");
            }
        }
    }

    /**
     * An AysncTask to connect to Bluetooth socket
     */
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {

            //show a progress dialog
            progressDialog = ProgressDialog.show(NumbersOnlyActivity.this,
                    "Connecting...", "Please wait!!!");
        }

        //while the progress dialog is shown, the connection is done in background
        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (btSocket == null || !isBtConnected) {
                    //get the mobile bluetooth device
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    //connects to the device's address and checks if it's available
                    BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(GlobalVariable.MAC_ADDRESS);

                    //create a RFCOMM (SPP) connection
                    btSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(GlobalVariable.myUUID);

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                    //start connection
                    btSocket.connect();
                }

            } catch (IOException e) {
                //if the try failed, you can check the exception here
                connectSuccess = false;
            }

            return null;
        }

        //after the doInBackground, it checks if everything went fine
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(LOG_TAG, connectSuccess + "");
            if (!connectSuccess) {
                makeToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
            } else {
                isBtConnected = true;
                makeToast("Connected to Server");

                connectBtn.setEnabled(false);
                connectBtn.setVisibility(View.GONE);

                bluetoothBtn.setEnabled(true);
                bluetoothBtn.setVisibility(View.VISIBLE);
            }

            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                startActivity(new Intent(this, InternetSettingActivity.class));
                return true;

            case R.id.blt:
                startActivity(new Intent(this, BluetoothConfig.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
