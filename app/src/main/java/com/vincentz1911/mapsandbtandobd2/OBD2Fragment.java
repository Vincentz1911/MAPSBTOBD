package com.vincentz1911.mapsandbtandobd2;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.IOException;

import com.vincentz1911.mapsandbtandobd2.obd.commands.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.engine.*;
import com.vincentz1911.mapsandbtandobd2.obd.commands.fuel.*;

public class OBD2Fragment extends Fragment {

    private String speed, rpm, fuelLevel, oilTemp, consumption;

    OBD2Fragment(final BluetoothSocket socket) {
        if (socket != null) new Thread(new Runnable() {
            public void run() {
                getODBdata(socket);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        final View view = li.inflate(R.layout.fragment_obd2, vg, false);
        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() { updateView(view);
                }
            });
        return view;
    }

    private void getODBdata(BluetoothSocket socket) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                //Thread.sleep(100);
                RPMCommand engineRpmCommand = new RPMCommand();
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                rpm = engineRpmCommand.getFormattedResult();

                SpeedCommand speedCommand = new SpeedCommand();
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                speed = speedCommand.getFormattedResult();

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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateView(View view) {
        ((TextView) view.findViewById(R.id.txt_speed))
                .setText(getString(R.string.speed, speed));
        ((TextView) view.findViewById(R.id.txt_rpm))
                .setText(getString(R.string.rpm, rpm));
//        ((TextView) view.findViewById(R.id.txt_oiltemp))
//                .setText(getString(R.string.oilTemp, oilTemp));
//        ((TextView) view.findViewById(R.id.txt_fuellevel))
//                .setText(getString(R.string.fuelLevel, fuelLevel));
//        ((TextView) view.findViewById(R.id.txt_consumption))
//                .setText(getString(R.string.consumption, consumption));
    }
}




