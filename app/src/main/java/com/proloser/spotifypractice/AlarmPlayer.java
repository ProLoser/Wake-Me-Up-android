package com.proloser.spotifypractice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.playback.PlayerStateCallback;

import kaaes.spotify.webapi.android.SpotifyApi;

/**
 * Created by dean on 3/25/15.
 */
public class AlarmPlayer extends Activity implements ConnectionStateCallback, PlayerNotificationCallback, Player.InitializationObserver {

    private Player mPlayer;
    private static final String CLIENT_ID = "447cbdc30cb443c79ab069b4dec478a8";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        SharedPreferences prefs = this.getSharedPreferences("com.proloser.spotifypractice", Context.MODE_PRIVATE);
        Config playerConfig = new Config(this, prefs.getString("com.proloser.spotifypractice.accessToken", ""), CLIENT_ID);

        mPlayer = Spotify.getPlayer(playerConfig, this, this);
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onPlaybackEvent(PlayerNotificationCallback.EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());

        if (eventType == EventType.TRACK_CHANGED) {
            TextView myTextView = (TextView) findViewById(R.id.textView);
            myTextView.setText("Playing: " + playerState.trackUri);
        }
    }

    @Override
    public void onPlaybackError(PlayerNotificationCallback.ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onInitialized(Player player) {
        Intent intent = getIntent();

        mPlayer.addConnectionStateCallback(AlarmPlayer.this);
        mPlayer.addPlayerNotificationCallback(AlarmPlayer.this);
        Log.d("AlarmPlayer", "Volume: " + String.valueOf(intent.getIntExtra("volume", 8)));
        Log.d("AlarmPlayer", "Volume Ramp Time: 60x1000x" + String.valueOf(intent.getIntExtra("volumeRampTime", 8)));
        Log.d("AlarmPlayer", "URI: " + intent.getStringExtra("URI"));
        mPlayer.play(intent.getStringExtra("URI"));
        mPlayer.setShuffle(true);
        // Control volume
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Handler someHandler = new Handler();
        someHandler.post(new VolumeRunnable(audioManager, someHandler, intent.getIntExtra("volumeRampTime", 0) * 60 * 1000, intent.getIntExtra("volume", 8)));
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
    }

    public void playPause(View view) {
        Log.d("MainActivity", "Pressed playPause");
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    mPlayer.pause();
                } else {
                    mPlayer.resume();
                }
            }
        });
    }

    public void next(View view) {
        Log.d("MainActivity", "Pressed next");
        mPlayer.skipToNext();
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }


}