package com.vincentz1911.mapsandbtandobd2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class oldSpotifyFragment extends Fragment {

    private static final String TAG = "SPOTIFY TAG";

    private static SpotifyAppRemote mSpotifyAppRemote;

    private static final String TRACK_URI = "spotify:track:4IWZsfEkaK49itBwCTFDXQ";
    private static final String ALBUM_URI = "spotify:album:4nZ5wPL5XxSY2OuDgbnYdc";
    private static final String ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2";
    private static final String PLAYLIST_URI = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF";
    private static final String PODCAST_URI = "spotify:show:2tgPYIeGErjk6irHRhk9kj";


    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Button mConnectButton, mConnectAuthorizeButton;
    Button mSubscribeToPlayerContextButton;
    Button mPlayerContextButton;
    Button mSubscribeToPlayerStateButton;
    Button mPlayerStateButton;
    ImageView mCoverArtImageView;
    AppCompatTextView mImageLabel;
    AppCompatTextView mImageScaleTypeLabel;
    AppCompatImageButton mToggleShuffleButton;
    AppCompatImageButton mPlayPauseButton;
    AppCompatImageButton mToggleRepeatButton;
    AppCompatSeekBar mSeekBar;
    AppCompatImageButton mPlaybackSpeedButton;

    List<View> mViews;
    TrackProgressBar mTrackProgressBar;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;
    Subscription<Capabilities> mCapabilitiesSubscription;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.oldfragment_spotify, container, false);


        mConnectButton = view.findViewById(R.id.connect_button);
        mConnectAuthorizeButton = view.findViewById(R.id.connect_authorize_button);
        mPlayerContextButton = view.findViewById(R.id.current_context_label);
        mSubscribeToPlayerContextButton = view.findViewById(R.id.subscribe_to_player_context_button);
        mCoverArtImageView = view.findViewById(R.id.image);
        mImageLabel = view.findViewById(R.id.image_label);
        mImageScaleTypeLabel = view.findViewById(R.id.image_scale_type_label);
        mPlayerStateButton = view.findViewById(R.id.current_track_label);
        mSubscribeToPlayerStateButton = view.findViewById(R.id.subscribe_to_player_state_button);
        mPlaybackSpeedButton = view.findViewById(R.id.playback_speed_button);
        mToggleRepeatButton = view.findViewById(R.id.toggle_repeat_button);
        mToggleShuffleButton = view.findViewById(R.id.toggle_shuffle_button);
        mPlayPauseButton = view.findViewById(R.id.play_pause_button);

        mSeekBar = view.findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
        mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        mTrackProgressBar = new TrackProgressBar(mSeekBar);

        mViews =
                Arrays.asList(
                        view.findViewById(R.id.disconnect_button),
                        mSubscribeToPlayerContextButton,
                        mSubscribeToPlayerStateButton,
                        mImageLabel,
                        mImageScaleTypeLabel,
                        mPlayPauseButton,
                        view.findViewById(R.id.seek_forward_button),
                        view.findViewById(R.id.seek_back_button),
                        view.findViewById(R.id.skip_prev_button),
                        view.findViewById(R.id.skip_next_button),
                        mToggleRepeatButton,
                        mToggleShuffleButton,
                        view.findViewById(R.id.connect_switch_to_local),
                        view.findViewById(R.id.play_podcast_button),
                        view.findViewById(R.id.play_track_button),
                        view.findViewById(R.id.play_album_button),
                        view.findViewById(R.id.play_artist_button),
                        view.findViewById(R.id.play_playlist_button),
                        view.findViewById(R.id.subscribe_to_capabilities),
                        view.findViewById(R.id.get_collection_state),
                        view.findViewById(R.id.remove_uri),
                        view.findViewById(R.id.save_uri),
                        view.findViewById(R.id.get_fitness_recommended_items_button),
                        mSeekBar);

        SpotifyAppRemote.setDebugMode(true);

        onDisconnected();
        //onConnectAndAuthorizedClicked(null);

        return view;
    }


    private final ErrorCallback mErrorCallback = this::logError;

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                    mPlayerContextButton.setText(
                            String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
                    mPlayerContextButton.setTag(playerContext);
                }
            };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {

                    Drawable drawable =
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.mediaservice_shuffle, getActivity().getTheme());
                    if (!playerState.playbackOptions.isShuffling) {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(mToggleShuffleButton.getDrawable(), Color.WHITE);
                    } else {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(
                                mToggleShuffleButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    }

                    if (playerState.playbackOptions.repeatMode == Repeat.ALL) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_all);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else if (playerState.playbackOptions.repeatMode == Repeat.ONE) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_one);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_off);
                        DrawableCompat.setTint(mToggleRepeatButton.getDrawable(), Color.WHITE);
                    }

                    mPlayerStateButton.setText(
                            String.format(
                                    Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name));
                    mPlayerStateButton.setTag(playerState);

                    // Update progressbar
                    if (playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }

                    // Invalidate play / pause
                    if (playerState.isPaused) {
                        mPlayPauseButton.setImageResource(R.drawable.btn_play);
                    } else {
                        mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                    }

                    // Invalidate playback speed
                    mPlaybackSpeedButton.setVisibility(View.VISIBLE);
                    if (playerState.playbackSpeed == 0.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_50);
                    } else if (playerState.playbackSpeed == 0.8f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_80);
                    } else if (playerState.playbackSpeed == 1f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_100);
                    } else if (playerState.playbackSpeed == 1.2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_120);
                    } else if (playerState.playbackSpeed == 1.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_150);
                    } else if (playerState.playbackSpeed == 2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_200);
                    } else if (playerState.playbackSpeed == 3f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_300);
                    }
                    if (playerState.track.isPodcast && playerState.track.isEpisode) {
                        mPlaybackSpeedButton.setEnabled(true);
                        mPlaybackSpeedButton.clearColorFilter();
                    } else {
                        mPlaybackSpeedButton.setEnabled(false);
                        mPlaybackSpeedButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                    }

                    // Get image from track
                    mSpotifyAppRemote
                            .getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(
                                    bitmap -> {
                                        mCoverArtImageView.setImageBitmap(bitmap);
                                        mImageLabel.setText(
                                                String.format(
                                                        Locale.ENGLISH, "%d x %d", bitmap.getWidth(), bitmap.getHeight()));
                                    });

                    // Invalidate seekbar length and position
                    mSeekBar.setMax((int) playerState.track.duration);
                    mTrackProgressBar.setDuration(playerState.track.duration);
                    mTrackProgressBar.update(playerState.playbackPosition);

                    mSeekBar.setEnabled(true);
                }
            };

