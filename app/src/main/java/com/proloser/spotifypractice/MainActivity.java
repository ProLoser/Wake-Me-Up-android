package com.proloser.spotifypractice;

// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.playback.ConnectionStateCallback;

import java.util.Calendar;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements ConnectionStateCallback {

    private static final String CLIENT_ID = "447cbdc30cb443c79ab069b4dec478a8";
    private static final String REDIRECT_URI = "com-proloser-spotilarm://callback";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    private PendingIntent alarmIntent;
    private AlarmManager alarmMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar seekbar = (SeekBar)findViewById(R.id.alarmVolume);
        seekbar.setOnSeekBarChangeListener(new DualOnSeekBarChangeListener((TextView)findViewById(R.id.alarmVolumeLabel)));
        seekbar = (SeekBar)findViewById(R.id.alarmVolumeRampTime);
        seekbar.setOnSeekBarChangeListener(new DualOnSeekBarChangeListener((TextView)findViewById(R.id.alarmVolumeRampTimeLabel)));

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "playlist-modify-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                // Store the access token
                SharedPreferences prefs = this.getSharedPreferences("com.proloser.spotifypractice", Context.MODE_PRIVATE);
                prefs.edit().putString("com.proloser.spotifypractice.accessToken", response.getAccessToken()).apply();

            }
        }

    }
    public void setAlarm(View view) {
        Log.d("MainActivity", "Pressed setAlarm");
        AlarmManager alarmMgr;


        // Store the playlist URI
        SharedPreferences prefs = this.getSharedPreferences("com.proloser.spotifypractice", Context.MODE_PRIVATE);
        EditText uriEditText = (EditText)findViewById(R.id.uriEditText);

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        decorateIntent(intent);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }

    public void cancelAlarm(View view) {
        Context context = getApplicationContext();

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);
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
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void goToPlayer(View view) {
        Intent startAlarm = new Intent(this, AlarmPlayer.class);
        decorateIntent(startAlarm);
        startActivity(startAlarm);
    }

    private void decorateIntent(Intent intent) {
        EditText uriEditText = (EditText)findViewById(R.id.uriEditText);
        SeekBar alarmVolume = (SeekBar)findViewById(R.id.alarmVolume);
        SeekBar alarmVolumeRampTime = (SeekBar)findViewById(R.id.alarmVolumeRampTime);
        intent.putExtra("URI", uriEditText.getText().toString());
        intent.putExtra("volume", alarmVolume.getProgress());
        intent.putExtra("volumeRampTime", alarmVolumeRampTime.getProgress());
    }

    public void getPlaylists(View view) {
        // Store the access token
        SharedPreferences prefs = this.getSharedPreferences("com.proloser.spotifypractice", Context.MODE_PRIVATE);

        // Load up spotify API
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(prefs.getString("com.proloser.spotifypractice.accessToken", ""));

        final SpotifyService spotify = api.getService();


        spotify.getMe(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                spotify.getPlaylists(user.id, new Callback<Pager<Playlist>>() {
                    @Override
                    public void success(Pager<Playlist> playlists, Response playlistResponse) {

                        for (Playlist playlist : playlists.items) {
                            Log.d("MainActivity", "Playlist: " + playlist.name);
                        }

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e("MainActivity", "Could not retrieve playlists: " + retrofitError.getMessage());
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("MainActivity", "Could not retrieve user profile: " + retrofitError.getMessage());
            }
        });
    }


    private class DualOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private TextView label;

        public DualOnSeekBarChangeListener(TextView label) {
            this.label = label;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            label.setText(String.valueOf(progress));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            // not used
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            // not used
        }
    }
}