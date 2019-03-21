package com.michaelmuratov.arduinobluetooth.Bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.michaelmuratov.arduinobluetooth.Controller.JoystickActivity;
import com.michaelmuratov.arduinobluetooth.MainActivity;
import com.michaelmuratov.arduinobluetooth.R;
import com.michaelmuratov.arduinobluetooth.Util.Permissions;
import com.michaelmuratov.arduinobluetooth.Util.Toolbox;

public class DeviceActivity extends Activity{
    public static final String TAG = "nRFUART";
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBtAdapter = null;
    DeviceScan scan;
    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_layout);
        this.activity = this;
        Permissions permissions = new Permissions(this);
        permissions.askForLocation();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Button btnConnectDisconnect = findViewById(R.id.btn_select);
        Button button = findViewById(R.id.btnSkip);
        scan = new DeviceScan(this);

        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(scan.mScanning){
                    scan.scanLeDevice(false);
                }
                else {
                    //Disconnect button pressed
                    scan.scanLeDevice(true);
                }
            }

        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, JoystickActivity.class);
                intent.putExtra("device address", "");
                startActivity(intent);
                finish();
            }
        });
        // Set initial UI state

    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
         scan.scanLeDevice(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbox.activiateFullscreen(this);
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
        statusCheck();
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
