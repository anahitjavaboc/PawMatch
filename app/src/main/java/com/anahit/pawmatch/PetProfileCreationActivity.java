package com.anahit.pawmatch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.anahit.pawmatch.models.Pet;
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

public class PetProfileCreationActivity extends AppCompatActivity {
    private static final String TAG = "PetProfileCreation";
    private static boolean isMediaManagerInitialized = false; // Flag to prevent reinitialization
    private ImageView petImageView;
    private Button uploadImageButton, saveProfileButton;
    private EditText petNameEditText, petAgeEditText, petBreedEditText, petBioEditText;
    private Uri filePath;
    private String imageUrl;
    private DatabaseReference databaseReference;
    private String ownerId;
    private ExecutorService executorService;
    private Handler mainHandler;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    filePath = uri;
                    Glide.with(this).load(uri).into(petImageView);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation_pet);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize MediaManager if not already initialized
        if (!isMediaManagerInitialized) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
            MediaManager.init(this, config);
            isMediaManagerInitialized = true;
            Log.d(TAG, "MediaManager initialized");
        }

        // Get ownerId from Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ownerId = auth.getCurrentUser().getUid();

        // Check ownerId from intent (for consistency)
        String intentOwnerId = getIntent().getStringExtra("ownerId");
        if (intentOwnerId != null && !intentOwnerId.equals(ownerId)) {
            Log.w(TAG, "Owner ID from intent (" + intentOwnerId + ") does not match authenticated user (" + ownerId + ")");
        }

        // Request storage permission for Android 13+
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
        }

        // Initialize views
        petImageView = findViewById(R.id.petImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        petNameEditText = findViewById(R.id.petNameEditText);
        petAgeEditText = findViewById(R.id.petAgeEditText);
        petBreedEditText = findViewById(R.id.petBreedEditText);
        petBioEditText = findViewById(R.id.petBioEditText);

        // Handle image upload
        uploadImageButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Storage permission required to upload images", Toast.LENGTH_LONG).show();
            }
        });

        // Handle save and continue
        saveProfileButton.setOnClickListener(v -> savePetProfile());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted");
            } else {
                Toast.makeText(this, "Storage permission required to upload images", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void savePetProfile() {
        String petName = petNameEditText.getText().toString().trim();
        String petAgeStr = petAgeEditText.getText().toString().trim();
        String petBreed = petBreedEditText.getText().toString().trim();
        String petBio = petBioEditText.getText().toString().trim();

        // Validate inputs
        if (petName.isEmpty() || petAgeStr.isEmpty() || petBreed.isEmpty() || petBio.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Validation failed: Empty fields");
            return;
        }

        if (filePath == null) {
            Toast.makeText(this, "Please upload a pet photo", Toast.LENGTH_SHORT).show();
            return;
        }

        int petAge;
        try {
            petAge = Integer.parseInt(petAgeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Invalid age format: " + e.getMessage());
            return;
        }

        String petId = databaseReference.child("pets").push().getKey();
        if (petId == null) {
            Toast.makeText(this, "Error: Unable to generate pet ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image to Cloudinary
        showLoading(true);
        executorService.execute(() -> {
            MediaManager.get().upload(filePath)
                    .unsigned("3-pawmatch")
                    .option("public_id", "pets/" + petId)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started for requestId: " + requestId);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> result = (Map<String, Object>) resultData;
                            imageUrl = result.get("url").toString();
                            Log.d(TAG, "Pet profile image uploaded successfully: " + imageUrl);
                            mainHandler.post(() -> {
                                showLoading(false);
                                savePetToFirebase(petId, petName, petAge, petBreed, petBio);
                            });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            mainHandler.post(() -> {
                                showLoading(false);
                                Toast.makeText(PetProfileCreationActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            float progress = (bytes / (float) totalBytes) * 100;
                            Log.d(TAG, "Upload progress: " + progress + "%");
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();
        });
    }

    private void savePetToFirebase(String petId, String petName, int petAge, String petBreed, String petBio) {
        showLoading(true);
        Pet pet = new Pet(petName, petAge, ownerId, imageUrl);
        pet.setId(petId);
        pet.setBreed(petBreed);
        pet.setBio(petBio);
        pet.setHealthStatus("Unknown");

        executorService.execute(() -> {
            databaseReference.child("users").child(ownerId).child("pets").child(petId).setValue(petId)
                    .addOnSuccessListener(aVoid -> {
                        databaseReference.child("pets").child(petId).setValue(pet)
                                .addOnSuccessListener(aVoid2 -> mainHandler.post(() -> {
                                    showLoading(false);
                                    Toast.makeText(this, "Pet profile saved!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(PetProfileCreationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }))
                                .addOnFailureListener(e -> mainHandler.post(() -> {
                                    showLoading(false);
                                    Toast.makeText(this, "Failed to save pet profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }));
                    })
                    .addOnFailureListener(e -> mainHandler.post(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to save pet profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });
    }

    private void showLoading(boolean isLoading) {
        findViewById(R.id.loadingIndicator).setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveProfileButton.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}