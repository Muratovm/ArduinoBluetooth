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
import android.widget.ImageView;

import com.michaelmuratov.arduinobluetooth.MainActivity;
import com.michaelmuratov.arduinobluetooth.R;
import com.michaelmuratov.arduinobluetooth.UART.UARTListener;
import com.michaelmuratov.arduinobluetooth.Util.Toolbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class JoystickActivity extends AppCompatActivity {
    public final int movingSpeed = 5;
    boolean down = true;
    float cursorX;
    float cursorY;

    float hSpeed;
    float vSpeed;

    float centreX;
    float centreY;

    float hDifference;
    float vDifference;

    UARTListener uartListener;

    boolean connected = true;

    private DateFormat df;
    private long currentDateTime;
    private Date currentDate;

    int num = 0;

    JSONArray myArray;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        currentDateTime = System.currentTimeMillis();
        currentDate = new Date(currentDateTime);
        df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss:SSSS");

        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra("device address");
        Log.d("ADDRESS",deviceAddress);
        if(!deviceAddress.equals("")){
            uartListener = new UARTListener(this,this);
            uartListener.service_init(deviceAddress);
        }
        else{
            setupController();
            connected = false;
        }
        myArray = new JSONArray();
    }

    public void setupController(){
        setContentView(R.layout.coordinate_screen);
        addJoystick();

        Button send_all =findViewById(R.id.send_all);
        send_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    public void run() {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("actions",myArray);
                            save(object);
                            Log.d("JSON","sent all instructions");
                            Log.d("NUM",""+num);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        });

    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addJoystick(){
        final ImageView circle = findViewById(R.id.circle);
        final ImageView control = findViewById(R.id.control);

        centreX = circle.getX() + (circle.getWidth() >> 1) - (control.getWidth() >> 1);
        centreY = circle.getY() + (circle.getHeight() >> 1) - (control.getHeight() >> 1);


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
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(connected) {
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

                    /*
                    new Thread(new Runnable() {
                        public void run() {
                            while(down){
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                control.setX(cursorX);
                                control.setY(cursorY);

                                hSpeed = movingSpeed * hDifference / circle.getWidth() * 2;
                                vSpeed = movingSpeed * vDifference / circle.getHeight() * 2;
                            }

                        }
                    }).start();
                */
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

                    hDifference = control.getX() - circle.getX() - (circle.getWidth() >> 1);
                    if (hDifference > 0)
                        hDifference = control.getX() + control.getWidth() - circle.getX() - (circle.getWidth() >> 1);

                    vDifference = control.getY() - circle.getY() - (circle.getHeight() >> 1);
                    if (vDifference > 0)
                        vDifference = control.getY() + control.getHeight() - circle.getY() - (circle.getHeight() >> 1);

                    float control_center_X = cursorX+ (control.getWidth() >> 1);
                    float control_center_Y = cursorY+ (control.getHeight() >> 1);

                    view.setCentreControl(control_center_X, control_center_Y);
                    view.setTouchCoordinates(event.getRawX(),event.getRawY());
                    view.updateOverlay();

                    final int vector_X = (int)(circle.getX() + circle.getWidth()/2 - control.getWidth()/2 - cursorX) * 255/(circle.getWidth()/2);
                    final int vector_Y = (int)(circle.getY() + circle.getHeight()/2 - control.getHeight()/2 - cursorY) * 100/(circle.getHeight()/2);
                    if(connected) {
                        uartListener.sendCommand("F" + vector_Y + "\0");
                        uartListener.sendCommand("S" + vector_X + "\0");
                    }
                    Log.d("X",""+vector_X);
                    Log.d("Y",""+vector_Y);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                currentDateTime = System.currentTimeMillis();
                                currentDate = new Date(currentDateTime);
                                JSONObject action = new JSONObject();
                                action.put(df.format(currentDate),"X "+vector_X+" Y "+vector_Y);
                                myArray.put(action);
                                num++;
                                Log.d("JSON DATA", action.toString());
                                save(action);
                            } catch (IOException e) {
                                e.printStackTrace();
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
        if(connected) {
            uartListener.service_terminate();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Toolbox.activiateFullscreen(this);
    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void save(JSONObject action) throws IOException {
        HttpURLConnection conn = null;
        try{
            URL url = new URL("http://142.1.200.140:10023/uploadData/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String action_string = action.toString();

            action_string = action_string.replace("[","");
            action_string = action_string.replace("]","");
            byte[] outputBytes = action_string.getBytes("UTF-8");

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.write(outputBytes);
            os.flush();
            os.close();

            InputStream in = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);

            String res = "";
            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                res += current;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}