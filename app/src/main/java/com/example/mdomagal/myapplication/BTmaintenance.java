package com.example.mdomagal.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

//import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by mdomagal on 2015-03-26.
 */
public class BTmaintenance {

    private static BTmaintenance btMaintenance = new BTmaintenance(); //stała statyczna - obiekt współdzielony przez wszystkie obiekty tej klasy ( 1 obiekt - singleton)

    private BluetoothAdapter mBluetoothAdapter;

    private BTmaintenance(){ //prywatny konstruktor - nie pozwala zainicjować tego w innej klasie
        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();

    }

    public static BTmaintenance getInstance(){ //zwraca obiekt klasy - singleton
        return btMaintenance;
    }

    private final int NO_BT = 1;       //brak odbiornika bt
    private final int BT_N_EN = 2;     //bt wyłączony
    private final int BT_EN = 3;       //bt włączony
    private static final int REQUEST_ENABLE_BT = 2;

    private boolean state = false; //czy włączony BT

    //NXT wgrane na stałe do aplikacji
    protected final String nxt2 = "00:16:53:04:52:3A";
    protected final String nxt1 = "00:16:53:0F:32:32";
    protected  String connected_nxt = "";

    private BluetoothSocket socket_nxt1,socket_nxt2;
    private OutputStream nxtOutputStream = null;

    protected boolean success=false; //czy udało się połączyć

    //*************WARTOŚCI PORTÓW SILNIKÓW***********
    private static final int MOTOR_A = 0;
    private static final int MOTOR_B = 1;
    private static final int MOTOR_C = 2;


    //************USTAWIENIA PRZY KALIBRACJI*********

    private int MOTOR_X = 0;
    private int MOTOR_Y = 1;

    private int directionX = 1;
    private int directionY = 1;

    private int scale = 20;


    //***********************************************

    private boolean isBT(){
        if (mBluetoothAdapter == null)
        {
            //Urządzenie nie ma radia BT
            return false;
        }
        else
        {
            //Urządzenie ma radio BT
            return true;
        }
    }

    protected int checkBT(){
        if(isBT())
        {
            if (!mBluetoothAdapter.isEnabled())
            {
                return BT_N_EN;
            }
            else
            {
                return BT_EN;
            }
        }
        else
        {
            return NO_BT;
        }
    }

    protected int getMotorX(){
        return MOTOR_X;
    }

    protected int getMotorY(){
        return MOTOR_Y;
    }

    protected int getScale(){
        return scale;
    }

    protected int getMotorA(){
        return MOTOR_A;
    }

    protected int getMotorB(){
        return MOTOR_B;
    }

    protected int getMotorC(){
        return MOTOR_C;
    }

    protected boolean getDirectionX(){
        if (directionX == 1)
            return true;
        else return false;
    }

    protected boolean getDirectionY(){
        if(directionY == 1)
            return true;
        else return false;
    }

    protected void setDirectionX(boolean _dir){
        if(_dir)
            directionX = 1;
        else
            directionX = -1;
    }

    protected void setDirectionY(boolean _dir){
        if(_dir)
            directionY = 1;
        else
            directionY = -1;
    }

    protected void setScale(int _scale){
        scale = _scale;
    }

    protected void setMotorX(int _x){
        MOTOR_X = _x;
    }

    protected void setMotorY(int _y){
        MOTOR_Y = _y;
    }

    protected boolean getState(){
        return state;
    }

    protected void setState(boolean _state){
        state = _state;
    }

    //połącz do NXT
    protected  boolean connectToNXT(String _nxt){

        //get the BluetoothDevice of the NXT
        BluetoothDevice nxt_2 = mBluetoothAdapter.getRemoteDevice(_nxt);
//        BluetoothDevice nxt_1 = localAdapter.getRemoteDevice(nxt1);
        //try to connect to the nxt
        try {
            socket_nxt2 = nxt_2.createRfcommSocketToServiceRecord(UUID
                    .fromString("00001101-0000-1000-8000-00805F9B34FB"));

            socket_nxt2.connect();

            nxtOutputStream = socket_nxt2.getOutputStream();

            success = true;

        } catch (IOException e) {
            Log.d("Bluetooth", "Err: Device not found or cannot connect");
            success=false;
        }
        return success;

    }



    protected void enableButton(boolean _btON, RelativeLayout _background, Context _context){
        if(_btON)
        {
            _background.setBackgroundColor(_context.getResources().getColor(R.color.defBackgroundColor));
            Toast.makeText(_context, _context.getResources().getString(R.string.txtTurnOnBT), Toast.LENGTH_SHORT).show();
        }
        else
        {
            _background.setBackgroundColor(_context.getResources().getColor(R.color.errBackgroundColor));
            Toast.makeText(_context, _context.getResources().getString(R.string.txtBtErr), Toast.LENGTH_SHORT).show();

        }

        setState(_btON);
    }

    public void writeMessage(byte msg, String nxt) throws InterruptedException{
        BluetoothSocket connSock;

        //Swith nxt socket
        if(nxt.equals("nxt2")){
            connSock=socket_nxt2;
        }else{
            connSock=null;
        }

        if(connSock!=null){
            try {

                OutputStreamWriter out=new OutputStreamWriter(connSock.getOutputStream());
                out.write(msg);
                out.flush();

                Thread.sleep(1000);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            //Error
        }
    }

    protected void changeMotorSpeed(int motor, int speed) {
        if (speed > 100)
            speed = 100;

        else if (speed < -100)
            speed = -100;

        byte[] message = LCPMessage.getMotorMessage(motor, speed);
        sendMessageAndState(message);
    }

//    protected void rotateTo(int motor, int end) {
//        byte[] message = LCPMessage.getMotorMessage(motor, -80, end);
//        sendMessageAndState(message);
//    }

    protected void rotateTo(int motor, int speed, int end) {
        byte[] message = LCPMessage.getMotorMessage(motor, speed, end);
        sendMessageAndState(message);
    }


    protected void reset(int motor) {
        byte[] message = LCPMessage.getResetMessage(motor);
        sendMessageAndState(message);
    }

    private void sendMessage(byte[] message) throws IOException {
        if (nxtOutputStream == null)
            throw new IOException();

        // send message length
        int messageLength = message.length;
        nxtOutputStream.write(messageLength);
        nxtOutputStream.write(messageLength >> 8);
        nxtOutputStream.write(message, 0, message.length);
    }

    private void sendMessageAndState(byte[] message) {
        if (nxtOutputStream == null)
            return;

        try {
            sendMessage(message);
        }
        catch (IOException e) {
//            sendState(STATE_SENDERROR);
            e.printStackTrace();
        }
    }

    public int readMessage(String nxt){
        BluetoothSocket connSock;
        int n;
        //Swith nxt socket
        if(nxt.equals("nxt2")){
            connSock=socket_nxt2;
        }else if(nxt.equals("nxt1")){
            connSock=socket_nxt1;
        }else{
            connSock=null;
        }

        if(connSock!=null){
            try {

                InputStreamReader in=new InputStreamReader(connSock.getInputStream());
                n=in.read();

                return n;


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return -1;
            }
        }else{
            //Error
            return -1;
        }

    }

}
