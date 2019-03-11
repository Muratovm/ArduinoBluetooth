package com.michaelmuratov.arduinobluetooth.Controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.michaelmuratov.arduinobluetooth.MainActivity;
import com.michaelmuratov.arduinobluetooth.R;
import com.michaelmuratov.arduinobluetooth.Server.Sender;
import com.michaelmuratov.arduinobluetooth.UART.UARTListener;
import com.michaelmuratov.arduinobluetooth.Util.Toolbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JoystickActivity extends AppCompatActivity {
    boolean down = true;



    final int STOP = 0;
    final int CORRECT = 1;
    final int INCORRECT = 2;
    final int CORRECTING = 3;

    int state = STOP;

    public int sensor_1 = 0;
    public int sensor_2 = 0;

    UARTListener uartListener;
    int num = 0;
    JSONArray myArray;
    Sender sender;

    TextView tvNum;



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);

        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra("device address");
        Log.d("ADDRESS",deviceAddress);
        sender = new Sender(this);
        if(!deviceAddress.equals("")){
            uartListener = new UARTListener(this,this);
            uartListener.service_init(deviceAddress);
        }
        else{
            setupController();
            sender.connected = false;
        }
        myArray = new JSONArray();
        tvNum = findViewById(R.id.tvNum);
    }

    public void setupController(){
        setContentView(R.layout.coordinate_screen);
        addJoystick();

        Button finish_training = findViewById(R.id.btnClearAll);
        finish_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        myArray = new JSONArray();
                        num = 0;
                        try {
                            JSONObject object = sender.single_format("STOP");
                            Log.d("SENDER",object.toString());
                            sender.send(object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvNum.setText("Number Recorded: "+num);
                            }
                        });
                    }
                }).start();
            }
        });

        Button send_all = findViewById(R.id.btnSendAll);
        send_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("actions",myArray);
                            sender.send(object);
                            Log.d("JSON","sent all instructions");
                            Log.d("NUM",""+num);
                            myArray = new JSONArray();
                            num = 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvNum.setText("Number Recorded: "+num);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        final Switch correct = findViewById(R.id.swCorrectPath);
        final Switch incorrect = findViewById(R.id.swIncorrectPath);
        final Switch correcting = findViewById(R.id.swCorrectingPath);
        final Switch stoprecord = findViewById(R.id.swStopRecording);

        correct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    correct.setChecked(false);
                }
                else {
                    state = CORRECT;
                    incorrect.setChecked(false);
                    correcting.setChecked(false);
                    stoprecord.setChecked(false);
                }
            }
        });
        incorrect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    incorrect.setChecked(false);
                }
                else {
                    state = INCORRECT;
                    correct.setChecked(false);
                    correcting.setChecked(false);
                    stoprecord.setChecked(false);
                }
            }
        });
        correcting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    correcting.setChecked(false);
                }
                else {
                    state = CORRECTING;
                    incorrect.setChecked(false);
                    correct.setChecked(false);
                    stoprecord.setChecked(false);
                }
            }
        });
        stoprecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    stoprecord.setChecked(false);
                }
                else {
                    state = STOP;
                    incorrect.setChecked(false);
                    correcting.setChecked(false);
                    correct.setChecked(false);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addJoystick(){
        final ImageView circle = findViewById(R.id.imgCircle);
        final ImageView control = findViewById(R.id.imgControl);

        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });

        circle.setOnTouchListener(new View.OnTouchListener(){
            Coordinate_View view = findViewById(R.id.drawing_screen);

            @Override
            public boolean onTouch(View v, final MotionEvent event){
                view.setCentreCircle(circle.getX()+ (circle.getWidth() >> 1),
                        circle.getY()+ (circle.getHeight() >> 1));

                float cursorX;
                float cursorY;
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(sender.connected) {
                        uartListener.sendCommand("X\0");
                    }
                    cursorX = circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                    cursorY = circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    control.setX(cursorX);
                    control.setY(cursorY);
                    down = false;
                    view.setCentreCircle(0,0);
                    view.setCentreControl(0,0);
                    view.setTouchCoordinates(0,0);
                    view.updateOverlay();
                }

                else if(event.getAction() == MotionEvent.ACTION_DOWN){
                    down = true;
                    cursorX = event.getX() + circle.getX();
                    cursorY = event.getY() + circle.getY();
                }

                else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    float x = event.getX()- (circle.getWidth() >> 1);
                    float y = event.getY()- (circle.getHeight() >> 1);
                    double distance = Math.sqrt(Math.pow(x,2)+ (float) Math.pow(y,2));
                    //Log.d("Distance",""+distance);
                    if (distance > circle.getWidth()/2){
                        double angle  = Math.atan2(x,y);
                        cursorX = (float) ((circle.getWidth()/2)*Math.sin(angle));
                        cursorY = (float) ((circle.getHeight()/2)*Math.cos(angle));
                        cursorX+=circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                        cursorY+=circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    }
                    else{
                        cursorX = x+circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
                        cursorY = y+circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);
                    }
                    control.setX(cursorX);
                    control.setY(cursorY);
                    /*
                    view.setCentreControl(control_center_X, control_center_Y);
                    view.setTouchCoordinates(event.getRawX(),event.getRawY());
                    view.updateOverlay();
                       */
                    final int vector_X = (int)(circle.getX() + circle.getWidth()/2 - control.getWidth()/2 - cursorX) * 255/(circle.getWidth()/2);
                    final int vector_Y = (int)(circle.getY() + circle.getHeight()/2 - control.getHeight()/2 - cursorY) * 100/(circle.getHeight()/2);
                    if(sender.connected) {
                        uartListener.sendCommand("F" + vector_Y + "\0");
                        uartListener.sendCommand("S" + vector_X + "\0");
                    }
                    Log.d("X",""+vector_X);
                    Log.d("Y",""+vector_Y);
                    new Thread(new Runnable() {
                        public void run() {
                            JSONObject action = null;
                            try {
                                action = sender.format_message(
                                        "X",""+vector_X, "Y",""+vector_Y,
                                        "Sensor1",""+sensor_1, "Sensor2",""+sensor_2,
                                        "State",""+state);
                                Log.d("JSON",action.toString());
                                myArray.put(action);
                                num++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvNum.setText("Number Recorded: "+num);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(sender.connected) {
            uartListener.service_terminate();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Toolbox.activiateFullscreen(this);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}