//    @Override
//    public void onCreate(final Bundle savedInstanceState) {
//        getActivity().setTheme(R.style.AppTheme);
//        super.onCreate(savedInstanceState);
//        getActivity().setContentView(R.layout.app_remote_layout);
//
//        mConnectButton = getView().findViewById(R.id.connect_button);
//        mConnectAuthorizeButton = getView().findViewById(R.id.connect_authorize_button);
//        mPlayerContextButton = getView().findViewById(R.id.current_context_label);
//        mSubscribeToPlayerContextButton = getView().findViewById(R.id.subscribe_to_player_context_button);
//        mCoverArtImageView = getView().findViewById(R.id.image);
//        mImageLabel = getView().findViewById(R.id.image_label);
//        mImageScaleTypeLabel = getView().findViewById(R.id.image_scale_type_label);
//        mPlayerStateButton = getView().findViewById(R.id.current_track_label);
//        mSubscribeToPlayerStateButton = getView().findViewById(R.id.subscribe_to_player_state_button);
//        mPlaybackSpeedButton = getView().findViewById(R.id.playback_speed_button);
//        mToggleRepeatButton = getView().findViewById(R.id.toggle_repeat_button);
//        mToggleShuffleButton = getView().findViewById(R.id.toggle_shuffle_button);
//        mPlayPauseButton = getView().findViewById(R.id.play_pause_button);
//
//        mSeekBar = getView().findViewById(R.id.seek_to);
//        mSeekBar.setEnabled(false);
//        mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//
//        mTrackProgressBar = new oldSpotifyFragment().TrackProgressBar(mSeekBar);
//
//        mViews =
//                Arrays.asList(
//                        getView().findViewById(R.id.disconnect_button),
//                        mSubscribeToPlayerContextButton,
//                        mSubscribeToPlayerStateButton,
//                        mImageLabel,
//                        mImageScaleTypeLabel,
//                        mPlayPauseButton,
//                        getView().findViewById(R.id.seek_forward_button),
//                        getView().findViewById(R.id.seek_back_button),
//                        getView().findViewById(R.id.skip_prev_button),
//                        getView().findViewById(R.id.skip_next_button),
//                        mToggleRepeatButton,
//                        mToggleShuffleButton,
//                        getView().findViewById(R.id.connect_switch_to_local),
//                        getView().findViewById(R.id.play_podcast_button),
//                        getView().findViewById(R.id.play_track_button),
//                        getView().findViewById(R.id.play_album_button),
//                        getView().findViewById(R.id.play_artist_button),
//                        getView().findViewById(R.id.play_playlist_button),
//                        getView().findViewById(R.id.subscribe_to_capabilities),
//                        getView().findViewById(R.id.get_collection_state),
//                        getView().findViewById(R.id.remove_uri),
//                        getView().findViewById(R.id.save_uri),
//                        getView().findViewById(R.id.get_fitness_recommended_items_button),
//                        mSeekBar);
//
//        SpotifyAppRemote.setDebugMode(true);
//
//        onDisconnected();
//        onConnectAndAuthorizedClicked(null);
//    }



    private void onConnected() {
        for (View input : mViews) {
            input.setEnabled(true);
        }
        mConnectButton.setEnabled(false);
        mConnectButton.setText(R.string.connected);
        mConnectAuthorizeButton.setEnabled(false);
        mConnectAuthorizeButton.setText(R.string.connected);

        onSubscribedToPlayerStateButtonClicked(null);
        onSubscribedToPlayerContextButtonClicked(null);
    }

    private void onConnecting() {
        mConnectButton.setEnabled(false);
        mConnectButton.setText(R.string.connecting);
        mConnectAuthorizeButton.setEnabled(false);
        mConnectAuthorizeButton.setText(R.string.connecting);
    }

    private void onDisconnected() {
        for (View view : mViews) {
            view.setEnabled(false);
        }
        mConnectButton.setEnabled(true);
        mConnectButton.setText(R.string.connect);
        mConnectAuthorizeButton.setEnabled(true);
        mConnectAuthorizeButton.setText(R.string.authorize);
        mCoverArtImageView.setImageResource(R.drawable.widget_placeholder);
        mPlayerContextButton.setText(R.string.title_player_context);
        mPlayerStateButton.setText(R.string.title_current_track);
        mToggleRepeatButton.clearColorFilter();
        mToggleRepeatButton.setImageResource(R.drawable.btn_repeat);
        mToggleShuffleButton.clearColorFilter();
        mToggleShuffleButton.setImageResource(R.drawable.btn_shuffle);
        mPlayerContextButton.setVisibility(View.INVISIBLE);
        mSubscribeToPlayerContextButton.setVisibility(View.VISIBLE);
        mPlayerStateButton.setVisibility(View.INVISIBLE);
        mSubscribeToPlayerStateButton.setVisibility(View.VISIBLE);
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }

