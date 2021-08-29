package com.example.pictureslideshow;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

public class SlideShowActivity extends AppCompatActivity {

    private final String IMAGE_SOURCE = "https://source.unsplash.com/random";
    private final int mInterval = 5000; // 5000ms or 5sec
    private Handler mHandler;

    private ImageView mSlideShow;

    Runnable mLoadImage = new Runnable() {
        @Override
        public void run() {
            try {
                Glide.with(SlideShowActivity.this)
                        .load(IMAGE_SOURCE)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .placeholder(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(mSlideShow);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSlideShow = findViewById(R.id.iv_slideshow_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoadImage.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mLoadImage);
    }

}