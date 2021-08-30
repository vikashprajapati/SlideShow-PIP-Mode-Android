package com.example.pictureslideshow;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private final String TAG = App.class.getCanonicalName();
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    /*
    * Overrides the onCreate method of the Application class
    * */
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if(++activityReferences == 1 && !isActivityChangingConfigurations){
            // App entered in foreground
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if(--activityReferences == 0 && !isActivityChangingConfigurations){
            // App moved to background
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public int getActivityReferences(){
        return activityReferences;
    }
}
