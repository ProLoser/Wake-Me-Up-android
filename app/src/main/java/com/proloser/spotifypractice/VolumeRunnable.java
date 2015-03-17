package com.proloser.spotifypractice;

import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

/**
 * Created by dean on 3/17/15.
 */
public class VolumeRunnable implements Runnable{

    private AudioManager mAudioManager;
    private Handler mHandlerThatWillIncreaseVolume;
    private int incrementDelay;
    private int maxVolume;
    VolumeRunnable(AudioManager audioManager, Handler handler, int duration, int maxVolume){
        this.mAudioManager = audioManager;
        this.mHandlerThatWillIncreaseVolume = handler;
        this.maxVolume = maxVolume; // = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        this.incrementDelay = duration / maxVolume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    @Override
    public void run(){
        int currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentAlarmVolume < maxVolume) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);

            Log.d("VolumeRunnable", "Volume increased: " + currentAlarmVolume + " of " + maxVolume);
            mHandlerThatWillIncreaseVolume.postDelayed(this, incrementDelay); // recursively call this runnable again with delay
        }

    }
}
