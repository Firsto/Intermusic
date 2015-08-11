package ru.firsto.intermusic;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by razor on 11.08.15.
 */
public class IntermusicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
