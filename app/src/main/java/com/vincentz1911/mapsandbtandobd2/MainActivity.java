package com.vincentz1911.mapsandbtandobd2;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_bottom, new MapFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big, new SpotifyFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fl_left_top, new oldSpotifyFragment(), "").commit();

    }
}
