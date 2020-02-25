package com.vincentz1911.mapsandbtandobd2;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.Track;

public class MainActivity extends FragmentActivity {


    private static final String CLIENT_ID = "51f679e062be42a490b49754fcf073d8";
    private static final String REDIRECT_URI = "comvincentzmapsandbtandodb2://callback";
    private static SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_bottom, new MapFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_top, new SpotifyFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fl_left_top, new oldSpotifyFragment(), "").commit();

    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d("MainActivity", "Connected! Yay!");
                connected();
            }

            public void onFailure(Throwable throwable) {
                Log.e("MyActivity", throwable.getMessage(), throwable);
                // Something went wrong when attempting to connect! Handle errors here
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().resume();
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;

//                    if (track != null) {
//                        // Update progressbar
//                        if (playerState.playbackSpeed > 0) {
//                            mTrackProgressBar.unpause();
//                        } else {
//                            mTrackProgressBar.pause();
//                        }
//
//                        // Invalidate play / pause
//                        if (playerState.isPaused) {
//                            mPlayPauseButton.setImageResource(R.drawable.ic_play);
//                        } else {
//                            mPlayPauseButton.setImageResource(R.drawable.ic_pause);
//                        }
//
//
//                        // Get image from track
//                        mSpotifyAppRemote.getImagesApi()
//                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
//                                .setResultCallback(bitmap -> {
//                                    mCoverArtImageView.setImageBitmap(bitmap);
//                                });
//
//                        txt_artist.setText(track.artist.name);
//                        txt_album.setText(track.album.name);
//                        txt_song.setText(track.name);// + "(" + track.duration + ")"
//                        Log.d("MainActivity", track.name + " by " + track.artist.name);
//                    }
                });
    }
}
