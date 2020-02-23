package com.vincentz1911.mapsandbtandobd2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.vincentz1911.mapsandbtandobd2.obd.commands.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.engine.*;

public class OBD2Fragment extends Fragment {

    private String speed, rpm, fuelLevel, oilTemp, consumption;
    //BluetoothSocket socket;
    //private A activity;
    //View view;

    //public BluetoothSocket socket = null;

    OBD2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //activity = (MainActivity) getActivity();
        View view = li.inflate(R.layout.fragment_obd2, vg, false);

        initBT();

        new Thread(new Runnable() {public void run() { getODBdata(); }});
        //getODBdata(socket);
//        TimerTask repeatedTask = new TimerTask() {
//            public void run() { getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() { updateView(view); }}); }};
//        Timer timer = new Timer("Timer");
//        timer.scheduleAtFixedRate(repeatedTask, 1000, 1000);

//        new Thread(new Runnable() {public void run() { getODBdata(); }});
        return view;
    }

    private void initBT() {
        //Gets list of paired devices
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Tools.msg(getContext(),"Couldn't find Bluetooth");
            return;
        }

        final ArrayList<BluetoothDevice> paired =
                new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
        if (paired.size() == 0) {
            Tools.msg(getContext(),"No paired devices found");
            return;
        }

        //Checks if device is named OBDII and connects
        for (BluetoothDevice device : paired) {
            if (device.getName().toUpperCase().equals("OBDII")) {
                Tools.msg(getContext(), "Bluetooth OBDII device found: " + device.getName());
                getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                        .putString("btaddress", device.getAddress()).apply();
                connectBT(device.getAddress());
                return;
            }
        }
        //If no applicable devices have been found, open list of paired devices
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                selectBT(paired);
//            }
//        });

        selectBT(paired);
    }

    private void selectBT(final ArrayList<BluetoothDevice> paired) {
        //Creates list for dialog with paired bluetooth devices
        ArrayList<String> list = new ArrayList<>();
        for (BluetoothDevice device : paired) list.add(device.getName());
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_singlechoice, list);

        //Creates dialog for choosing bluetooth device
        new AlertDialog.Builder(getContext())
                .setTitle("Choose Bluetooth device")
                .setSingleChoiceItems(adp, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        final String address = paired.get(pos).getAddress();
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().
                                putString("btaddress", address).apply();
                        connectBT(address);
                    }
                })
                .show();
    }

    BluetoothSocket socket = null;
    private void connectBT(String address) {
        //Connects to OBDII device and opens Fragment
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            //getODBdata(socket);
        } catch (IOException e) {
            Tools.msg(getActivity(),"Couldn't connect to ELM327 Bluetooth ODBII adapter");
            e.printStackTrace();
        }


        //getODBdata(socket);
    }

    private void getODBdata() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) getView().findViewById(R.id.txt_time))
                                .setText(getString(R.string.time, formatter.format(new Date())));

                        ((TextView) getView().findViewById(R.id.txt_speed))
                                .setText(getString(R.string.speed, speed));
                        ((TextView) getView().findViewById(R.id.txt_rpm))
                                .setText(getString(R.string.rpm, rpm));

                    }});


                if (socket != null && socket.isConnected()){

                    try {
                //BluetoothSocket socket = socket;
                //Thread.sleep(100);
                RPMCommand engineRpmCommand = new RPMCommand();
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                rpm = engineRpmCommand.getFormattedResult();

                SpeedCommand speedCommand = new SpeedCommand();
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                speed = speedCommand.getFormattedResult();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                 }
//                OilTempCommand oilTempCommand = new OilTempCommand();
//                oilTempCommand.run(socket.getInputStream(), socket.getOutputStream());
//                oilTemp = oilTempCommand.getFormattedResult();
//
//                ConsumptionRateCommand consumptionRateCommand = new ConsumptionRateCommand();
//                consumptionRateCommand.run(socket.getInputStream(), socket.getOutputStream());
//                consumption = consumptionRateCommand.getFormattedResult();
//
//                FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();
//                fuelLevelCommand.run(socket.getInputStream(), socket.getOutputStream());
//                fuelLevel = fuelLevelCommand.getFormattedResult();
            }

    }

    private SimpleDateFormat formatter = new SimpleDateFormat(
            "HH:mm:ss dd/MM/yyyy ", Locale.getDefault());

    private void updateView(View view) {
        Date date = new Date();

        ((TextView) view.findViewById(R.id.txt_time))
                .setText(getString(R.string.time, formatter.format(date)));
        ((TextView) view.findViewById(R.id.txt_speed))
                .setText(getString(R.string.speed, speed));
        ((TextView) view.findViewById(R.id.txt_rpm))
                .setText(getString(R.string.rpm, rpm));

//        try {
//            ((TextView) view.findViewById(R.id.txt_oiltemp))
//                    .setText("Socket: " + socket.isConnected()
//                            + "Con Type: " + +socket.getConnectionType()
//                            + "Max Recieve " + socket.getMaxReceivePacketSize()
//                            + "Max Transmit " + socket.getMaxTransmitPacketSize()
//                            + " " + socket.getRemoteDevice().getName() + " ");
//        } catch (Exception ignored){};



//        ((TextView) view.findViewById(R.id.txt_oiltemp))
//                .setText(getString(R.string.oilTemp, oilTemp));
//        ((TextView) view.findViewById(R.id.txt_fuellevel))
//                .setText(getString(R.string.fuelLevel, fuelLevel));
//        ((TextView) view.findViewById(R.id.txt_consumption))
//                .setText(getString(R.string.consumption, consumption));
    }
}




