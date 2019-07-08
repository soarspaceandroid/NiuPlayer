package com.example.administrator.niuapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.PlayerState;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * ----------------------------------------------------
 * ※ Author :  GaoFei
 * ※ Date : 2019/3/6 0006
 * ※ Time : 下午 5:12
 * ※ Project : feimuAndroid
 * ※ Package : com.qiniu.player
 * ----------------------------------------------------
 */
public class FMVideoPlayer extends FrameLayout {

    private final static String TAG = "FMVideoPlayer";

    private final static int INIT_PRE = 2002;
    private final static int DELAY = 100;

    private TextureView textureView;
    private PLMediaPlayer plMediaPlayer;
    private Surface surfaceT;
    private ImageView coverView;
    private TextView buttomCurrentTime,totalTime;
    private ImageView playBt;
    private ProgressBar progressBar;
    private View buttomBar,layFullBar;
    private String videoPath = "";
    private SeekBar seekBar;
    private OnClickListener onClickListener;
    private ImageView pauseButtom;
    private WeakReference<FMVideoPlayer> instance = new WeakReference<>(this);
    private View layTitle;
    //recycleview 刷新时 header 暂时detach的标志位
    private long startPosition = -1;


    private ExecutorService service = Executors.newSingleThreadExecutor();
    private Runnable resetTask = new Runnable() {
        @Override
        public void run() {
            if(getContext() instanceof Activity){
                if(!((Activity) getContext()).isFinishing()){
                    if(plMediaPlayer != null) {
                        plMediaPlayer.setSurface(null);
                        plMediaPlayer.release();
                        plMediaPlayer = null;
                    }
                }
            }
        }
    };



    private Runnable setPlayerTask = new Runnable() {
        @Override
        public void run() {
            try {
                plMediaPlayer = new PLMediaPlayer(getContext(), PlayerOption.create().build());
                plMediaPlayer.setLooping(true);
                plMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
                plMediaPlayer.setOnInfoListener(mOnInfoListener);
                plMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
                plMediaPlayer.setOnPreparedListener(onPreparedListener);
                plMediaPlayer.setWakeMode(getContext(), PowerManager.PARTIAL_WAKE_LOCK);
                enableBuffer(true);
                plMediaPlayer.setDataSource(videoPath);
                plMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            post(startTask);
        }
    };


    private Runnable startTask = new Runnable() {
        @Override
        public void run() {
            if(plMediaPlayer != null) {
                plMediaPlayer.setSurface(surfaceT);
                plMediaPlayer.start();
                if(startPosition != -1) {
                    if(plMediaPlayer != null) {
                        plMediaPlayer.seekTo(startPosition);
                    }
                }
            }
            setPlayButtonVisable(false);
            buttomCurrentTime.setText("00:00");
            totalTime.setText(StringUtils.generateTime(mDuration*1000));
            if(tvCommentTotal!=null){
                tvCommentTotal.setText(StringUtils.generateTime(mDuration*1000));
            }
            if(tvCommentCurrent!=null){
                tvCommentCurrent.setText("00:00");
            }
        }
    };



    private void initView(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.fm_video_player , this , true);
        textureView = view.findViewById(R.id.pl_video);
        coverView = view.findViewById(R.id.cover_view);
        playBt = view.findViewById(R.id.play_image);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.fm_seek));
        layTitle=view.findViewById(R.id.lay_title);
        buttomBar=view.findViewById(R.id.lay_buttom);
        seekBar=view.findViewById(R.id.seek_bar);
        buttomCurrentTime=view.findViewById(R.id.time_current);
        totalTime=view.findViewById(R.id.time);
        pauseButtom =view.findViewById(R.id.pause);
        layFullBar=view.findViewById(R.id.lay_full_bar);
        setBackgroundColor(Color.BLACK);
        setOther();
    }
    boolean userTouch=false;
    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
