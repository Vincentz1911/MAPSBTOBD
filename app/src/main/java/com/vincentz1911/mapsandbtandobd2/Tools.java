package com.vincentz1911.mapsandbtandobd2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

class Tools {

    static void msg(final Activity activity, final String text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    static void checkPermissions(Activity activity) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(activity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private static boolean hasPermissions(Activity activity, String... permissions) {
        if (activity != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
