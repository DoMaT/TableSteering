package com.example.mdomagal.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;


public class LevelActivity extends ActionBarActivity {

    BTmaintenance bt;
    RadioButton radioXA, radioXB, radioXC, radioYA, radioYB, radioYC;
    RadioGroup radioGroupX, radioGroupY;
    Button acceptBut;
    Button xPlus, xMinus, yPlus, yMinus;
    SeekBar gear;
    CheckBox revX, revY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        //*********************INICJALIZACJA ELEMENTÓW*******************
        bt = BTmaintenance.getInstance();

        radioXA = (RadioButton) findViewById(R.id.radioXA);
        radioXA.setId(bt.getMotorA());
        radioXB = (RadioButton) findViewById(R.id.radioXB);
        radioXB.setId(bt.getMotorB());
        radioXC = (RadioButton) findViewById(R.id.radioXC);
        radioXC.setId(bt.getMotorC());
        radioYA = (RadioButton) findViewById(R.id.radioYA);
        radioYA.setId(bt.getMotorA());
        radioYB = (RadioButton) findViewById(R.id.radioYB);
        radioYB.setId(bt.getMotorB());
        radioYC = (RadioButton) findViewById(R.id.radioYC);
        radioYC.setId(bt.getMotorC());

        radioGroupX = (RadioGroup) findViewById(R.id.radioGroupX);
        radioGroupY = (RadioGroup) findViewById(R.id.radioGroupY);

        revX = (CheckBox) findViewById(R.id.checkBoxX);
        revY = (CheckBox) findViewById(R.id.checkBoxY);

        gear = (SeekBar) findViewById(R.id.gearSeekBar);

        gear.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        acceptBut = (Button) findViewById(R.id.acceptButton);

        xPlus = (Button) findViewById(R.id.butPlusX);
        xMinus = (Button) findViewById(R.id.butMinusX);
        yPlus = (Button) findViewById(R.id.butPlusY);
        yMinus = (Button) findViewById(R.id.butMinusY);

        xPlus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onXplus();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resetMotors();
                }
                return false;
            }});

        xMinus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    onXminus();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resetMotors();
                }
                return false;
        }});

        yPlus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onYplus();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resetMotors();
                }
                return false;
            }});

        yMinus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    onYminus();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resetMotors();
                }
                return false;
            }});

        //********************USTAWIENIE AKTUALNYCH WARTOŚCI************
        setCurrentValues();

        //*******************USTAWIENIE ONCLICKLISTENERA****************
        addListener();
    }

    private void saveValues()
    {
        int scale = 10 +  gear.getProgress()/5;

        bt.setScale(scale);
    }

    private void onXplus()
    {
        bt.changeMotorSpeed(bt.getMotorX(), bt.getScale());
    }

    private void onXminus()
    {
        bt.changeMotorSpeed(bt.getMotorX(), bt.getScale() * (-1));
    }

    private void onYplus()
    {
        bt.changeMotorSpeed(bt.getMotorY(), bt.getScale());
    }

    private void onYminus()
    {
        bt.changeMotorSpeed(bt.getMotorY(), bt.getScale() * (-1));
    }

    private void resetMotors()
    {
        bt.changeMotorSpeed(bt.getMotorX(), 0);
        bt.changeMotorSpeed(bt.getMotorY(), 0);
    }
    public void addListener()
    {
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int motorX, motorY;
                motorX = radioGroupX.getCheckedRadioButtonId();
                motorY = radioGroupY.getCheckedRadioButtonId();

                boolean dirX, dirY;

                dirX = !revX.isChecked();
                dirY = !revY.isChecked();

                saveValues();

                if(motorX != motorY && motorX != -1 && motorY != -1)
                {
                    //ZAPISZ STAN KONFIGURACJI
                    bt.setMotorX(motorX);
                    bt.setMotorY(motorY);

                    bt.setDirectionX(dirX);
                    bt.setDirectionY(dirY);

                    finish();
                }
                else
                {
                    //BŁĄD - WYBRANY 2 RAZY TEN SAM SILNIK
                    Toast.makeText(getBaseContext(),"Error configuration", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setCurrentValues()
    {
        switch(bt.getMotorX())
        {
            case 0:
                radioXA.setChecked(true);
                break;

            case 1:
                radioXB.setChecked(true);
                break;

            case 2:
                radioXC.setChecked(true);
                break;
        }

        switch(bt.getMotorY())
        {
            case 0:
                radioYA.setChecked(true);
                break;

            case 1:
                radioYB.setChecked(true);
                break;

            case 2:
                radioYC.setChecked(true);
                break;
        }

        gear.setProgress((bt.getScale()-10)*5);

        revX.setChecked(!bt.getDirectionX());

        revY.setChecked(!bt.getDirectionY());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_level, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onRadioClickedMotorX(){

    }
}
