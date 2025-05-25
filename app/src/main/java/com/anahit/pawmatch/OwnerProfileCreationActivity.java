package com.anahit.pawmatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OwnerProfileCreationActivity extends AppCompatActivity {
    private static final String TAG = "OwnerProfileCreation";
    private ImageView ownerImageView;
    private EditText ownerNameEditText, ownerAgeEditText;
    private Spinner ownerGenderSpinner;
    private Button uploadOwnerImageButton, saveOwnerProfileButton;
    private Uri filePath;
    private String imageUrl;
    private DatabaseReference databaseReference;
    private ExecutorService executorService;
    private Handler mainHandler;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    filePath = uri;
                    Glide.with(this).load(uri).into(ownerImageView);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile_creation);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Redirecting to login.");
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        Log.d(TAG, "Authenticated user UID: " + currentUser.getUid());

        ownerImageView = findViewById(R.id.ownerImageView);
        uploadOwnerImageButton = findViewById(R.id.uploadOwnerImageButton);
        ownerNameEditText = findViewById(R.id.ownerNameEditText);
        ownerAgeEditText = findViewById(R.id.ownerAgeEditText);
        ownerGenderSpinner = findViewById(R.id.ownerGenderSpinner);
        saveOwnerProfileButton = findViewById(R.id.saveOwnerProfileButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ownerGenderSpinner.setAdapter(adapter);

        uploadOwnerImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        saveOwnerProfileButton.setOnClickListener(v -> saveOwnerProfile());
    }

    private void saveOwnerProfile() {
        String name = ownerNameEditText.getText().toString().trim();
        String ageStr = ownerAgeEditText.getText().toString().trim();
        String gender = ownerGenderSpinner.getSelectedItem().toString();

        if (name.isEmpty() || ageStr.isEmpty() || gender.equals("Select Gender")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not authenticated during save. Redirecting to login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String ownerId = user.getUid();
        Log.d(TAG, "Saving profile for ownerId: " + ownerId + ", name: " + name + ", age: " + age + ", gender: " + gender);

        if (filePath != null) {
            showLoading(true);
            Log.d(TAG, "Starting upload for ownerId: " + ownerId + ", filePath: " + filePath.toString());
            executorService.execute(() -> {
                MediaManager.get().upload(filePath)
                        .unsigned("3-pawmatch")
                        .option("public_id", "owners/" + ownerId)
                        .option("resource_type", "image")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "Upload started: " + requestId);
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                Log.d(TAG, "Upload successful: " + resultData.toString());
                                imageUrl = resultData.get("secure_url").toString();
                                mainHandler.post(() -> {
                                    showLoading(false);
                                    saveOwnerToFirebase(ownerId, name, age, gender);
                                });
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.e(TAG, "Upload failed: " + error.getDescription() + ", Request ID: " + requestId + ", Code: " + error.getCode());
                                mainHandler.post(() -> {
                                    showLoading(false);
                                    Toast.makeText(OwnerProfileCreationActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                Log.d(TAG, "Upload progress: " + bytes + "/" + totalBytes);
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                            }
                        })
                        .dispatch();
            });
        } else {
            saveOwnerToFirebase(ownerId, name, age, gender);
        }
    }

    private void saveOwnerToFirebase(String ownerId, String name, int age, String gender) {
        showLoading(true);
        Map<String, Object> ownerData = new HashMap<>();
        ownerData.put("name", name);
        ownerData.put("age", age);
        ownerData.put("gender", gender);
        if (imageUrl != null) {
            ownerData.put("imageUrl", imageUrl);
        }
        Log.d(TAG, "Writing to database at path: users/" + ownerId + ", data: " + ownerData.toString());

        executorService.execute(() -> {
            databaseReference.child("users").child(ownerId).setValue(ownerData)
                    .addOnSuccessListener(aVoid -> mainHandler.post(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OwnerProfileCreationActivity.this, PetProfileCreationActivity.class);
                        intent.putExtra("ownerId", ownerId);
                        startActivity(intent);
                        finish();
                    }))
                    .addOnFailureListener(e -> mainHandler.post(() -> {
                        Log.e(TAG, "Failed to save profile: " + e.getMessage(), e);
                        showLoading(false);
                        Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });
    }

    private void showLoading(boolean isLoading) {
        ProgressBar loadingIndicator = findViewById(R.id.loadingIndicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        saveOwnerProfileButton.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}