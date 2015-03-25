package com.example.mdomagal.myapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.hardware.SensorEventListener;


public class Play extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float gravity[];

    private TextView accelX;
    private TextView accelY;

    private ImageView ball;

    private float startX;
    private float startY;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ball = (ImageView) findViewById(R.id.ball);

        accelX = (TextView) findViewById(R.id.accelX);
        accelY = (TextView) findViewById(R.id.accelY);

        accelX.setText("0");
        accelY.setText("0");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;


        int ballSize = (int) getResources().getDimension(R.dimen.ball_size);
        int verMarg = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
        int horMarg = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);

        startX = (width- ballSize)/2;
        startY = (height- ballSize)/2;

//        startX = size.x/2;
//        startY = size.y/2;

//        startX = (width - 215)/2;
//        startY = (height - 215)/2;

    }

//        public Play() {
//            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        }

        protected void onResume() {
            super.onResume();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        protected void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                gravity = event.values.clone();
            }

            if(gravity != null)
            {
                accelX.setText(Float.toString(gravity[0]));
                accelY.setText(Float.toString(gravity[1]));

                int a = Math.round(15 * gravity[0]);
                int b = Math.round(15 * gravity[1]);


//                ball.setPivotX(startX - 100);
//                ball.setPivotY(startY + 200);

                ball.setX(startX - a);
                ball.setY(startY + b);

            }

        }
}
