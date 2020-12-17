package com.ivyiot.playback;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class IvyVideo implements Comparator {
    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 结束时间
     */
    private long endTime;
    /**
     * 录像类型
     */
    private EVideoType videoType = EVideoType.MOTION;
    /***
     * 回放地址
     */
    private String url;

    /**
     * 获取开始时间
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取录像类型
     */
    public EVideoType getVideoType() {
        return videoType;
    }

    /**
     * 设置录像类型
     */
    public void setVideoType(EVideoType videoType) {
        this.videoType = videoType;
    }

    /**
     * 获取录像播放地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置录像播放地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int compare(Object arg0, Object arg1) {
        IvyVideo cloud_a = (IvyVideo) arg0;
        IvyVideo cloud_b = (IvyVideo) arg1;
        return (int) (cloud_a.getStartTime() - cloud_b.getStartTime());
    }
}