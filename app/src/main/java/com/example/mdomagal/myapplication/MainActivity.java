package com.example.mdomagal.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 2;
    private static final String URL = "https://plus.google.com/u/0/105854470793806449224/posts";

    BTmaintenance bt;

    RelativeLayout background;
    ImageButton playButton;
    ImageButton settingsButton;
    ImageButton exitButton;
    ImageButton levelButton;
    ImageButton aboutAuthor;
    ImageButton infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //*******************************INICJALIZACJA ELEMENTÓW*********************
        playButton = (ImageButton) findViewById(R.id.playButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        exitButton = (ImageButton) findViewById(R.id.exitButton);
        levelButton = (ImageButton) findViewById(R.id.levelButton);
        background = (RelativeLayout) findViewById(R.id.background);
        aboutAuthor = (ImageButton) findViewById(R.id.aboutButton);
        infoButton = (ImageButton) findViewById(R.id.infoButton);

        //******************************ONCLICKLISTENER'S***************************
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, Play.class);
                MainActivity.this.startActivity(i);

            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this,SettingsActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(mReceiver);
                finish();
            }
        });

        levelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,LevelActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        aboutAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse(URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        //***********************************POZOSTAŁE*************************
        bt = BTmaintenance.getInstance();

        boolean blueTooth = turnBtOn();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                //bt włączony
                manageOnBtChanged(true);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                //anulowano
                manageOnBtChanged(false);
            }
        }
    }

    private boolean turnBtOn(){

        final int NO_BT = 1;       //brak odbiornika bt
        final int BT_N_EN = 2;     //bt wyłączony
        final int BT_EN = 3;       //bt włączony

        switch (bt.checkBT()){
            case NO_BT:
            {
                manageOnBtChanged(false);
            }
                break;
            case BT_N_EN:
            {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.txtBtOff), Toast.LENGTH_SHORT).show();
                try
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                catch(Exception e)
                {
                    manageOnBtChanged(false);
                }
            }
                break;
            case BT_EN:
            {
                manageOnBtChanged(true);
            }
                break;
        }

        return(bt.getState());
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
            playButton.setClickable(_btON);
            settingsButton.setClickable(_btON);
            levelButton.setClickable(_btON);
            //inne przyciski

            bt.enableButton(_btON, background, getApplicationContext());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings && bt.getState())
        {
            Intent i = new Intent(MainActivity.this,SettingsActivity.class);
            MainActivity.this.startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
