package com.vincentz1911.mapsandbtandobd2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.vincentz1911.mapsandbtandobd2.obd.commands.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.control.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.engine.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.fuel.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.pressure.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.protocol.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.temperature.*;


public class OBD2Fragment extends Fragment {

    private String speed, rpm, fuelLevel, oilTemp, consumption;
    private TextView txt_speed, txt_rpm;
    private Thread getInfoThread, updateInfoThread;
    private SharedPreferences sharedPref;
    private String deviceAddress;
    private BluetoothSocket socket = null;

    public OBD2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_obd2, container, false);
        txt_speed = view.findViewById(R.id.txt_speed);
        txt_rpm = view.findViewById(R.id.txt_rpm);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        deviceAddress = sharedPref.getString("btaddress", "");
        //if (deviceAddress.equals(""))
        getBluetoothDevice();

        getInfoThread = new Thread() {
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(100);
                        RPMCommand engineRpmCommand = new RPMCommand();
                        SpeedCommand speedCommand = new SpeedCommand();

                        OilTempCommand oilTempCommand = new OilTempCommand();
                        ConsumptionRateCommand consumptionRateCommand = new ConsumptionRateCommand();
                        FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();

                        fuelLevelCommand.run(socket.getInputStream(), socket.getOutputStream());
                        consumptionRateCommand.run(socket.getInputStream(), socket.getOutputStream());
                        oilTempCommand.run(socket.getInputStream(), socket.getOutputStream());
                        engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                        speedCommand.run(socket.getInputStream(), socket.getOutputStream());

                        consumption = consumptionRateCommand.getFormattedResult();
                        fuelLevel = fuelLevelCommand.getFormattedResult();
                        oilTemp = oilTempCommand.getFormattedResult();
                        speed = speedCommand.getFormattedResult();
                        rpm = engineRpmCommand.getFormattedResult();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        updateInfoThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(100);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_speed.setText("speed: " + speed);
                                txt_rpm.setText("rpm: " + rpm);

                                ((TextView)view.findViewById(R.id.txt_oiltemp)).setText(oilTemp);
                                ((TextView)view.findViewById(R.id.txt_fuellevel)).setText(fuelLevel);
                                ((TextView)view.findViewById(R.id.txt_consumption)).setText(consumption);


                                // update TextView here!
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return view;
    }

    private void getBluetoothDevice() {
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[0]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                deviceAddress = devices.get(position);
                sharedPref.edit().putString("btaddress", deviceAddress).apply();
                dialog.dismiss();
                connect();
            }
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

    private void connect() {
        try {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getInfoThread.start();
        updateInfoThread.start();
    }
}