//                    Toast.makeText(getContext() , extra + "ms" , Toast.LENGTH_SHORT).show();
                    setCoverVisabel(false);
                    setPlayButtonVisable(false);
                    pauseButtom.setSelected(false);
                    if(commentPause!=null){
                        commentPause.setSelected(false);
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    setProgress();
                    pauseButtom.setSelected(false);
                    if (commentPause != null) {
                        commentPause.setSelected(false);
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_STATE_CHANGED_PAUSED:
//                  pauseButtom.setImageDrawable(getContext().getResources().getDrawable(R.mipmap.pic_start));
                    setPlayButtonVisable(true);
                    pauseButtom.setSelected(true);
                    if(commentPause!=null){
                        commentPause.setSelected(true);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    int width,height;
    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(final int width, final int height) {
            if (width != 0 && height != 0) {
                FMVideoPlayer.this.width=width;
                FMVideoPlayer.this.height=height;
                textureView.post(resetRunnable);
            }
        }
    };



    /**
     * 横屏画布全屏
     */
    Runnable resizeRunnable=new Runnable() {
        @Override
        public void run() {
            float ratioW = (float) width / (float) getHeight();
            float ratioH = (float) height / (float) getWidth();
            float ratio = Math.min(ratioW, ratioH);
            int newWidth = (int) Math.ceil((float) width / ratio);
            int newHeight = (int) Math.ceil((float) height / ratio);
            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) textureView.getLayoutParams();
            layout.width = newWidth;
            layout.height = newHeight;
            textureView.setLayoutParams(layout);
        }
    };

    /**
     * 竖屏屏画布全屏
     */
    Runnable resetRunnable=new Runnable() {
        @Override
        public void run() {
            float ratioW = (float) width / (float) getWidth();
            float ratioH = (float) height / (float) getHeight();
            float ratio = Math.min(ratioW, ratioH);
            int newWidth = (int) Math.ceil((float) width / ratio);
            int newHeight = (int) Math.ceil((float) height / ratio);
            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) textureView.getLayoutParams();
            layout.width = newWidth;
            layout.height = newHeight;
            textureView.setLayoutParams(layout);
        }
    };

    private PLOnPreparedListener onPreparedListener = new PLOnPreparedListener() {
        @Override
        public void onPrepared(int i) {
            //页面打开过快,onvisiblity监听提前执行,导致无法释放播放器
            if(plMediaPlayer != null && getWindowVisibility() == GONE){
                reset();
            }
        }
    };


    private PLOnBufferingUpdateListener onBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int i) {
            if(progressBar != null) {
                progressBar.setSecondaryProgress(i * 10);
            }
            if(seekBar != null){
                seekBar.setSecondaryProgress(i * 10);
            }
        }
    };

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setOutlineProvider(new TextureVideoViewOutlineProvider(DisplayUtils.dip2px(getContext(),4)));
        }
    }

    private void setProgress(){
        if(coverView.getVisibility() == VISIBLE){
            setCoverVisabel(false);
        }
        if(userTouch)return;
        if(plMediaPlayer != null && plMediaPlayer.isPlaying()) {
            long position = plMediaPlayer.getCurrentPosition();
            long duration = plMediaPlayer.getDuration();
            if (progressBar != null) {
                if (duration > 0) {
                    long pos = 1000L * position / duration;
                    progressBar.setProgress((int) pos);
                    seekBar.setProgress((int) pos);
                    if(commentSeekBar!=null){
                        commentSeekBar.setProgress((int) pos);
                    }
                }
            }
            if(totalTime!=null){
                totalTime.setText(StringUtils.generateTime(mDuration*1000));
            }
            if(tvCommentTotal!=null){
                tvCommentTotal.setText(StringUtils.generateTime(mDuration*1000));
            }
            if(tvCommentCurrent!=null){
                tvCommentCurrent.setText(StringUtils.generateTime( position));
            }
            if(buttomCurrentTime!=null){
                buttomCurrentTime.setText(StringUtils.generateTime( position));
            }
        }
    }

    /**
     * 显示，隐藏封面，剧集布局
     */
    private void setCoverVisabel(boolean visabel){
        if(visabel){
            coverView.setVisibility(VISIBLE);
        }else {
            animGone(coverView);
            layTitle.setVisibility(GONE);
        }
    }


    public FMVideoPlayer(Context context) {
        super(context);
        initView(context);
    }

    public FMVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FMVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private long mDuration;

    private void setPlayer(){
//        service.execute(setPlaerTask);
        setCoverVisabel(true);
        progressBar.setVisibility(VISIBLE);
        layFullBar.setVisibility(GONE);
        changePlayBtPostionAndSize(32, 8);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        this.onClickListener = l;
    }

    private void setOther(){
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceT = new Surface(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                surfaceT = null;
                if(plMediaPlayer != null){
                    plMediaPlayer.setSurface(null);
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        playBt.setOnClickListener(v -> {
            setPlayButtonVisable(false);
            start();
        });


        super.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onClick(this);
                pause();
                return;
            }
            if(plMediaPlayer != null && plMediaPlayer.isPlaying()){
                pause();
                setPlayButtonVisable(true);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(!fromUser)return;
//                final long newposition = (mDuration * seekBar.getProgress()) ;
//                    getHandler().removeCallbacks(mLastSeekBarRunnable);
//                    mLastSeekBarRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            plMediaPlayer.seekTo(newposition);
//                            if(getPlayState()==PlayerState.PAUSED){
//                                start();
//                            }
//                        }
//                    };
//                    postDelayed(mLastSeekBarRunnable,200);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final long newposition = (mDuration * seekBar.getProgress()) ;
                if(getPlayState()!=PlayerState.PLAYING){
                    start();
                }
                if(plMediaPlayer != null) {
                    plMediaPlayer.seekTo(newposition);
                }
                userTouch=false;
            }
        });
        pauseButtom.setOnClickListener(v->{
//            clickStartButtom();
            if(getPlayState() == PlayerState.PLAYING){
                pause();
                setPlayButtonVisable(true);
            }else {
                start();
            }
        });
    }





    public void setVolume(float value){
        if(plMediaPlayer != null){
            plMediaPlayer.setVolume(value , value);
        }
        if(value == 1f){
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }


    public FMVideoPlayer setVideoDurition(int length){
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        mDuration=length;
        return this;
    }

    public PlayerState getPlayState(){
        if(plMediaPlayer != null) {
            try {
                return plMediaPlayer.getPlayerState();
            }catch (Exception e){
                return PlayerState.IDLE;
            }
        }else{
            return PlayerState.IDLE; //默认按照播放处理
        }
    }

    public void release(){
        if(plMediaPlayer != null) {
            plMediaPlayer.setDisplay(null);
            plMediaPlayer.release();
            plMediaPlayer = null;
        }
        service.shutdownNow();
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);
    }


    public void reset(){
        setPlayButtonVisable(true);
        setCoverVisabel(true);
//        if(plMediaPlayer != null) {
//            plMediaPlayer.setSurface(null);
//            plMediaPlayer.release();
//        }
//        setPlayer();
        if(!service.isShutdown()){
            service.execute(resetTask);
        }
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(visibility == VISIBLE){
            setPlayer();
        }else{
            if(getPlayState() != PlayerState.IDLE) {
                reset();
            }
        }
    }

    public void pause(){
        if(getPlayState() == PlayerState.PLAYING || getPlayState() == PlayerState.PLAYING_CACHE ||  getPlayState() == PlayerState.BUFFERING) {
            plMediaPlayer.pause();
            enableBuffer(false);
            setPlayButtonVisable(true);
        }else if(getPlayState() == PlayerState.PREPARING || getPlayState() == PlayerState.ERROR){
            reset();
        }
    }




    /**
     * 设置封面图
     * @param path
     * @return
     */
    public FMVideoPlayer setCoverPath(String path){
        if(coverView.getVisibility() == GONE){
            setCoverVisabel(true);
        }
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        coverView.setBackgroundColor(getContext().getResources().getColor(R.color.color_EDEDED));
        Glide.with(this).load(path).apply(requestOptions).transition(new DrawableTransitionOptions().crossFade(500)).into(coverView);
        return this;
    }

    /**
     * 设置视频地址
     * @param path
     * @return
     */
    public FMVideoPlayer setVideoPath(String path){
//        if(!TextUtils.isEmpty(videoPath) && !videoPath.equals(path)){
//            reset();
//        }
        this.videoPath = path;
        return this;
    }

    /**videoPath
     * 设置从中间播放
     * @param second
     * @return
     */
    public FMVideoPlayer startFromPosition(long second){
        startPosition = second;
        start();
        return this;
    }

    /**
     * 是否需要圆角
     * @param isNeed
     * @return
     */
    public FMVideoPlayer isNeedRound(boolean isNeed){
        return this;
    }

    /**
     * get position
     * @return
     */
    public long getCurrentPosition(){
        return plMediaPlayer.getCurrentPosition();
    }

    public String getVideoPath() {
        return videoPath;
    }

    public ImageView getCoverView() {
        return coverView;
    }

    /**
     * 修改progressbar位置
     * @param hasBarMargin  底部progressbar距离底部是否需要间距
     * @return
     */
    public FMVideoPlayer changeProgressbarPosition(boolean hasBarMargin){
        if(hasBarMargin){
            buttomBar.setVisibility(VISIBLE);
        }else {
            buttomBar.setVisibility(GONE);
        }
        return this;
    }



    private void enableBuffer(boolean enable){
        //开启缓存不要关闭buffer
        try {
            if(plMediaPlayer != null){
                plMediaPlayer.setBufferingEnabled(enable);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void setDoubleClickListener(GestureDetector videoGesDetector){
        setOnTouchListener((v,event)->{
            videoGesDetector.onTouchEvent(event);
            return true;
        });
    }

    public void start(){
        if(getPlayState() == PlayerState.IDLE) {
            if(!service.isShutdown()) {
                service.execute(setPlayerTask);
            }
        }else if(getPlayState() == PlayerState.PAUSED){
            if(plMediaPlayer != null){
                enableBuffer(true);
                plMediaPlayer.start();
                setPlayButtonVisable(false);
            }
        }

    }





    private void setPlayButtonVisable(boolean visable){
        if(visable){
            playBt.setSelected(false);
        }else {
            playBt.setSelected(true);
        }

    }




    SeekBar commentSeekBar;
    public void setSeekBar(SeekBar seekBar){
        commentSeekBar=seekBar;
        if(seekBar==null)return;
        commentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final long newposition = (mDuration * seekBar.getProgress()) ;
                if(getPlayState()==PlayerState.PAUSED){
                    start();
                }
                if(plMediaPlayer != null) {
                    plMediaPlayer.seekTo(newposition);
                }
                userTouch=false;
            }
        });
    }
    TextView tvCommentCurrent,tvCommentTotal;
    ImageView commentPause;



    /**
     * 改变播放按钮位置
     * @param whDp
     */
    public void changePlayBtPostionAndSize(int whDp , int paddingDp){
        if(playBt == null){
            return;
        }
        setPlayButtonVisable(true);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)playBt.getLayoutParams();
        layoutParams.width = DisplayUtils.dip2px(getContext() ,whDp);
        layoutParams.height = DisplayUtils.dip2px(getContext() ,whDp);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT ;
        playBt.setLayoutParams(layoutParams);
        playBt.setPadding(DisplayUtils.dip2px(getContext() ,paddingDp),DisplayUtils.dip2px(getContext() ,paddingDp),DisplayUtils.dip2px(getContext() ,paddingDp),DisplayUtils.dip2px(getContext() ,paddingDp));
    }



    /**
     * seek to
     * @param time
     */
    public void seekTo(long time){
        if(plMediaPlayer != null){
            plMediaPlayer.seekTo(time);
        }
    }

    /**
     * 动画
     */
    private void animGone(View view){
        view.setVisibility(GONE);
//        if(view.getVisibility() != GONE) {
//            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
//            objectAnimator.setDuration(300);
//            objectAnimator.setInterpolator(new LinearInterpolator());
//            objectAnimator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    if (view != null) {
//                        view.setAlpha(1f);
//                        view.setVisibility(GONE);
//                    }
//                }
//
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//                    if (view != null) {
//                        view.setAlpha(1f);
//                        view.setVisibility(GONE);
//                    }
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//
//            objectAnimator.start();
//        }
    }


    /**
     * 获取视频时长
     * @return
     */
    public long getDuration(){
        if (plMediaPlayer != null) {
            return plMediaPlayer.getDuration();
        }
        return 0;
    }


}
