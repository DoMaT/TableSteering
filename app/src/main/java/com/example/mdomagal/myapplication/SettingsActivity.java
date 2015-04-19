package com.example.mdomagal.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


import static com.example.mdomagal.myapplication.R.*;


public class SettingsActivity extends ActionBarActivity {

    private ListView devicesList;

    BTmaintenance bt;
    RelativeLayout backgroundSettings;
    ArrayList<String> list;

    Button findNXT;

    private static final String FILENAME = "NXT_MAC.txt"; //NXT_MAC //getNXTlist
    private static final String FILENAMEsave = "NXT.txt"; //getNXTlist2


    private final static String TAG = "SerializeObject";

    boolean newDevice = false;
    private ArrayList<String> mArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_settings);

        devicesList = (ListView) findViewById(id.devicesList); //lista w widoku activity_settings

        findNXT = (Button) findViewById(id.findNXTbutton);

        findNXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryFindNewNXT();
            }
        });

        bt = BTmaintenance.getInstance(); //singleton Bluetooth

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        mArrayAdapter = getNXTlist2();

        setList(getNXTlist2());

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                onItemClickListener(position);
            }
        });

        devicesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onItemLongClickListener(position);
                return false;
            }
        });


        backgroundSettings = (RelativeLayout) findViewById(id.backgroundSettings);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        IntentFilter filterFD = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiverFD, filterFD);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiverFD);
        super.onDestroy();
    }

    private void setList(ArrayList<String> arrayList){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layout.list_element, id.line, arrayList);//list
        devicesList.setAdapter(adapter);
    }

    private void onItemClickListener(int position){
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

    private void onItemLongClickListener(final int position){
        AlertDialog alertDialog = new AlertDialog.Builder(
                SettingsActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Delete paired device");

        // Setting Dialog Message
        alertDialog.setMessage("You are about to delete this item");

        // Setting Icon to Dialog
//        alertDialog.setIcon(R.drawable.tick);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                SaveText(position);

                setList(getNXTlist2());
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                Toast.makeText(getApplicationContext(), "Deleting canceled", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }



    private void SaveText(){

        ArrayList<String> list = getNXTlist2();
        //DODAJ ZNALEZIONEGO NXT

        try {

            // open myfilename.txt for writing
            OutputStreamWriter out=new OutputStreamWriter(openFileOutput(FILENAMEsave, MODE_PRIVATE)); //MODE_APPEND
            // write the contents to the file

            int i = 0;
            int listSize = list.size();

            while (i != listSize)
            {
                out.write(list.get(i));
                out.write('\n');
                i++;
            }
            // close the file

            out.close();

            Toast.makeText(this,"Text Saved !",Toast.LENGTH_LONG).show();
        }

        catch (java.io.IOException e) {

            //do something if an IOException occurs.
            Toast.makeText(this,"Sorry Text could't be added",Toast.LENGTH_LONG).show();
        }
    }

    private void SaveText(ArrayList<String> _list){

        ArrayList<String> list = _list;
        //DODAJ ZNALEZIONEGO NXT

        try {

            // open myfilename.txt for writing
            OutputStreamWriter out=new OutputStreamWriter(openFileOutput(FILENAMEsave, MODE_PRIVATE)); //MODE_APPEND
            // write the contents to the file

            int i = 0;
            int listSize = list.size();

            while (i != listSize)
            {
                out.write(list.get(i));
                out.write('\n');
                i++;
            }
            // close the file

            out.close();

            Toast.makeText(this,"New NXT saved",Toast.LENGTH_SHORT).show();
        }

        catch (java.io.IOException e) {

            //do something if an IOException occurs.
            Toast.makeText(this,"Sorry , nothing added",Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveText(int deleteID){

        ArrayList<String> list = getNXTlist2();
        //DODAJ ZNALEZIONEGO NXT

        try {

            // open myfilename.txt for writing
            OutputStreamWriter out=new OutputStreamWriter(openFileOutput(FILENAMEsave, MODE_PRIVATE)); //MODE_APPEND
            // write the contents to the file

            int i = 0;
            int listSize = list.size();

            while (i != listSize)
            {
                if(i!=deleteID)
                {
                    out.write(list.get(i));
                    out.write('\n');
                }
                i++;
            }
            // close the file

            out.close();

            Toast.makeText(this,"NXT deleted",Toast.LENGTH_SHORT).show();
        }

        catch (java.io.IOException e) {

            //do something if an IOException occurs.
            Toast.makeText(this,"Sorry, nothing deleted",Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getNXTlist2 (){

//        StringBuilder text = new StringBuilder();
        ArrayList<String> NXTlist = new ArrayList<String>();

        try {
            // open the file for reading we have to surround it with a try

            InputStream instream = openFileInput(FILENAMEsave);//open the text file for reading

            // if file the available for reading
            if (instream != null) {

                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line=null;
                //We initialize a string "line"

                while (( line = buffreader.readLine()) != null) {
                    //buffered reader reads only one line at a time, hence we give a while loop to read all till the text is null
                    NXTlist.add(line);
//                    text.append(line);
//                    text.append('\n');    //to display the text in text line


                }}}

        //now we have to surround it with a catch statement for exceptions
        catch (IOException e) {
            e.printStackTrace();
        }

        //now we assign the text readed to the textview

        Log.e(TAG, "getNXT2: " + NXTlist.toString()+ getFilesDir());
        return NXTlist;

    }
//*********************STARA WERSJA - PLIK Z ASSETSMANAGERA*************************
    private ArrayList<String> getNXTlist(){
        ArrayList<String> NXTlist = new ArrayList<String>();

        AssetManager assetManager = getResources().getAssets();

        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(assetManager.open(FILENAME)));
            ArrayList<String> values = new ArrayList<String>();
            String line = bReader.readLine();
            while (line != null) {
//                values.add(line);
                NXTlist.add(line);
                line = bReader.readLine();
            }
            bReader.close();
//            for (String v : values)
//                Log.i("Array is ", v);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "getNXT: " + NXTlist.toString());
        return NXTlist;
    }

    private void tryFindNewNXT(){

//        SaveText();

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();

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

    private final BroadcastReceiver mReceiverFD = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(/*device.getName() + "\n" +*/ device.getAddress());

                if(addToList(mArrayAdapter, device.getAddress()))
                {
                    setList(mArrayAdapter);

                    SaveText(mArrayAdapter);
                }
            }
        }
    };

    private boolean addToList(ArrayList<String> list, String newDevice)
    {
        int i = 0;
        int listSize = list.size();

        boolean exists = false;
        boolean isNXT = false;

        if(newDevice.contains("00:16:53"))
        {
            isNXT = true;
        }

        while (i != listSize)
        {
            if(newDevice.equals(list.get(i)))
            {
                exists = true;
            }

            i++;
        }
        if(!exists && isNXT)
        {
            list.add(newDevice);
            return true;
        }
        else
        {
            return false;
        }
    }


    private void manageOnBtChanged(boolean _btON)
    {

        finish();
    }
}



