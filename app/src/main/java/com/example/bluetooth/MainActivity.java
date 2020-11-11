package com.example.bluetooth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final String DEVICE_NAME = "ESP32_LED_Control";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream liveInpStream;
    private OutputStream cmdOutStream;
    boolean deviceConnected = false;
    byte[] buffer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BTinit()) {
            if (BTconnect()) {
                deviceConnected = true;
                Toast.makeText(getApplicationContext(), "BLE Connected", Toast.LENGTH_SHORT).show();
                ((findViewById(R.id.on))).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String On_but ="1";
                        buffer = On_but.getBytes();
                        try {
                            cmdOutStream.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                ((findViewById(R.id.off))).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String On_but ="0";
                        buffer = On_but.getBytes();
                        try {
                            cmdOutStream.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

    }

    public boolean BTinit() {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_LONG).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Bonded Devices found.", Toast.LENGTH_LONG).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
//                if ( iterator.getAddress().equals(DEVICE_ADDR) ) {
                if ( iterator.getName().equals(DEVICE_NAME) ) {
                    device = iterator;
                    found = true;
                    break;
                }
            }

            if ( !found ) {
                Toast.makeText(getApplicationContext(), "TIPSAFE not paired.  Please Pair with TIPSAFE Sensor using PIN 8377", Toast.LENGTH_LONG).show();
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;


        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        }
        catch (IOException e) {
            e.printStackTrace();
            connected=false;

        }
        if(connected)
        {
            try {
                liveInpStream=socket.getInputStream();
                cmdOutStream=socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "TIPSAFE Sensor not found", Toast.LENGTH_SHORT).show();
        }
        return connected;
    }
}