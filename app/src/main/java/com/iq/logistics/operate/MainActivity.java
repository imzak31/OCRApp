package com.iq.logistics.operate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.iq.logistics.common.GlobalVariable;
import com.iq.logistics.R;
import com.iq.logistics.setting.BluetoothConfig;
import com.iq.logistics.setting.InternetSettingActivity;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.iq.logistics.common.JsonUtil;
import com.iq.logistics.model.PackagesInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ENABLE_BT = 1;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    String[] cameraPermission;
    String[] storagePermission;

    private Uri imageuri;
    private int stackNum = 0;

    private StringBuilder sp;
    private String lockerStr = "", trackStr = "", descriptionStr = "", baseStr = "";
    private ArrayList<String> pictureList = new ArrayList<>();

    private EditText lockerText, trackText, descriptionText;
    private TextView stackText;
    private ImageView imgcrop;
    private Bitmap trackBitmap;

    private LinearLayout sendLayout;
    private RelativeLayout bluetoothBtn, connectBtn;
    private KProgressHUD hudOverlay;

    private ProgressDialog progressDialog;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lockerText = findViewById(R.id.locker_text);
        trackText = findViewById(R.id.tracking_text);
        descriptionText = findViewById(R.id.description_text);
        stackText = findViewById(R.id.stack_text);

        imgcrop = findViewById(R.id.cropimg);
        sendLayout = findViewById(R.id.send_layout);

        bluetoothBtn = findViewById(R.id.btn_bluetooth);
        connectBtn = findViewById(R.id.btn_connect);

        initState();

        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    public void initState() {
        stackNum = 0;
        pictureList.clear();
        sendLayout.setVisibility(View.INVISIBLE);

        lockerText.setText("");
        trackText.setText("");
        descriptionText.setText("");
        stackText.setText("Image Stack: 0 (Maximum 4 pictures)");

        lockInputFields(false);

        imgcrop.setImageResource(0);
        trackText.setFocusable(true);

        if (btSocket == null) {
            connectBtn.setEnabled(true);
            bluetoothBtn.setVisibility(View.VISIBLE);

            bluetoothBtn.setEnabled(false);
            bluetoothBtn.setVisibility(View.GONE);
        }
    }

    public void startScaning() {
        lockInputFields(true);

        // Add contrast of bitmap
        BitmapDrawable drawable = (BitmapDrawable) imgcrop.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap orginalBmp = bitmap;

        // Image operation for crop of Locker number
        //bitmap = adjustedContrast(bitmap, 50);
        //Bitmap lockerBitmap = Bitmap.createBitmap(bitmap, 0, 40,
        //        bitmap.getWidth() / 2, bitmap.getHeight() /2 );

        //lockerBitmap = Bitmap.createScaledBitmap(lockerBitmap,(int)(lockerBitmap.getWidth()*4),
        //        (int)(lockerBitmap.getHeight()*4), true);

        sendLayout.setVisibility(View.VISIBLE);

        // For Base64 of bitmap
        baseStr = bitmapToBase64(orginalBmp);

        // For Track Number from DataMatrix
        //trackBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 3 * 2, bitmap.getHeight() /3 ,
        //        bitmap.getWidth() / 3, bitmap.getHeight() /4 );

        //trackBitmap = Bitmap.createScaledBitmap(trackBitmap, (int)(trackBitmap.getWidth()*2),
        //        (int)(trackBitmap.getHeight()*2), true);

        // For Locker Number with OCR Asyntask
        //new lockerOCRTask().execute(lockerBitmap);

        // For Tracking Number with Barcode DataMatrix Asyntask
        //barcodeDetect(trackBitmap);

        //new trackBarcodeTask().execute(trackBitmap);
    }

    public void lockInputFields(boolean isLock) {
        lockerText.setEnabled(!isLock);
        trackText.setEnabled(!isLock);
        descriptionText.setEnabled(!isLock);
    }

    public String bitmapToBase64(Bitmap bitmap){

        ByteArrayOutputStream baos = new  ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);

        byte [] b = baos.toByteArray();

        String temp = null;

        try {

            System.gc();
            temp = Base64.encodeToString(b, Base64.DEFAULT);

        } catch(Exception e) {

            e.printStackTrace();

        } catch(OutOfMemoryError e) {

            baos = new  ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG,50, baos);
            b = baos.toByteArray();

            temp = Base64.encodeToString(b, Base64.DEFAULT);

            Log.e("EWN", "Out of memory error catched");
        }

        return temp;
    }

    private class lockerOCRTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected void onPreExecute() {
            showLoadingOverlay();
            super.onPreExecute();
        }

        // Do the long-running work in here
        protected Void doInBackground(Bitmap... bitmaps) {

            try {
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!textRecognizer.isOperational()) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                else {

                    Frame frame = new Frame.Builder().setBitmap(bitmaps[0]).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    sp = new StringBuilder();

                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        sp.append(myItem.getValue());
                        sp.append(" ");
                    }
                }
            } catch (Exception e) {

            }
            return null;
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Void result) {

            try {
                String[] strings = sp.toString().split(" ");
                String lockNumber = "";
                int i = 0;
                while (i < strings.length) {

                    if (strings[i].contains("ENM") || strings[i].contains("ENMO"))
                        lockNumber = strings[i];
                    i++;
                }

                String s1 = lockNumber.substring(lockNumber.indexOf("ENM"));
                lockNumber = s1.trim();

                lockNumber = lockNumber.replaceAll("O", "0");

                lockerText.setText(String.format("%s", lockNumber));
                lockerStr = lockNumber;
            } catch (Exception e) {
                makeToast("Don't catch the Locker Number");
            }

            hideLoadingOverlay();
        }
    }

    private void barcodeDetect(Bitmap bitmap) {
        try {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                    .setBarcodeFormats(Barcode.DATA_MATRIX)
                    .build();

            if (barcodeDetector.isOperational()) {
                SparseArray<Barcode> sparseArray = barcodeDetector.detect(frame);
                Barcode code = sparseArray.valueAt(0);
                trackStr = code.rawValue;
            }
            trackText.setText(String.format("T%s", trackStr));
        } catch (Exception e) {
            makeToast("Don't catch the Tracking Number");
        }
    }

    private class trackBarcodeTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected void onPreExecute() {
            showLoadingOverlay();
            super.onPreExecute();
        }

        // Do the long-running work in here
        protected Void doInBackground(Bitmap... bitmaps) {

            try {
                Frame frame = new Frame.Builder().setBitmap(bitmaps[0]).build();
                BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX)
                        .build();

                if (barcodeDetector.isOperational()) {
                    SparseArray<Barcode> sparseArray = barcodeDetector.detect(frame);
                    Barcode code = sparseArray.valueAt(0);
                    trackStr = code.rawValue;
                }
            } catch (Exception e) {

            }

            return null;
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Void result) {
            try {
                trackText.setText(String.format("T%s", trackStr));
            } catch (Exception e) {
                makeToast("Don't catch the Tracking Number");
            }

            hideLoadingOverlay();
        }
    }

    private Bitmap adjustedContrast(Bitmap src, double value) {
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));

        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    public void connectWithBT(View view) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            makeToast(getString(R.string.does_not_have_bluetooth));
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

        PackagesInfo packageInfo = new PackagesInfo(lockerStr, trackStr, descriptionStr, pictureList);
        String JSONString = '[' + JsonUtil.toPackageJSon(packageInfo) + ']';
        sendDataBT(JSONString);
    }

    public void sendWithInternet(View view) {
        ConnectPyTask task = new ConnectPyTask();

        lockerStr = lockerText.getText().toString();
        trackStr = trackText.getText().toString();
        descriptionStr = descriptionText.getText().toString();

        PackagesInfo packageInfo = new PackagesInfo(lockerStr, trackStr, descriptionStr, pictureList);

        String JSONString = '[' + JsonUtil.toPackageJSon(packageInfo) + ']';
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
            progressDialog = ProgressDialog.show(MainActivity.this,
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

    public void takeImage(View view) {
        if (stackNum > 3) {
            makeToast("Cannot take more than 4 pictures!");
            return;
        }

        if (!checkcamerapermission()) {
            requestcamerapermission();
        } else {
            pickcamera();
        }
    }

    public void savePackageInfo(View view) {
        if (stackNum > 3) {
            makeToast("Cannot take more than 4 pictures!");
            return;
        }
        stackNum++;
        stackText.setText(String.format("Image Stack: %d (Maximum 4 pictures)", stackNum));

        pictureList.add(baseStr);
        makeToast("Picture saved");
    }

    private void pickcamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Images to Text");

        imageuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraintent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestcamerapermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkcamerapermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean camerAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if (camerAccepted && writeStorageAccepted){
                        pickcamera();
                    }
                    else{
                        makeToast("Permission Denied");
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                break;
        }
    }

    //handle Image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                if (imageuri != null) {
                    imgcrop.setImageURI(imageuri);

                    showLoadingOverlay();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startScaning();
                            hideLoadingOverlay();
                        }
                    }, 200);
                }
            }
        }
    }

    private void showLoadingOverlay() {
        hudOverlay = KProgressHUD.create(MainActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).show();
    }

    private void hideLoadingOverlay() {
        if (hudOverlay != null)
            hudOverlay.dismiss();
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

    /**
     * to disconnect the bluetooth connection
     */
    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                makeToast("Error");
            }
        }
    }
}
