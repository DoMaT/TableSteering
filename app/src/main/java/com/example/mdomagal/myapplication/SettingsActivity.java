package com.example.mdomagal.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.mdomagal.myapplication.R.*;


public class SettingsActivity extends ActionBarActivity {

    private ListView devicesList;

    BTmaintenance bt;
    RelativeLayout backgroundSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_settings);

        devicesList = (ListView) findViewById(id.devicesList); //lista w widoku activity_settings



        String[] values = new String[]{"asd","def"}; //lista z elementami, które mają byś wyświetlone na liście

        bt = BTmaintenance.getInstance(); //singleton Bluetooth

        final ArrayList<String> list = new ArrayList<String>(); //lista z elementami, które mają byś wyświetlone na liście
        list.add(bt.nxt1);
        list.add(bt.nxt2);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layout.list_element, id.line, list);

        devicesList.setAdapter(adapter);

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String itemValue = (String) devicesList.getItemAtPosition(position);

                if(bt.connectToNXT(itemValue))
                {
                    Toast.makeText(getApplicationContext(), "Connected to: " + itemValue, Toast.LENGTH_LONG).show();
                    bt.connected_nxt = itemValue;

                    Intent i = new Intent(SettingsActivity.this, Play.class);
                    finish();
                    SettingsActivity.this.startActivity(i);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Not able to connect to: " + itemValue, Toast.LENGTH_LONG).show();
                }
            }
        });

        backgroundSettings = (RelativeLayout) findViewById(id.backgroundSettings);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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



