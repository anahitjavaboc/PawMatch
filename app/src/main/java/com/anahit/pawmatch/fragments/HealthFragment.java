package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.HealthAdapter;
import com.anahit.pawmatch.models.Pet;
import com.anahit.pawmatch.dialogs.VaccinationDialogFragment;
import com.anahit.pawmatch.dialogs.MedicalHistoryDialogFragment;
import com.anahit.pawmatch.dialogs.MedicationDialogFragment;
import com.anahit.pawmatch.dialogs.VetAppointmentDialogFragment;
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
        adapter = new HealthAdapter(pets, this); // Pass fragment for button callbacks
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
        petsRef.orderByChild("ownerId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
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

    public void refreshData() {
        if (currentUserId == null) {
            Log.e("HealthFragment", "Cannot refresh data: Current user ID is null");
            return;
        }
        Log.d("HealthFragment", "Refreshing pet data");
        loadPets(); // Re-fetch pet data
    }

    public void onViewVaccinationsClick(Pet pet) {
        VaccinationDialogFragment dialog = VaccinationDialogFragment.newInstance(pet);
        dialog.show(getChildFragmentManager(), "VaccinationDialog");
    }

    public void onViewMedicalHistoryClick(Pet pet) {
        MedicalHistoryDialogFragment dialog = MedicalHistoryDialogFragment.newInstance(pet);
        dialog.show(getChildFragmentManager(), "MedicalHistoryDialog");
    }

    public void onViewMedicationsClick(Pet pet) {
        MedicationDialogFragment dialog = MedicationDialogFragment.newInstance(pet);
        dialog.show(getChildFragmentManager(), "MedicationDialog");
    }

    public void onViewVetAppointmentsClick(Pet pet) {
        VetAppointmentDialogFragment dialog = VetAppointmentDialogFragment.newInstance(pet);
        dialog.show(getChildFragmentManager(), "VetAppointmentDialog");
    }
}