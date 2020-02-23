package com.vincentz1911.mapsandbtandobd2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends FragmentActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            public void run() {
                initBT();
            }
        }).start();
        Tools.checkPermissions(this);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big, new MapFragment(), "").commit();
    }

    private void initBT() {
        //Gets list of paired devices
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Tools.msg(this,"Couldn't find Bluetooth");
            return;
        }

        final ArrayList<BluetoothDevice> paired =
                new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
        if (paired.size() == 0) {
            Tools.msg(this,"No paired devices found");
            return;
        }

        //Checks if device is named OBDII and connects
        for (BluetoothDevice device : paired) {
            if (device.getName().toUpperCase().equals("OBDII")) {
                Tools.msg(this, "Bluetooth OBDII device found: " + device.getName());
                getPreferences(Context.MODE_PRIVATE).edit()
                        .putString("btaddress", device.getAddress()).apply();
                connectBT(device.getAddress());
                return;
            }
        }
        //If no applicable devices have been found, open list of paired devices
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selectBT(paired);
            }
        });
    }

    private void selectBT(final ArrayList<BluetoothDevice> paired) {
        //Creates list for dialog with paired bluetooth devices
        ArrayList<String> list = new ArrayList<>();
        for (BluetoothDevice device : paired) list.add(device.getName());
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.select_dialog_singlechoice, list);

        //Creates dialog for choosing bluetooth device
        new AlertDialog.Builder(this)
                .setTitle("Choose Bluetooth device")
                .setSingleChoiceItems(adp, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        final String address = paired.get(pos).getAddress();
                        getPreferences(Context.MODE_PRIVATE).edit().
                                putString("btaddress", address).apply();
                        connectBT(address);
                    }
                })
                .show();
    }

    private void connectBT(String address) {
        //Connects to OBDII device and opens Fragment
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket socket = null;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Tools.msg(this,"Couldn't connect to ELM327 Bluetooth ODBII adapter");
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_top, new OBD2Fragment(socket), "").commit();
    }
}
