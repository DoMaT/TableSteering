package com.example.mdomagal.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MotorControl extends ActionBarActivity {

    BTmaintenance bt;
    private int speed = 0;
    private int angle = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_control);

        bt = BTmaintenance.getInstance(); //singleton Bluetooth

        final EditText spedTxt = (EditText) findViewById(R.id.editText);
        final EditText angleTxt = (EditText) findViewById(R.id.editText2);

        final Button move = (Button) findViewById(R.id.button);
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed  = Integer.parseInt(spedTxt.getText().toString());
                angle = Integer.parseInt(angleTxt.getText().toString());

                sendBt();
            }
        });
    }

    private void sendBt(){
        bt.rotateTo(0, speed, angle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_motor_control, menu);
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
}
