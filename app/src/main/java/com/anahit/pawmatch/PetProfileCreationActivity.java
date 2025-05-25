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
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.anahit.pawmatch.models.Pet;
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

public class PetProfileCreationActivity extends AppCompatActivity {
    private static final String TAG = "PetProfileCreation";
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

        databaseReference = FirebaseDatabase.getInstance().getReference();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Redirecting to login.");
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        ownerId = currentUser.getUid();
        Log.d(TAG, "Authenticated user UID: " + ownerId);

        String intentOwnerId = getIntent().getStringExtra("ownerId");
        if (intentOwnerId != null && !intentOwnerId.equals(ownerId)) {
            Log.w(TAG, "Owner ID from intent (" + intentOwnerId + ") does not match authenticated user (" + ownerId + ")");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
        }

        petImageView = findViewById(R.id.petImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        petNameEditText = findViewById(R.id.petNameEditText);
        petAgeEditText = findViewById(R.id.petAgeEditText);
        petBreedEditText = findViewById(R.id.petBreedEditText);
        petBioEditText = findViewById(R.id.petBioEditText);

        uploadImageButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Storage permission required to upload images", Toast.LENGTH_LONG).show();
            }
        });

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

        showLoading(true);
        executorService.execute(() -> {
            MediaManager.get().upload(filePath)
                    .unsigned("3-pawmatch")
                    .option("public_id", "pets/" + petId)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started for requestId: " + requestId);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            imageUrl = resultData.get("secure_url").toString();
                            Log.d(TAG, "Pet profile image uploaded successfully: " + imageUrl);
                            mainHandler.post(() -> {
                                showLoading(false);
                                savePetToFirebase(petId, petName, petAge, petBreed, petBio);
                            });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Upload failed: " + error.getDescription() + ", Request ID: " + requestId);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not authenticated during save. Redirecting to login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Pet pet = new Pet(petName, petAge, ownerId, imageUrl);
        pet.setId(petId);
        pet.setBreed(petBreed);
        pet.setBio(petBio);
        pet.setHealthStatus("Unknown");

        // Write to pets/<petId>
        Map<String, Object> updates = new HashMap<>();
        updates.put("pets/" + petId, pet);

        Log.d(TAG, "Writing to database at path: pets/" + petId + ", data: " + pet.toString());

        executorService.execute(() -> {
            databaseReference.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> mainHandler.post(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Pet profile saved!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PetProfileCreationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }))
                    .addOnFailureListener(e -> mainHandler.post(() -> {
                        Log.e(TAG, "Failed to save pet profile: " + e.getMessage(), e);
                        showLoading(false);
                        Toast.makeText(this, "Failed to save pet profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });
    }

    private void showLoading(boolean isLoading) {
        ProgressBar loadingIndicator = findViewById(R.id.loadingIndicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
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