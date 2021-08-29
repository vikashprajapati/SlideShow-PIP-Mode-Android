package com.example.pictureslideshow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

public class SlideShowActivity extends AppCompatActivity implements View.OnClickListener {

    private final String IMAGE_SOURCE = "https://source.unsplash.com/random";
    private final int mInterval = 5000; // 5000ms or 5sec
    private Handler mHandler;

    private ImageView mSlideShow;
    private ImageButton mSlideShowPlayback;
    private boolean isPlaying = true;
    private ImageButton mSlideShowStop;

    FutureTarget<Drawable> futureTarget;

    Runnable mLoadImage = new Runnable() {
        @Override
        public void run() {
            try {
                Glide.with(SlideShowActivity.this)
                        .load(futureTarget.get())
                        .placeholder(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(mSlideShow);

                // prefetching the image for lazyloading
                futureTarget = Glide.with(SlideShowActivity.this)
                        .asDrawable()
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
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
        setContentView(R.layout.activity_slide_show);
        mHandler = new Handler(this.getMainLooper());

        futureTarget = Glide.with(SlideShowActivity.this)
                .asDrawable()
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).load(IMAGE_SOURCE).submit();
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
        mLoadImage.run();
        mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mLoadImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_playback:
                if(isPlaying){
                    mHandler.removeCallbacks(mLoadImage);
                    mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                }
                else{
                    mLoadImage.run();
                    mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
                }
                isPlaying = !isPlaying;
                break;
            case R.id.ib_stop:
                Glide.with(SlideShowActivity.this)
                        .clear(mSlideShow);
                mSlideShowPlayback.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                mHandler.removeCallbacks(mLoadImage);
                Toast.makeText(SlideShowActivity.this, R.string.slide_show_stop_message, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}