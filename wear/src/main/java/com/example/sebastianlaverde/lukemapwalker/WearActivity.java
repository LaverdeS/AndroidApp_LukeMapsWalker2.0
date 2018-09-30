package com.example.sebastianlaverde.lukemapwalker;

import android.app.Notification;
import android.app.Service;
import android.os.IBinder;
import android.util.Log;
import android.support.wear.widget.BoxInsetLayout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.wearable.DataClient;

import java.text.DecimalFormat;
import java.util.concurrent.ScheduledExecutorService;

public class WearActivity extends WearableActivity implements SensorEventListener {
    //Code strongly inspired from
    //https://github.com/pocmo/SensorDashboard

    private TextView accelerometerTextView;
    private TextView gyroscopeTextView;
    private TextView rotVecTextView;
    private TextView significantMotionText;
    private static final String TAG = "WearActivity";
    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    private final static int SENS_GYROSCOPE_UNCALIBRATED = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
    private final static int SENS_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
    private final static int SENS_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
    private final static int SENS_SIGNIFICANT_MOTION = Sensor.TYPE_SIGNIFICANT_MOTION;
    private ScheduledExecutorService mScheduler;
    //private DeviceClient client;

    SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        BoxInsetLayout wear_layout = findViewById(R.id.wear_layout);

        //client = Dev.getInstance(this);

        accelerometerTextView = (TextView) findViewById(R.id.accelerometer_text);
        gyroscopeTextView     = (TextView) findViewById(R.id.gyroscope_text);
        rotVecTextView        = (TextView) findViewById(R.id.rotation_text);
        significantMotionText = (TextView) findViewById(R.id.significant_motion_text);

        accelerometerTextView.setText("Accelerometer");
        gyroscopeTextView.setText("Gyroscope");

        // Enables Always-on
        setAmbientEnabled();
        startMeasurement();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMeasurement();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        switch (event.sensor.getType()) {
            case SENS_LINEAR_ACCELERATION:
                accelerometerTextView.setText("Accelerometer (no gravity):\n X " + new DecimalFormat("#.###").format(event.values[0]) +
                        " Y " +  new DecimalFormat("#.###").format(event.values[1]) +
                        " Z " +  new DecimalFormat("#.###").format(event.values[2]));
                break;
            case SENS_GYROSCOPE:
                gyroscopeTextView.setText("Gyroscope:\n X " + new DecimalFormat("#.###").format(event.values[0]) +
                        " Y " +  new DecimalFormat("#.###").format(event.values[1]) +
                        " Z " +  new DecimalFormat("#.###").format(event.values[2]));
                break;
            case SENS_ROTATION_VECTOR:
                rotVecTextView.setText("Rotation Vector:\n X " + new DecimalFormat("#.###").format(event.values[0]) +
                        " Y " +  new DecimalFormat("#.###").format(event.values[1]) +
                        " Z " +  new DecimalFormat("#.###").format(event.values[2]) +
                        " Scalar " + new DecimalFormat("#.###").format(event.values[3]));
                break;
            case SENS_SIGNIFICANT_MOTION:
                significantMotionText.setText("Significant Motion: " + event.values);
        }
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void startMeasurement() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        //if(BuildConfig.DEBUG) { logAvailableSensors(); }

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor((SENS_ACCELEROMETER));
        Sensor linearAccelerationSensor = mSensorManager.getDefaultSensor((SENS_LINEAR_ACCELERATION));
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor((SENS_GYROSCOPE));
        Sensor uncaibratedGyroscopeSensor = mSensorManager.getDefaultSensor((SENS_GYROSCOPE_UNCALIBRATED));
        Sensor rotationVectorSensor = mSensorManager.getDefaultSensor((SENS_ROTATION_VECTOR));

        //Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }
            if (linearAccelerationSensor != null) {
                mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Linear Acceleration Sensor found");
            }
            if (gyroscopeSensor != null) {
                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Gyroscope Sensor found");
            }
            if (uncaibratedGyroscopeSensor != null) {
                mSensorManager.registerListener(this, uncaibratedGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Uncalibrated Gyroscope Sensor found");
            }
            if (rotationVectorSensor != null) {
                mSensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Rotation Vector Sensor found");
            }
        }
    }

    protected void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }
}
