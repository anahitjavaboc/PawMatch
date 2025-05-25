package com.anahit.pawmatch;

import android.app.Application;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class PawMatchApplication extends Application {
    private static final String TAG = "PawMatchApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, Object> config = new HashMap<>();
        String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME;
        String apiKey = BuildConfig.CLOUDINARY_API_KEY;
        Log.d(TAG, "Initializing Cloudinary - Cloud Name: " + cloudName + ", API Key: " + apiKey);
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey); // Optional for unsigned uploads, included for completeness
        // Do NOT include api_secret here for security reasons
        try {
            MediaManager.init(this, config);
            Log.d(TAG, "Cloudinary initialized successfully with cloud name: " + cloudName);
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary initialization failed: " + e.getMessage(), e);
        }
    }
}