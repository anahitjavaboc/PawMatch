package com.anahit.pawmatch;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class PawMatchApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, Object> config = new HashMap<>();
        config.put("dmmjc18z9", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("161134658645382", BuildConfig.CLOUDINARY_API_KEY);
        config.put("byMd4Ixvx794mNH06dZsNTkfZco", BuildConfig.CLOUDINARY_API_SECRET);
        MediaManager.init(this, config);
    }
}

