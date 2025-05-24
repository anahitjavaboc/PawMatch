package com.anahit.pawmatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.anahit.pawmatch.BuildConfig; // Corrected import
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
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

        // Check if user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        ownerImageView = findViewById(R.id.ownerImageView);
        uploadOwnerImageButton = findViewById(R.id.uploadOwnerImageButton);
        ownerNameEditText = findViewById(R.id.ownerNameEditText);
        ownerAgeEditText = findViewById(R.id.ownerAgeEditText);
        ownerGenderSpinner = findViewById(R.id.ownerGenderSpinner);
        saveOwnerProfileButton = findViewById(R.id.saveOwnerProfileButton);

        // Setup gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ownerGenderSpinner.setAdapter(adapter);

        // Handle image upload
        uploadOwnerImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Handle save and continue
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

        String ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (filePath != null) {
            showLoading(true);
            executorService.execute(() -> {
                MediaManager.get().upload(filePath)
                        .unsigned("3-pawmatch")
                        .option("public_id", "owners/" + ownerId)
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {}

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                imageUrl = resultData.get("url").toString();
                                mainHandler.post(() -> {
                                    showLoading(false);
                                    saveOwnerToFirebase(ownerId, name, age, gender);
                                });
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                mainHandler.post(() -> {
                                    showLoading(false);
                                    Toast.makeText(OwnerProfileCreationActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {}
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
                        showLoading(false);
                        Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });
    }

    private void showLoading(boolean isLoading) {
        findViewById(R.id.loadingIndicator).setVisibility(isLoading ? View.VISIBLE : View.GONE);
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