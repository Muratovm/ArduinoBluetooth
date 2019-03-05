package com.michaelmuratov.arduinobluetooth.UART;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.michaelmuratov.arduinobluetooth.Controller.Coordinate_View;
import com.michaelmuratov.arduinobluetooth.Controller.JoystickActivity;
import com.michaelmuratov.arduinobluetooth.MainActivity;
import com.michaelmuratov.arduinobluetooth.R;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class UARTListener {

    private static final String TAG = "nRFUART";


    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int mState = UART_PROFILE_DISCONNECTED;

    private Activity activity;
    private JoystickActivity joystick;
    public UartService mService;
    private String  deviceAddress;

    Coordinate_View view = null;

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);

            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                activity.finish();
            }
            if(mService.connect(deviceAddress)){
                Log.d(TAG,"CONNECTED TO THE CAR");
            }
            else{
                Log.d(TAG,"SOMETHING WENT WRONG CONNECTING TO THE CAR");
            }
        }
        public void onServiceDisconnected(ComponentName classname) {
            ////mService.disconnect(mDevice);
            mService = null;
        }
    };

    public UARTListener(JoystickActivity joystick,
                        Activity activity){
        this.joystick = joystick;
        this.activity = activity;
    }

    public void service_init(String  deviceAddress) {
        this.deviceAddress =  deviceAddress;
        Intent bindIntent = new Intent(activity, UartService.class);
        activity.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(activity).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    public void service_terminate(){
        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        mService.disconnect();
        mService.stopSelf();
        activity.unbindService(mServiceConnection);
        mService= null;
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //*********************//
            assert action != null;
            switch (action) {
                case UartService.ACTION_GATT_CONNECTED:
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(TAG, "UART_CONNECT_MSG");
                            mState = UART_PROFILE_CONNECTED;
                            Toast.makeText(activity, "connected to the car", Toast.LENGTH_SHORT).show();
                            joystick.setupController();
                            view = activity.findViewById(R.id.drawing_screen);
                        }
                    });
                    break;
                case UartService.ACTION_GATT_DISCONNECTED:
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(TAG, "UART_DISCONNECT_MSG");
                            mState = UART_PROFILE_DISCONNECTED;
                            mService.close();
                            Toast.makeText(activity, "disconnected from the car", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);
                            activity.finish();

                        }
                    });
                    break;
                case UartService.ACTION_GATT_SERVICES_DISCOVERED:
                    Log.d("TAG","DISCOVERED");
                    mService.enableTXNotification();
                    Log.d("TAG","DISCOVERED");
                    break;
                case UartService.ACTION_DATA_AVAILABLE:
                    final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                String text = new String(txValue, "UTF-8");
                                String number = text.substring(7);
                                int value = Integer.valueOf(number);
                                if(view != null){
                                    if (value < 100) {
                                        view.setFrontDistance(20, 20, value * 10, 20);
                                        view.updateOverlay();
                                    }
                                }
                                Log.d("OUTPUT",text);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                    break;
                case UartService.DEVICE_DOES_NOT_SUPPORT_UART:
                    mService.disconnect();
                    break;
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    public void sendCommand(String message){
        if(mState == UART_PROFILE_CONNECTED) {
            byte[] value;
            try {
                //send data to service
                value = message.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
