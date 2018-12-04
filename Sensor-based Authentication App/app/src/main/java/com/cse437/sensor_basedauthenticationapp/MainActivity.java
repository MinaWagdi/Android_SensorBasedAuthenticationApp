package com.cse437.sensor_basedauthenticationapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private static String TAG="MINA";
    private SensorManager sensorManager;
    Sensor accelerometer;

    float last_x=0;
    float last_y=0;
    float last_z=0;
    int shake_count=0;

    long BeginTime=0;
    long curTime=0;
    public static long TimeDiff = 0;

    int[] pin={1,2,3};
    int pin_index=0;
    public static int time_frame = 5000;
    public static boolean ErrorRisen=false;
    public static boolean PinAuthenticated = false;
    public static boolean StopThread=false;


    private static final float SHAKE_THRESHOLD = 2.55f;

    TextView progress_bar;
    public static Handler handler = new Handler();
    public static ProgressBar pb;

    Button StartBtn;
    Button ResetBtn;

    ProgressBarThread pb_thread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");

        progress_bar=findViewById(R.id.progress_bar_text);
        pb = findViewById(R.id.progressBar);
        pb.setMax(time_frame);

        StartBtn=findViewById(R.id.button);
        ResetBtn=findViewById(R.id.button2);

        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeginTime=System.currentTimeMillis();
                curTime = System.currentTimeMillis();
                StopThread=false;

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
                Log.i(TAG,"Sensor Registered");

                pb_thread=new ProgressBarThread();
                pb_thread.start();
            }
        });

        ResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(MainActivity.this,accelerometer);
                shake_count=0;
                PinAuthenticated=false;
                ErrorRisen=false;
                pin_index=0;
                progress_bar.setText("");
                StopThread=true;

                pb.setProgress(0);
            }
        });


    }
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        float speed = Math.abs(x + y + z - last_x - last_y - last_z) / 200*10000;
        curTime=System.currentTimeMillis();
        TimeDiff=curTime-BeginTime;

        //if Lesa el SmallTimeFrame ma5lessh
        if( PinAuthenticated==false && ErrorRisen==false) {
            if (TimeDiff < time_frame) {
                if (gForce > SHAKE_THRESHOLD) {
                    shake_count++;
                    progress_bar.append(" " + shake_count);
                    if (shake_count > pin[pin_index]) {
                        progress_bar.append(" Error ");
                        ErrorRisen = true;
                    }
                }
            }
            //hena ya3ni el timeframe el so3'ayar 5eles wel shakes mazboota laken el BigTimeFrame ma5lessh
            else if (shake_count == pin[pin_index] && pin_index < pin.length - 1) {
                progress_bar.append(" - ");
                shake_count = 0;
                pin_index++;
                BeginTime = System.currentTimeMillis();
                curTime = System.currentTimeMillis();
                Log.i(TAG, "pin index incremented");

            }
            //hena el timeframe el kebiir 5eles wel shakes mazboota
            else if (pin_index == (pin.length - 1) && shake_count == pin[pin_index]) {
                progress_bar.append(" Finished ");
                PinAuthenticated = true;
            } else {
                progress_bar.append(" ERROR ");
                ErrorRisen = true;

            }
        }

        last_x=x;
        last_y=y;
        last_z=z;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