//    public void onConnectClicked(View view) {
//        onConnecting();
//        connect(false);
//    }
//
//    public void onConnectAndAuthorizedClicked(View view) {
//        onConnecting();
//        connect(true);
//    }



    public void onDisconnectClicked(View v) {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        onDisconnected();
    }

    public void onImageClicked(View view) {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .getPlayerState()
                    .setResultCallback(
                            playerState -> {
                                PopupMenu menu = new PopupMenu(getContext(), view);

                                menu.getMenu().add(720, 720, 0, "Large (720px)");
                                menu.getMenu().add(480, 480, 1, "Medium (480px)");
                                menu.getMenu().add(360, 360, 2, "Small (360px)");
                                menu.getMenu().add(240, 240, 3, "X Small (240px)");
                                menu.getMenu().add(144, 144, 4, "Thumbnail (144px)");

                                menu.show();

                                menu.setOnMenuItemClickListener(
                                        item -> {
                                            mSpotifyAppRemote
                                                    .getImagesApi()
                                                    .getImage(
                                                            playerState.track.imageUri, Image.Dimension.values()[item.getOrder()])
                                                    .setResultCallback(
                                                            bitmap -> {
                                                                mCoverArtImageView.setImageBitmap(bitmap);
                                                                mImageLabel.setText(
                                                                        String.format(
                                                                                Locale.ENGLISH,
                                                                                "%d x %d",
                                                                                bitmap.getWidth(),
                                                                                bitmap.getHeight()));
                                                            });
                                            return false;
                                        });
                            })
                    .setErrorCallback(mErrorCallback);
        }
    }

    public void onImageScaleTypeClicked(View view) {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .getPlayerState()
                    .setResultCallback(
                            playerState -> {
                                PopupMenu menu = new PopupMenu(getContext(), view);

                                menu.getMenu().add(0, ImageView.ScaleType.CENTER.ordinal(), 0, "CENTER");
                                menu.getMenu().add(1, ImageView.ScaleType.CENTER_CROP.ordinal(), 1, "CENTER_CROP");
                                menu.getMenu()
                                        .add(2, ImageView.ScaleType.CENTER_INSIDE.ordinal(), 2, "CENTER_INSIDE");
                                menu.getMenu().add(3, ImageView.ScaleType.MATRIX.ordinal(), 3, "MATRIX");
                                menu.getMenu().add(4, ImageView.ScaleType.FIT_CENTER.ordinal(), 4, "FIT_CENTER");
                                menu.getMenu().add(4, ImageView.ScaleType.FIT_XY.ordinal(), 5, "FIT_XY");

                                menu.show();

                                menu.setOnMenuItemClickListener(
                                        item -> {
                                            mCoverArtImageView.setScaleType(
                                                    ImageView.ScaleType.values()[item.getItemId()]);
                                            mImageScaleTypeLabel.setText(
                                                    ImageView.ScaleType.values()[item.getItemId()].toString());
                                            return false;
                                        });
                            })
                    .setErrorCallback(mErrorCallback);
        }
    }

    public void onPlayPodcastButtonClicked(View view) {
        playUri(PODCAST_URI);
    }

    public void onPlayTrackButtonClicked(View view) {
        playUri(TRACK_URI);
    }

    public void onPlayAlbumButtonClicked(View view) {
        playUri(ALBUM_URI);
    }

    public void onPlayArtistButtonClicked(View view) {
        playUri(ARTIST_URI);
    }

    public void onPlayPlaylistButtonClicked(View view) {
        playUri(PLAYLIST_URI);
    }

    private void playUri(String uri) {
        mSpotifyAppRemote
                .getPlayerApi()
                .play(uri)
                .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "play")))
                .setErrorCallback(mErrorCallback);
    }

    public void showCurrentPlayerContext(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerContext", gson.toJson(view.getTag()));
        }
    }

    public void showCurrentPlayerState(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerState", gson.toJson(view.getTag()));
        }
    }

    public void onToggleShuffleButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .toggleShuffle()
                .setResultCallback(
                        empty -> logMessage(getString(R.string.command_feedback, "toggle shuffle")))
                .setErrorCallback(mErrorCallback);
    }

    public void onToggleRepeatButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .toggleRepeat()
                .setResultCallback(
                        empty -> logMessage(getString(R.string.command_feedback, "toggle repeat")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSkipPreviousButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipPrevious()
                .setResultCallback(
                        empty -> logMessage(getString(R.string.command_feedback, "skip previous")))
                .setErrorCallback(mErrorCallback);
    }

    public void onPlayPauseButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                mSpotifyAppRemote
                                        .getPlayerApi()
                                        .resume()
                                        .setResultCallback(
                                                empty -> logMessage(getString(R.string.command_feedback, "play")))
                                        .setErrorCallback(mErrorCallback);
                            } else {
                                mSpotifyAppRemote
                                        .getPlayerApi()
                                        .pause()
                                        .setResultCallback(
                                                empty -> logMessage(getString(R.string.command_feedback, "pause")))
                                        .setErrorCallback(mErrorCallback);
                            }
                        });
    }

    public void onSkipNextButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipNext()
                .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "skip next")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSeekBack(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .seekToRelativePosition(-15000)
                .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "seek back")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSeekForward(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .seekToRelativePosition(15000)
                .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "seek fwd")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSubscribeToCapabilitiesClicked(View view) {

        if (mCapabilitiesSubscription != null && !mCapabilitiesSubscription.isCanceled()) {
            mCapabilitiesSubscription.cancel();
            mCapabilitiesSubscription = null;
        }

        mCapabilitiesSubscription =
                (Subscription<Capabilities>)
                        mSpotifyAppRemote
                                .getUserApi()
                                .subscribeToCapabilities()
                                .setEventCallback(
                                        capabilities ->
                                                logMessage(
                                                        getString(
                                                                R.string.on_demand_feedback,
                                                                Boolean.valueOf(capabilities.canPlayOnDemand))))
                                .setErrorCallback(mErrorCallback);

        mSpotifyAppRemote
                .getUserApi()
                .getCapabilities()
                .setResultCallback(
                        capabilities ->
                                logMessage(
                                        getString(
                                                R.string.on_demand_feedback,
                                                Boolean.valueOf(capabilities.canPlayOnDemand))))
                .setErrorCallback(mErrorCallback);
    }

    public void onGetCollectionStateClicked(View view) {
        mSpotifyAppRemote
                .getUserApi()
                .getLibraryState(TRACK_URI)
                .setResultCallback(
                        libraryState ->
                                showDialog(
                                        getString(R.string.command_response, getString(R.string.get_collection_state)),
                                        gson.toJson(libraryState)))
                .setErrorCallback(this::logError);
    }

    public void onRemoveUriClicked(View view) {
        mSpotifyAppRemote
                .getUserApi()
                .removeFromLibrary(TRACK_URI)
                .setResultCallback(
                        empty -> getString(R.string.command_feedback, getString(R.string.remove_uri)))
                .setErrorCallback(this::logError);
    }

    public void onSaveUriClicked(View view) {
        mSpotifyAppRemote
                .getUserApi()
                .addToLibrary(TRACK_URI)
                .setResultCallback(
                        empty -> logMessage(getString(R.string.command_feedback, getString(R.string.save_uri))))
                .setErrorCallback(this::logError);
    }

    public void onGetFitnessRecommendedContentItemsClicked(View view) {
        mSpotifyAppRemote
                .getContentApi()
                .getRecommendedContentItems(ContentApi.ContentType.FITNESS)
                .setResultCallback(
                        listItems -> {
                            final CountDownLatch latch = new CountDownLatch(listItems.items.length);
                            final List<ListItem> combined = new ArrayList<>(50);
                            for (int j = 0; j < listItems.items.length; j++) {
                                if (listItems.items[j].playable) {
                                    combined.add(listItems.items[j]);
                                    handleLatch(latch, combined);
                                } else {
                                    mSpotifyAppRemote
                                            .getContentApi()
                                            .getChildrenOfItem(listItems.items[j], 3, 0)
                                            .setResultCallback(
                                                    childListItems -> {
                                                        combined.addAll(Arrays.asList(childListItems.items));
                                                        handleLatch(latch, combined);
                                                    })
                                            .setErrorCallback(mErrorCallback);
                                }
                            }
                        })
                .setErrorCallback(mErrorCallback);
    }

    private void handleLatch(CountDownLatch latch, List<ListItem> combined) {
        latch.countDown();
        if (latch.getCount() == 0) {
            showDialog(
                    getString(R.string.command_response, getString(R.string.browse_content)),
                    gson.toJson(combined));
        }
    }

    public void onConnectSwitchToLocalClicked(View view) {
        mSpotifyAppRemote
                .getConnectApi()
                .connectSwitchToLocalDevice()
                .setResultCallback(
                        empty ->
                                logMessage(
                                        getString(
                                                R.string.command_feedback, getString(R.string.connect_switch_to_local))))
                .setErrorCallback(mErrorCallback);
    }

    public void onSubscribedToPlayerContextButtonClicked(View view) {
        if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
            mPlayerContextSubscription.cancel();
            mPlayerContextSubscription = null;
        }

        mPlayerContextButton.setVisibility(View.VISIBLE);
        mSubscribeToPlayerContextButton.setVisibility(View.INVISIBLE);

        mPlayerContextSubscription =
                (Subscription<PlayerContext>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerContext()
                                .setEventCallback(mPlayerContextEventCallback)
                                .setErrorCallback(
                                        throwable -> {
                                            mPlayerContextButton.setVisibility(View.INVISIBLE);
                                            mSubscribeToPlayerContextButton.setVisibility(View.VISIBLE);
                                            logError(throwable);
                                        });
    }

    public void onSubscribedToPlayerStateButtonClicked(View view) {

        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

        mPlayerStateButton.setVisibility(View.VISIBLE);
        mSubscribeToPlayerStateButton.setVisibility(View.INVISIBLE);

        mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(mPlayerStateEventCallback)
                                .setLifecycleCallback(
                                        new Subscription.LifecycleCallback() {
                                            @Override
                                            public void onStart() {
                                                logMessage("Event: start");
                                            }

                                            @Override
                                            public void onStop() {
                                                logMessage("Event: end");
                                            }
                                        })
                                .setErrorCallback(
                                        throwable -> {
                                            mPlayerStateButton.setVisibility(View.INVISIBLE);
                                            mSubscribeToPlayerStateButton.setVisibility(View.VISIBLE);
                                            logError(throwable);
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

    public void onPlaybackSpeedButtonClicked(View view) {
        PopupMenu menu = new PopupMenu(getContext(), view);

        menu.getMenu().add(50, 50, 0, "0.5x");
        menu.getMenu().add(80, 80, 1, "0.8x");
        menu.getMenu().add(100, 100, 2, "1x");
        menu.getMenu().add(120, 120, 3, "1.2x");
        menu.getMenu().add(150, 150, 4, "1.5x");
        menu.getMenu().add(200, 200, 5, "2x");
        menu.getMenu().add(300, 300, 6, "3x");

        menu.show();

        menu.setOnMenuItemClickListener(
                item -> {
                    mSpotifyAppRemote
                            .getPlayerApi()
                            .setPodcastPlaybackSpeed(PlaybackSpeed.PodcastPlaybackSpeed.values()[item.getOrder()])
                            .setResultCallback(
                                    empty ->
                                            logMessage(
                                                    getString(
                                                            R.string.command_feedback,
                                                            getString(R.string.play_podcast_button_label))))
                            .setErrorCallback(mErrorCallback);
                    return false;
                });
    }

    private class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;

        private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .seekTo(seekBar.getProgress())
                                .setErrorCallback(mErrorCallback);
                    }
                };

        private final Runnable mSeekRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        int progress = mSeekBar.getProgress();
                        mSeekBar.setProgress(progress + LOOP_DURATION);
                        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
                    }
                };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress) {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }


}
