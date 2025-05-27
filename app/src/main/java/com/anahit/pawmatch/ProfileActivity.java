package com.anahit.pawmatch;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView petImageView = findViewById(R.id.petImageView);
        TextView petNameTextView = findViewById(R.id.petNameTextView);
        TextView ownerNameTextView = findViewById(R.id.ownerNameTextView);
        TextView ownerNameDetailTextView = findViewById(R.id.ownerNameDetailTextView);
        TextView ownerAgeTextView = findViewById(R.id.ownerAgeTextView);
        TextView ownerIdTextView = findViewById(R.id.ownerIdTextView);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set navigation click listener
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get intent extras
        String userId = getIntent().getStringExtra("userId");
        String petName = getIntent().getStringExtra("petName");
        String petImageUrl = getIntent().getStringExtra("petImageUrl");
        String ownerName = getIntent().getStringExtra("otherUserName");

        // Populate views with intent data
        petNameTextView.setText(petName != null ? petName : "Unknown Pet");
        ownerNameTextView.setText(ownerName != null ? ownerName : "Unknown Owner");
        ownerNameDetailTextView.setText("Name: " + (ownerName != null ? ownerName : "Unknown"));
        ownerIdTextView.setText("User ID: " + (userId != null ? userId : "Unknown"));

        // Load pet image
        if (petImageUrl != null && !petImageUrl.isEmpty()) {
            Glide.with(this).load(petImageUrl).placeholder(R.drawable.pawmatchlogo).into(petImageView);
        } else {
            petImageView.setImageResource(R.drawable.pawmatchlogo);
        }

        // Fetch owner age from Firebase
        fetchOwnerAge(userId, ownerAgeTextView);
    }

    private void fetchOwnerAge(String userId, TextView ownerAgeTextView) {
        if (userId != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String age = dataSnapshot.child("age").getValue(String.class);
                    ownerAgeTextView.setText("Age: " + (age != null ? age : "N/A"));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ownerAgeTextView.setText("Age: N/A");
                }
            });
        } else {
            ownerAgeTextView.setText("Age: N/A");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}