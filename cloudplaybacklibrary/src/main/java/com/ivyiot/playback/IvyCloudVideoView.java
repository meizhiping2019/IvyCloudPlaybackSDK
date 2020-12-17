package com.ivyiot.playback;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class IvyCloudVideoView extends PlayerView {
    private static final String TAG = "IvyCloudVideoView";
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private SimpleExoPlayer mMediaPlayer;
    /**播放地址*/
    private Uri mUri;
    private Context _mContext;
    private int mCurrentState;
    /***
     * Handler
     */
    private final Handler mHandler = new Handler();

    private static final int CHECK_PROGRESS_DELAY = 1000;

    /***
     * 更新视频拖动条位置  每隔一秒更新一次
     */
    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            if (isVideoPlaying() && null != mOnMoviePlayListener) {
                int position = getCurrentPosition();
                int duration = getDuration();
                mOnMoviePlayListener.onPlaying(position, duration);
            }
            mHandler.postDelayed(mProgressChecker, CHECK_PROGRESS_DELAY);
        }
    };

    public IvyCloudVideoView(Context context) {
        this(context, null);
    }

    public IvyCloudVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        _mContext = context;
        this.mMediaPlayer = null;
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();
        this.setUseController(false);
    }

    public IvyCloudVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    /**
     * 设置url播放
     */
    private void setVideoURI() {
//        if (null != mOnMoviePlayListener) {
//            mOnMoviePlayListener.onStartPlay();
//        }
        this.openVideo();
        this.requestLayout();
        this.invalidate();
    }

    /**
     * 停止播放
     */
    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
        }

        // this.clearSurface();
    }

    private void checkerStart() {
        mHandler.post(mProgressChecker);
    }

    /**
     * 取消检测
     */
   private void cancelCheck() {
        mHandler.removeCallbacks(mProgressChecker);
        mHandler.removeCallbacksAndMessages(null);
    }

    DefaultHlsExtractorFactory extractorsFactory;
    private void openVideo() {
        if (this.mUri != null) {
            this.stopPlayback();
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(_mContext)
                    .build();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();

            mMediaPlayer = new SimpleExoPlayer.Builder(_mContext)
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .build();
            mMediaPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Log.e(TAG, "onPlayerStateChanged. playWhenReady:" + playWhenReady + ",player state:" + playbackState);
                    /*
                     *    * The player does not have any media to play.
                     *      int STATE_IDLE = 1;
                     *
                     *    * The player is not able to immediately play from its current position. This state typically
                     *    * occurs when more data needs to be loaded.
                     *      int STATE_BUFFERING = 2;
                     *
                     *    * The player is able to immediately play from its current position. The player will be playing if
                     *    * {@link #getPlayWhenReady()} is true, and paused otherwise.
                     *      int STATE_READY = 3;
                     *
                     *    * The player has finished playing the media.
                     *      int STATE_ENDED = 4;
                     */
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                            mCurrentState = STATE_IDLE;
                        case Player.STATE_ENDED:
                            mCurrentState = STATE_PLAYBACK_COMPLETED;
                        case Player.STATE_IDLE:
                            mCurrentState = STATE_IDLE;
                        case Player.STATE_READY:
                            mCurrentState = STATE_PREPARED;
                    }

                    switch (playbackState) {
                        case Player.STATE_ENDED://视频播放完成
                            if (null != mOnMoviePlayListener) {
                                mOnMoviePlayListener.onCompletion();
                            }
                            Log.d(TAG, "onCompletion,MediaPlayer:Duration=");
                            break;
                        case Player.STATE_READY://开始播放视频
                            if(playWhenReady){
                                resumeCloudVideo();

                                if (null != mOnMoviePlayListener) {
                                    mOnMoviePlayListener.onStartPlay();
                                }
                                Log.d(TAG, "onStartPlay,MediaPlayer:Duration=");
                            }
                            break;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
//                    if(0 == error.type){
//                        if(STATE_PLAYING == mCurrentState){
//                            mCurrentState = STATE_PLAYBACK_COMPLETED;
//                            if (null != mOnMoviePlayListener) {
//                                mOnMoviePlayListener.onCompletion();
//                            }
//                            Log.d(TAG, "onCompletion,MediaPlayer:Duration=");
//                        }else{
//                            extractorsFactory = new DefaultHlsExtractorFactory(DefaultTsPayloadReaderFactory.FLAG_IGNORE_AAC_STREAM, true);
//                            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(_mContext,
//                                    Util.getUserAgent(_mContext, "ExoPlayerInfo"), null);
//                            DrmSessionManager<ExoMediaCrypto> drmSessionManager = DrmSessionManager.getDummyDrmSessionManager();
//                            HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
//                                    .setDrmSessionManager(drmSessionManager)
//                                    .setAllowChunklessPreparation(true)
//                                    .setExtractorFactory(extractorsFactory)
//                                    .createMediaSource(mUri);
//
//
//                            //play
//                            mMediaPlayer.setPlayWhenReady(true);
//                            mMediaPlayer.prepare(mediaSource);
//                        }
//
//                    }else {
                        mCurrentState = STATE_ERROR;
                        if (null != mOnMoviePlayListener) {
                            mOnMoviePlayListener.onError();
                        }
                        stopPlayback();
                        Log.d(TAG, "onError what=" + error.getMessage() + ",extra=" + error.type);
//                    }


                    //Log.e(TAG, "onPlayerError. render index=" + e.rendererIndex + ",error type=" + getErrTypeString(e.type));*/
                    //Toast.makeText(PlaybackActivity.this, "播放异常。。。。"+e.getMessage(), Toast.LENGTH_LONG);
                }

                @Override
                @SuppressWarnings("ReferenceEquality")
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    Log.e(TAG, "onTracksChanged. TrackGroupArray size=" + trackGroups.length + ",TrackSelectionArray size=" + trackSelections.length);
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    Log.e(TAG, "onIsPlayingChanged. isPlaying："+isPlaying);
                    if(isPlaying){
                        cancelCheck();
                        checkerStart();
                    }else {
                        cancelCheck();
                    }
                }

                @Override
                public void onSeekProcessed() {
                    Log.e(TAG, "onSeekProcessed. ");
                }
            });
            //player.addAnalyticsListener(new ExoplayerAnalyticsListener());
            //mMediaPlayer.addAnalyticsListener(new EventLogger(trackSelector));
            this.setPlayer(mMediaPlayer);
            // Measures bandwidth during playback. Can be null if not required.
            // Produces DataSource instances through which media data is loaded.
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(_mContext,
                    Util.getUserAgent(_mContext, "ExoPlayerInfo"), null);
            if(null == extractorsFactory){
                extractorsFactory = new DefaultHlsExtractorFactory();
            }

            //media source
            DrmSessionManager<ExoMediaCrypto> drmSessionManager = DrmSessionManager.getDummyDrmSessionManager();
            HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManager(drmSessionManager)
                    .setAllowChunklessPreparation(true)
                    .setExtractorFactory(extractorsFactory)
                    .setLoadErrorHandlingPolicy(
                            new DefaultLoadErrorHandlingPolicy() {
                                @Override
                                public long getRetryDelayMsFor(int dataType, long loadDurationMs, IOException exception, int errorCount) {
                                    return super.getRetryDelayMsFor(dataType, loadDurationMs, exception, errorCount);
                                }
                            })
                    .createMediaSource(mUri);


            //play
            mMediaPlayer.setPlayWhenReady(true);
            mMediaPlayer.prepare(mediaSource);
            //mCurrentState = STATE_PLAYING;
        }
    }



    public int getDuration() {
        return this.isInPlaybackState() ? (int) this.mMediaPlayer.getDuration() : -1;
    }

    public int getCurrentPosition() {
        return this.isInPlaybackState() ? (int) this.mMediaPlayer.getCurrentPosition() : 0;
    }

    public void seekTo(int msec) {
        if (this.isInPlaybackState()) {
            this.mMediaPlayer.seekTo(msec);
        }

    }

    public boolean isVideoPlaying() {
        return isInPlaybackState() && STATE_PLAYING == mCurrentState;
    }


    public boolean isInPlaybackState() {
        return this.mMediaPlayer != null && this.mCurrentState != -1 && this.mCurrentState != 0 && this.mCurrentState != 1;
    }

    /**
     * 是否打开声音
     * @param isOpen
     */
    public void setSoundSwitch(boolean isOpen){
        if(null != mMediaPlayer){
            if(isOpen){
                mMediaPlayer.setVolume(1f);
            } else {
                mMediaPlayer.setVolume(0f);
            }
        }
    }


    /**
     * 抓拍
     * @return
     */
    public Bitmap snapVideoBitmap(){
        TextureView textureView = (TextureView) this.getVideoSurfaceView();
        if(null == textureView){
            return null;
        }
        return textureView.getBitmap();
    }




    private ICloudVideoPlayListener mOnMoviePlayListener;





    /**
     * 播放某段视频
     */
    public void startCloudVideo(String videoUri) {
        setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(videoUri)){
            this.mUri = Uri.parse(videoUri);
        }
        setVideoURI();
    }



    /**
     * 继续播放
     */
    public void resumeCloudVideo() {
        if (this.isInPlaybackState()) {
            mMediaPlayer.setPlayWhenReady(true);
            this.mCurrentState = STATE_PLAYING;
        }

    }

    /**
     * 暂停播放
     */
    public void pauseCloudVideo() {
        if (isInPlaybackState() && STATE_PLAYING == mCurrentState) {
            mMediaPlayer.setPlayWhenReady(false);
            mCurrentState = STATE_PAUSED;
        }

    }




   public void setCloudVideoPlayListener(ICloudVideoPlayListener onMoviePlayListener) {
        this.mOnMoviePlayListener = onMoviePlayListener;
    }

}
