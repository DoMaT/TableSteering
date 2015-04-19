package com.example.mdomagal.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.hardware.SensorEventListener;


public class Play extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float gravity[];

    private ImageView ball;

    private float startX;
    private float startY;

    private  RelativeLayout relLay;

    private int scale;

    BTmaintenance bt;

    private float prevX = 0;
    private float prevY = 0;

    static int ACCE_FILTER_DATA_MIN_TIME = 200; // 200ms
    long lastSaved = System.currentTimeMillis();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ball = (ImageView) findViewById(R.id.ball);

        relLay = (RelativeLayout) findViewById(R.id.relLay);

        bt = BTmaintenance.getInstance(); //singleton Bluetooth

        scale = bt.getScale(); //ospowiada za wielkość ruchu silnika - musi być ustalana przy kalibracji

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        updateSizeInfo();
    }

    private void updateSizeInfo() {
        int ballSize = (int) getResources().getDimension(R.dimen.ball_size);

        int height = relLay.getHeight();
        int width = relLay.getWidth();

        startX = (width- ballSize)/2;
        startY = (height- ballSize)/2;
    }



    protected void onResume() {
            super.onResume();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        protected void onPause() {
            super.onPause();
            bt.changeMotorSpeed(bt.getMotorX(),0);
            bt.changeMotorSpeed(bt.getMotorY(),0);
            mSensorManager.unregisterListener(this);
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        bt.changeMotorSpeed(bt.getMotorX(),0);
        bt.changeMotorSpeed(bt.getMotorY(),0);
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
                if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME) { //odczyt wartości co określony czas
                    lastSaved = System.currentTimeMillis();
                    float x = gravity[0];
                    float y = gravity[1];
                    float z = gravity[2];

                    onAccelChange(x, y);
                }
            }
        }

    private void onAccelChange(float x, float y){

        //******************USTAWIENIE OBRAZKA*****************
        int scaleBall = 20;
        float ballA = scaleBall * x;
        float ballB = scaleBall * y;

        ball.setX(startX - ballA);
        ball.setY(startY + ballB);

        //******************WYSTEROWANIE STOLIKA*****************
        float a = scale * x; //wartość wychylenia X przeskalowana
        float b = scale * y; //wartość wychylenia Y przeskalowana

        if(bt.success)
        {
            // a - wartość wychylenia telefonu (float) <-10*scale, 10*scale>
            //speed - wartość sterująca silnik (int) <-100, 100>
            //tableX - aktualne wychylenie stolika (float) tableX -> a
            //epsilon - odchyłka - różnica między zadanym a aktualnym położeniem stolika (float)

            int speedX = 0, speedY = 0;

            float epsilonX = a - prevX;
            float epsilonY = b - prevY;

            float abs_epsilonX = Math.abs(epsilonX);
            float abs_epsilonY = Math.abs(epsilonY);

            int s0 = 2;
            int x0 = 15; //setX0.getProgress();


            if(abs_epsilonX>s0)
            {
                speedX+= x0 + abs_epsilonX / scale * 10 - Math.pow(abs_epsilonX/(2*scale), 2);
//
            }

            if(abs_epsilonY>s0)
            {
                speedY+= x0 + abs_epsilonY / scale * 10 - Math.pow(abs_epsilonY/(2*scale), 2);
//
            }

            speedX *= (epsilonX/abs_epsilonX); //ustalenie znaku (+/-)
            speedY *= (epsilonY/abs_epsilonY);


            if(!bt.getDirectionX())
                speedX *= -1;

            if(!bt.getDirectionY())
                speedY *= -1;

            bt.changeMotorSpeed(bt.getMotorX(), speedX);
            bt.changeMotorSpeed(bt.getMotorY(), speedY);


            prevX = a;
            prevY = b;


            /**
            int speed = 80; //regulacja kierunku ruchu za pomocą prędkości silnika

            int angle = 0;
            float act = a - tableX; //odchyłka (to co chcę - to co mam)

            tableX += act;

            //a = scale * <-10, 10>
            if(act < 0)
            {
                speed *= -1; //ustalenie kierunku obrotu
                act *= -1;
            }


            angle = (int) act;

            if(angle > 0)
            {
                    bt.rotateTo(0, speed, angle);
            }
            else
            {
//                bt.changeMotorSpeed(0,0);
            }

            actVar.setText("speed = " + Integer.toString(speed) + " \n angle = "+Integer.toString(angle) );

**/

        }
        else
        {
            //nic nie rób ewentualnie wywal do menu głównego
//            finish();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                    {
//                        enableButton(false);
                    }
                    break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    {
                        manageOnBtChanged(false);
                    }
                    break;
                    case BluetoothAdapter.STATE_ON:
                    {
//                        enableButton(true);
                    }
                    break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                    {
                        manageOnBtChanged(true);
                    }
                    break;
                }
            }
        }
    };

    private void manageOnBtChanged(boolean _btON) //wyłączenie BT
    {
        finish();
    }
}
