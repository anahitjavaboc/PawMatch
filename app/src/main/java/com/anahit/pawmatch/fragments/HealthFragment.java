package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Add this import
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.HealthAdapter;
import com.anahit.pawmatch.models.Pet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HealthFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Pet> pets = new ArrayList<>();
    private HealthAdapter adapter;
    private DatabaseReference petsRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        recyclerView = view.findViewById(R.id.healthRecyclerView);
        if (recyclerView == null) {
            Log.e("HealthFragment", "RecyclerView not found in layout");
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HealthAdapter(pets);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        if (currentUserId == null) {
            Log.e("HealthFragment", "Current user ID is null, cannot load pets");
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadPets();
        return view;
    }

    private void loadPets() {
        petsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pets.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pet pet = data.getValue(Pet.class);
                    if (pet != null && currentUserId.equals(pet.getOwnerId())) {
                        pets.add(pet);
                    }
                }
                Log.d("HealthFragment", "Loaded pets count: " + pets.size());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("HealthFragment", "Adapter is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("HealthFragment", "Error fetching pets: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to load pet data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}