package com.ivyiot.playback;

/**
 * 云回放时间回调
 */
public interface ITimeLineListener {

    void onSetCloudVideoColor(EVideoType type);
    /** 时间轴开始滑动*/
    void onTimeScrollStart();
    /** 时间轴滑动中*/
    void onTimeScrollMove(long time);
    /** 时间轴滑动完成*/
    void onTimeScrollEnd(long time);
}
