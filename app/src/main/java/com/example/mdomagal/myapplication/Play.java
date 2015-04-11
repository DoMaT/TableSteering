package com.example.mdomagal.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
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

    private TextView actVar;

    private ImageView ball;

    private float startX;
    private float startY;

//    private int tableXfin = 0;
    private float tableX = 0;
    private float prevVar = 0;

    private  RelativeLayout relLay;

    BTmaintenance bt;

    //regulator strojenie
    private double Kp = 1;
    private double Ki = 0;
    private double Kd = 0;

    private final int MOTOR_A = 0;
    private final int MOTOR_B = 1;

    private double prev = 0;

    static int ACCE_FILTER_DATA_MIN_TIME = 200; // 200ms
    long lastSaved = System.currentTimeMillis();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        actVar = (TextView) findViewById(R.id.act);

        ball = (ImageView) findViewById(R.id.ball);

        relLay = (RelativeLayout) findViewById(R.id.relLay);

        accelX = (TextView) findViewById(R.id.accelX);
        accelY = (TextView) findViewById(R.id.accelY);

        bt = BTmaintenance.getInstance(); //singleton Bluetooth
//        bt.rotateTo(0,0);

        //regulator
        final EditText KpField = (EditText) findViewById(R.id.editText);
        final EditText KiField = (EditText) findViewById(R.id.editText2);
        final EditText KdField = (EditText) findViewById(R.id.editText3);

        final Button accept = (Button) findViewById(R.id.button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kp = Double.parseDouble(KpField.getText().toString());
                Ki = Double.parseDouble(KiField.getText().toString());
                Kd = Double.parseDouble(KdField.getText().toString());
            }
        });

        final Button motroControl = (Button) findViewById(R.id.button2);
        motroControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Play.this, MotorControl.class);
                Play.this.startActivity(i);
            }
        });

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
            bt.changeMotorSpeed(0,0);
//            bt.reset(0);
            mSensorManager.unregisterListener(this);
//            unregisterReceiver(mReceiver);(
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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

                    // algorytm co ma się dziać po zmianie wartości na akcelerometrze

                    accelY.setText(Float.toString(y));
                    accelX.setText(Float.toString(x));

                    onAccelChange(x, y);
                }
            }
        }

    private void onAccelChange(float x, float y){

        final int scale = 20; //ospowiada za wielkość ruchu silnika - musi być ustalana przy kalibracji

        float a = scale * x;
        float b = scale * y;

        ball.setX(startX - a);
        ball.setY(startY + b);

        if(bt.success)
        {
            // a - wartość wychylenia telefonu (float) <-10*scale, 10*scale>
            //speed - wartość sterująca silnik (int) <-100, 100>
            //tableX - aktualne wychylenie stolika (float) tableX -> a
            //epsilon - odchyłka - różnica między zadanym a aktualnym położeniem stolika (float)

            int speed = 0;

            float epsilon = a - tableX;

            float abs_epsilon = Math.abs(epsilon);

            if(abs_epsilon>10)
            {
                speed+= 10;
                if(abs_epsilon>50)
                {
                    speed+= 15;
                    if(abs_epsilon>100)
                    {
                        speed+= 20;
                    }
                }
            }

            speed *= (epsilon/abs_epsilon); //ustalenie znaku (+/-)

            bt.changeMotorSpeed(MOTOR_A,speed);

            tableX += speed;

            //rusz stolikiem w odpowiednią stronę
            //a - pozycja do której dąży stolik (-10..0..10)
            //tableX - aktualna pozycja stolika
            //na razie najlepiej
            //go = act
//                    float kP = 2;
//                    double Td = 0.2;
//                    double move = kP * (act + Td* (act - prevVar));
//                    int go = ((int) move);
//                    bt.changeMotorSpeed(0, go);
//                    tableX += go; //=== tableX = a;
//                    prevVar = act;

//            double act = a - tableX; // różnica między tym co chcę, a tym co mam (odchyłka w regulatorze) - pierwsza wersja wstawiane do silnika

//            double var = Kp*(act + Ki*(act+prev)/2 + Kd*(act-prev)); //wartość wstawiana

//            double var = 10*act;

//            tableX += var;

//            bt.changeMotorSpeed(0, (int)var);

//            prev = act;

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

    private void manageOnBtChanged(boolean _btON)
    {
//        playButton.setClickable(_btON);
//        settingsButton.setClickable(_btON);
        //inne przyciski

//        bt.enableButton(_btON, backgroundSettings, getApplicationContext());
        finish();
    }
}
