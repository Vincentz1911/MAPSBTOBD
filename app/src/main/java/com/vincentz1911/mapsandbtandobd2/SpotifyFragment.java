package com.vincentz1911.mapsandbtandobd2;


import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.PlaybackSpeed;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpotifyFragment extends Fragment {

    private static final String CLIENT_ID = "51f679e062be42a490b49754fcf073d8";
    private static final String REDIRECT_URI = "MapsandBTandOBD2://callback";
    private static SpotifyAppRemote mSpotifyAppRemote;
    private final ErrorCallback mErrorCallback = this::logError;
    String TAG = "Spotify";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ImageView mCoverArtImageView;
    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;

    public SpotifyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_spotify, vg, false);

        initUI(view);
        initOnClick(view);
        // Inflate the layout for this fragment
        return view;
    }

    private void initUI(View view) {
        mCoverArtImageView = view.findViewById(R.id.image);

    }

    void initOnClick(View view) {

//        mPlayerStateSubscription =
//                (Subscription<PlayerState>)
//                        mSpotifyAppRemote
//                                .getPlayerApi()
//                                .subscribeToPlayerState()
//                                .setEventCallback(mPlayerStateEventCallback)
//                                .setLifecycleCallback(
//                                        new Subscription.LifecycleCallback() {
//                                            @Override
//                                            public void onStart() {
//                                                logMessage("Event: start");
//                                            }
//
//                                            @Override
//                                            public void onStop() {
//                                                logMessage("Event: end");
//                                            }
//                                        })
//                                .setErrorCallback(
//                                        throwable -> {
////                                            mPlayerStateButton.setVisibility(View.INVISIBLE);
////                                            mSubscribeToPlayerStateButton.setVisibility(View.VISIBLE);
//                                            logError(throwable);
//                                        });


        (view.findViewById(R.id.play_pause_button)).

    setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick (View v){
            mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                if (playerState.isPaused) {
                    mSpotifyAppRemote.getPlayerApi().resume().setResultCallback(empty ->
                            logMessage(getString(R.string.command_feedback, "play")))
                            .setErrorCallback(mErrorCallback);


                    // Get image from track
                    mSpotifyAppRemote
                            .getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(
                                    bitmap -> {
                                        mCoverArtImageView.setImageBitmap(bitmap);

                                    });
                } else {
                    mSpotifyAppRemote.getPlayerApi().pause().setResultCallback(empty ->
                            logMessage(getString(R.string.command_feedback, "pause")))
                            .setErrorCallback(mErrorCallback);
                }
            });
        }
    });
}


    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {

                    // Get image from track
                    mSpotifyAppRemote
                            .getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(
                                    bitmap -> {
                                        mCoverArtImageView.setImageBitmap(bitmap);

                                    });
                }
            };

    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();

        SpotifyAppRemote.connect(getContext(), connectionParams, new Connector.ConnectionListener() {
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
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:1uTiiBdwPlUzdDPt0qcHYj");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }


    private void logError(Throwable throwable) {
        Toast.makeText(getContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "", throwable);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(getContext(), msg, duration).show();
        Log.d(TAG, msg);
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message).create().show();
    }
}
