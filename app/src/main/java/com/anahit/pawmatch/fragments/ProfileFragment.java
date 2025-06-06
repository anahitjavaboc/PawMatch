package com.anahit.pawmatch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anahit.pawmatch.LoginActivity;
import com.anahit.pawmatch.OwnerProfileEditActivity;
import com.anahit.pawmatch.PetProfileEditActivity;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import com.anahit.pawmatch.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView ownerNameTextView, ownerInfoTextView, petNameTextView, petInfoTextView;
    private ImageView ownerImageView, petImageView, editOwnerProfileButton, editPetProfileButton;
    private Button logoutButton;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("pets");
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ownerImageView = view.findViewById(R.id.profile_owner_image);
        ownerNameTextView = view.findViewById(R.id.profile_owner_name);
        ownerInfoTextView = view.findViewById(R.id.profile_owner_info);
        petImageView = view.findViewById(R.id.profile_pet_image);
        petNameTextView = view.findViewById(R.id.profile_pet_name);
        petInfoTextView = view.findViewById(R.id.profile_pet_info);
        editOwnerProfileButton = view.findViewById(R.id.edit_owner_profile);
        editPetProfileButton = view.findViewById(R.id.edit_pet_profile);
        logoutButton = view.findViewById(R.id.logout_button);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Log.e("ProfileFragment", "Current user ID is null");
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Load initial data
        loadOwnerProfile();
        loadPetProfile();

        editOwnerProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OwnerProfileEditActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });

        editPetProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PetProfileEditActivity.class);
            intent.putExtra("OWNER_ID", currentUserId);
            startActivity(intent);
        });

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        } else {
            Log.e("ProfileFragment", "Logout button not found in layout");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when the fragment becomes visible
        refreshProfile();
    }

    public void refreshProfile() {
        if (currentUserId != null) {
            loadOwnerProfile();
            loadPetProfile();
        }
    }

    private void loadOwnerProfile() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        ownerNameTextView.setText(user.getName() != null ? user.getName() : "Unknown Owner");
                        String ownerInfo = "Age: " + (user.getAge() != null ? user.getAge().toString() : "Unknown");
                        ownerInfoTextView.setText(ownerInfo);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(ownerImageView);
                        } else {
                            ownerImageView.setImageResource(R.drawable.ic_profile);
                        }
                    } else {
                        Log.e("ProfileFragment", "User data is null");
                    }
                } else {
                    Log.e("ProfileFragment", "User snapshot does not exist for userId: " + currentUserId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProfileFragment", "Error fetching user data: " + error.getMessage());
                Toast.makeText(requireContext(), "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPetProfile() {
        petsRef.orderByChild("ownerId").equalTo(currentUserId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                    Pet pet = petSnapshot.getValue(Pet.class);
                    if (pet != null) {
                        petNameTextView.setText(pet.getName() != null ? pet.getName() : "Unknown Pet");
                        String petInfo = (pet.getAge() > 0 ? pet.getAge() : "Unknown") + ", " +
                                (pet.getBreed() != null ? pet.getBreed() : "Unknown");
                        petInfoTextView.setText(petInfo);
                        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                            Glide.with(requireContext())
                                    .load(pet.getImageUrl())
                                    .placeholder(R.drawable.ic_pet_placeholder)
                                    .error(R.drawable.ic_pet_placeholder)
                                    .into(petImageView);
                        } else {
                            petImageView.setImageResource(R.drawable.ic_pet_placeholder);
                        }
                        break; // Display only the first pet
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProfileFragment", "Error fetching pet data: " + error.getMessage());
                Toast.makeText(requireContext(), "Error fetching pet data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}