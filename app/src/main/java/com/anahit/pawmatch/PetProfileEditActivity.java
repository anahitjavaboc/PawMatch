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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PetProfileEditActivity extends AppCompatActivity {
    private static final String TAG = "PetProfileEditActivity";
    private ImageView petImageView;
    private Button uploadImageButton, saveProfileButton;
    private EditText petNameEditText, petAgeEditText, petBreedEditText, petBioEditText;
    private Spinner animalTypeSpinner;
    private Uri filePath;
    private String imageUrl;
    private DatabaseReference databaseReference;
    private String ownerId;
    private String petId;
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
        animalTypeSpinner = findViewById(R.id.animalTypeSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.animal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animalTypeSpinner.setAdapter(adapter);

        // Load existing pet data
        databaseReference.child("pets").orderByChild("ownerId").equalTo(ownerId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                            petId = petSnapshot.getKey();
                            Pet pet = petSnapshot.getValue(Pet.class);
                            if (pet != null) {
                                petNameEditText.setText(pet.getName() != null ? pet.getName() : "");
                                petAgeEditText.setText(String.valueOf(pet.getAge()));
                                petBreedEditText.setText(pet.getBreed() != null ? pet.getBreed() : "");
                                petBioEditText.setText(pet.getBio() != null ? pet.getBio() : "");
                                String animalType = pet.getSpecies() != null ? pet.getSpecies() : "Select Type";
                                int spinnerPosition = adapter.getPosition(animalType);
                                animalTypeSpinner.setSelection(spinnerPosition);
                                if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                                    Glide.with(PetProfileEditActivity.this).load(pet.getImageUrl()).into(petImageView);
                                    imageUrl = pet.getImageUrl(); // Preserve existing image URL
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error loading pet data: " + error.getMessage());
                    }
                });

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
        String animalType = animalTypeSpinner.getSelectedItem().toString();

        if (petName.isEmpty() || petAgeStr.isEmpty() || petBreed.isEmpty() || petBio.isEmpty() || animalType.equals("Select Type")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Validation failed: Empty fields");
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not authenticated during save. Redirecting to login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        Log.d(TAG, "Saving profile for ownerId: " + ownerId + ", name: " + petName + ", age: " + petAge + ", breed: " + petBreed);

        if (filePath != null) {
            showLoading(true);
            Log.d(TAG, "Starting upload for petId: " + petId + ", filePath: " + filePath.toString());
            executorService.execute(() -> {
                MediaManager.get().upload(filePath)
                        .unsigned("3-pawmatch")
                        .option("public_id", "pets/" + petId)
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
                                    savePetToFirebase(petId, petName, petAge, petBreed, petBio, animalType);
                                });
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.e(TAG, "Upload failed: " + error.getDescription() + ", Request ID: " + requestId + ", Code: " + error.getCode());
                                mainHandler.post(() -> {
                                    showLoading(false);
                                    Toast.makeText(PetProfileEditActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
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
            savePetToFirebase(petId, petName, petAge, petBreed, petBio, animalType);
        }
    }

    private void savePetToFirebase(String petId, String petName, int petAge, String petBreed, String petBio, String animalType) {
        showLoading(true);
        Map<String, Object> updates = new HashMap<>();
        Pet pet = new Pet(petName, petAge, ownerId, imageUrl);
        pet.setId(petId);
        pet.setBreed(petBreed);
        pet.setBio(petBio);
        pet.setHealthStatus("Unknown");
        pet.setSpecies(animalType);

        updates.put("pets/" + petId, pet);

        Log.d(TAG, "Writing to database at path: pets/" + petId + ", data: " + pet.toString());

        executorService.execute(() -> {
            databaseReference.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> mainHandler.post(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Pet profile saved!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PetProfileEditActivity.this, MainActivity.class);
                        intent.putExtra("fragment_to_load", "profile");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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