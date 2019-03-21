package com.michaelmuratov.arduinobluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.michaelmuratov.arduinobluetooth.Bluetooth.DeviceActivity;
import com.michaelmuratov.arduinobluetooth.Controller.JoystickActivity;
import com.michaelmuratov.arduinobluetooth.Util.Toolbox;
import com.michaelmuratov.arduinobluetooth.Video.CameraActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Button video = findViewById(R.id.btnvideo);
        Button car = findViewById(R.id.btncar);

        final Activity activity = this;

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CameraActivity.class);
                startActivity(intent);
                finish();
            }
        });

        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, DeviceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Toolbox.activiateFullscreen(this);
    }
}
