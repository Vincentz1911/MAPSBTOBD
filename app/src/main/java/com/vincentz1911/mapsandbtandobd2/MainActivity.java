package com.vincentz1911.mapsandbtandobd2;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class MainActivity extends FragmentActivity {

    private static final String CLIENT_ID = "51f679e062be42a490b49754fcf073d8";
    private static final String REDIRECT_URI = "MapsandBTandOBD2://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big, new MapFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_top, new SpotifyFragment(), "").commit();

//        ((Button)findViewById(R.id.spotify)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(getBaseContext(), RemotePlayerActivity.class);
//                //myIntent.putExtra("key", value); //Optional parameters
//                startActivity(myIntent);
//            }
//        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
//                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();
//
//        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
//
//            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                mSpotifyAppRemote = spotifyAppRemote;
//                Log.d("MainActivity", "Connected! Yay!");
//
//                // Now you can start interacting with App Remote
//                connected();
//
//            }
//
//            public void onFailure(Throwable throwable) {
//                Log.e("MyActivity", throwable.getMessage(), throwable);
//                // Something went wrong when attempting to connect! Handle errors here
//            }
//        });
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
//    }
//
//    private void connected() {
//        // Play a playlist
//        //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:1uTiiBdwPlUzdDPt0qcHYj");
//
//        // Subscribe to PlayerState
//        mSpotifyAppRemote.getPlayerApi()
//                .subscribeToPlayerState()
//                .setEventCallback(playerState -> {
//                    final Track track = playerState.track;
//                    if (track != null) {
//                        Log.d("MainActivity", track.name + " by " + track.artist.name);
//                    }
//                });
//    }
}
