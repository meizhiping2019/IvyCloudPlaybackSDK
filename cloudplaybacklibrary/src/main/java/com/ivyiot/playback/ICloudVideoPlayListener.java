package com.ivyiot.playback;

/**
 * 云回放事件回调
 */
public interface ICloudVideoPlayListener {

    void onStartPlay();

    void onPlaying(int position,int duration);

    void onCompletion();

    void onError();
}
