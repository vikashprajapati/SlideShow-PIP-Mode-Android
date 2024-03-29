package com.example.pictureslideshow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SlideShowActivity extends AppCompatActivity implements View.OnClickListener {

    private final String IMAGE_SOURCE = "https://source.unsplash.com/random";
    private final int mInterval = 5000; // 5000ms or 5sec
    private Handler mHandler;

    private ImageView mSlideShow;
    private ImageButton mSlideShowPlayback;
    private boolean isPlaying = true;
    private ImageButton mSlideShowStop;
    private List<RemoteAction> actions = new ArrayList<>();

    FutureTarget<Drawable> futureTarget;
    DrawableCrossFadeFactory crossFadeFactory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    Runnable mLoadImage = new Runnable() {
        @Override
        public void run() {
            try {
                Glide.with(SlideShowActivity.this)
                        .load(futureTarget.get())
                        .transition(withCrossFade(crossFadeFactory))
                        .placeholder(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(mSlideShow);

                // prefetching the image for lazyloading
                futureTarget = Glide.with(SlideShowActivity.this)
                        .asDrawable()
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .centerCrop()
                        .load(IMAGE_SOURCE).submit();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                mHandler.postDelayed(mLoadImage, mInterval);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_slide_show);
        mHandler = new Handler(this.getMainLooper());

        futureTarget = Glide.with(SlideShowActivity.this)
                .asDrawable()
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).load(IMAGE_SOURCE).submit();


        RemoteAction pauseAction = new RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_pause),
                getString(R.string.btn_playback),
                getString(R.string.btn_playback),
                PendingIntent.getBroadcast(this, 0, new Intent("PAUSE"), PendingIntent.FLAG_UPDATE_CURRENT)
        );
        actions.add(pauseAction);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSlideShow = findViewById(R.id.iv_slideshow_image);

        mSlideShowPlayback = findViewById(R.id.ib_playback);
        mSlideShowPlayback.setOnClickListener(this);

        mSlideShowStop = findViewById(R.id.ib_stop);
        mSlideShowStop.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        App app = (App)getApplicationContext();
        if(app.getActivityReferences() == 1) {
            mLoadImage.run();
            mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App app = (App)getApplicationContext();
        if(app.getActivityReferences() == 1){
            PictureInPictureParams pictureInPictureParams = new PictureInPictureParams
                    .Builder()
                    .setActions(actions)
                    .build();
            enterPictureInPictureMode(pictureInPictureParams);
        }else{
            mHandler.removeCallbacks(mLoadImage);
        }
    }

    private void handlePlayback(){
        if(isPlaying){
            mHandler.removeCallbacks(mLoadImage);
            mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, getTheme()));
        }
        else{
            mLoadImage.run();
            mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
        }
        isPlaying = !isPlaying;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_playback:
                handlePlayback();
                break;
            case R.id.ib_stop:
                Glide.with(SlideShowActivity.this)
                        .clear(mSlideShow);
                mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                isPlaying = false;
                mHandler.removeCallbacks(mLoadImage);
                Toast.makeText(SlideShowActivity.this, R.string.slide_show_stop_message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if(isInPictureInPictureMode){
            updateVisibility(View.GONE);
        }
        else{
            updateVisibility(View.VISIBLE);
        }
    }

    private void updateVisibility(int val){
        mSlideShowPlayback.setVisibility(val);
        mSlideShowStop.setVisibility(val);
    }
}