package com.anahit.pawmatch;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class PawMatch extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

        try {
            MediaManager.init(this, config);
            android.util.Log.d("PawMatch", "Cloudinary initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("PawMatch", "Cloudinary initialization failed: " + e.getMessage());
        }
    }
}
