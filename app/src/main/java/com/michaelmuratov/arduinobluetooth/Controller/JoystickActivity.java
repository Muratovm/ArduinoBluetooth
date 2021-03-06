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

import com.michaelmuratov.arduinobluetooth.Bluetooth.DeviceActivity;
import com.michaelmuratov.arduinobluetooth.R;
import com.michaelmuratov.arduinobluetooth.Server.Sender;
import com.michaelmuratov.arduinobluetooth.Bluetooth.UARTListener;
import com.michaelmuratov.arduinobluetooth.Util.Toolbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class JoystickActivity extends AppCompatActivity {
    boolean down = true;

    final int CORRECT = 1;
    final int INCORRECT = 2;
    final int CORRECTING = 3;

    int state = CORRECT;

    public int sensor_1 = 0;
    public int sensor_2 = 0;

    UARTListener uartListener;
    int num = 0;
    JSONArray myArray;
    Sender sender;

    TextView tvNum;
    TextView tvX;
    TextView tvY;

    String start_stop = "STOP";

    Timer myTimer;

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
            tvNum = findViewById(R.id.tvNum);
            tvX = findViewById(R.id.tvX);
            tvY = findViewById(R.id.tvY);
        }
        myArray = new JSONArray();
    }

    public void setupController(){
        setContentView(R.layout.coordinate_screen);
        addJoystick();

        tvNum = findViewById(R.id.tvNum);
        tvX = findViewById(R.id.tvX);
        tvY = findViewById(R.id.tvY);



        final Button finish_training = findViewById(R.id.btnClearAll);
        finish_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        myArray = new JSONArray();
                        num = 0;
                        try {
                            if(start_stop.equals("STOP")){
                                Log.d("SENDER","START");
                                sender.send("START");
                                start_stop = "START";
                                runOnUiThread(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void run() {

                                        finish_training.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryRed));
                                        finish_training.setText("Finish Training");
                                    }
                                });

                            }
                            else{
                                Log.d("SENDER","STOP");
                                sender.send("STOP");
                                start_stop = "STOP";
                                runOnUiThread(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void run() {
                                        finish_training.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                                        finish_training.setText("Start Training");
                                    }
                                });
                            }
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

        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JSONObject object = new JSONObject();
                try {
                        object.put("actions", myArray);
                        sender.send(object);
                        Log.d("JSON", "sent all instructions");
                        Log.d("NUM", "" + num);
                        myArray = new JSONArray();
                        num = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvNum.setText("Number Recorded: " + num);
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }, 0, 5000);

        final Switch correct = findViewById(R.id.swCorrectPath);
        final Switch incorrect = findViewById(R.id.swIncorrectPath);
        final Switch correcting = findViewById(R.id.swCorrectingPath);

        correct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    correct.setChecked(false);
                }
                else {
                    state = CORRECT;
                    incorrect.setChecked(false);
                    correcting.setChecked(false);
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
                }
            }
        });
        try {
            sender.send("STOP");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    double angle  = Math.atan2(x,y);
                    if (distance > circle.getWidth()/2){

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
                    final int vector_Y = (int)(circle.getY() + circle.getHeight()/2 - control.getHeight()/2 - cursorY) * 255/(circle.getHeight()/2);

                    distance = (int) Math.sqrt(Math.pow(vector_Y, 2) + Math.pow(vector_X, 2));

                    float dist = 0;

                    if(Math.abs(vector_X) >= Math.abs(vector_Y)){
                        if(vector_X != 0){
                            dist = (float) Math.abs(vector_Y)/Math.abs(vector_X);
                        }
                    }
                    else{
                        if(vector_Y != 0) {
                            dist = (float) Math.abs(vector_X)/Math.abs(vector_Y);
                        }
                    }
                    int final_distance = (int) (dist*distance);

                    if(vector_X < 0){
                        final_distance *= -1;
                    }

                    final int final_Y = vector_Y * 150/255;
                    final int final_X = final_distance;

                    Log.d("Y",""+final_Y);
                    Log.d("X",""+final_X);

                    tvX.setText("X: "+final_X);
                    tvY.setText("Y: "+final_Y);

                    if(sender.connected) {
                        uartListener.sendCommand("F" + final_Y + "\0");
                        uartListener.sendCommand("S" + final_X + "\0");
                    }

                    JSONObject action = null;
                    try {
                        action = sender.format_message(
                                "F",""+final_X, "S",""+final_Y,
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
                return false;
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(myTimer != null){
            myTimer.cancel();
        }
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
        Intent intent = new Intent(this, DeviceActivity.class);
        startActivity(intent);
        finish();
    }
}