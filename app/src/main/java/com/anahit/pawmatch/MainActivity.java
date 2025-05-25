package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.anahit.pawmatch.fragments.FeedFragment;
import com.anahit.pawmatch.fragments.HealthFragment;
import com.anahit.pawmatch.fragments.MatchesFragment;
import com.anahit.pawmatch.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ImageButton feedButton, matchesButton, healthButton, profileButton;
    private DatabaseReference usersRef;
    private DatabaseReference petsRef;
    private String userId;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ExecutorService for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Follow system theme for day/night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        userId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        // Check if user profile exists
        checkUserProfile();
    }

    private void checkUserProfile() {
        executorService.execute(() -> {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // User profile doesn't exist, redirect to OwnerProfileCreationActivity
                        Intent intent = new Intent(MainActivity.this, OwnerProfileCreationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // User profile exists, check for pet profile
                        checkPetProfile();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("MainActivity", "Error checking user profile: " + error.getMessage());
                }
            });
        });
    }

    private void checkPetProfile() {
        executorService.execute(() -> {
            petsRef.orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Pet profile doesn't exist, redirect to PetProfileCreationActivity
                        Intent intent = new Intent(MainActivity.this, PetProfileCreationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Both profiles exist, proceed with UI setup
                        runOnUiThread(() -> setupUI());
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("MainActivity", "Error checking pet profile: " + error.getMessage());
                }
            });
        });
    }

    private void setupUI() {
        // Initialize views
        viewPager = findViewById(R.id.view_pager);
        feedButton = findViewById(R.id.feed_button);
        matchesButton = findViewById(R.id.matches_button);
        healthButton = findViewById(R.id.health_button);
        profileButton = findViewById(R.id.profile_button);

        // Setup ViewPager with FragmentStatePagerAdapter
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Add ViewPager listener to log fragment changes
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Log.d("MainActivity", "ViewPager switched to position: " + position);
                updateButtonState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Set click listeners for bottom navigation buttons
        feedButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Feed button clicked");
            viewPager.setCurrentItem(0);
            updateButtonState(0);
        });

        matchesButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Matches button clicked");
            viewPager.setCurrentItem(1);
            updateButtonState(1);
        });

        healthButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Health button clicked");
            viewPager.setCurrentItem(2);
            updateButtonState(2);
        });

        profileButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Profile button clicked");
            viewPager.setCurrentItem(3);
            updateButtonState(3);
        });

        // Set initial state
        updateButtonState(0);
    }

    private void updateButtonState(int position) {
        // Reset all buttons to unselected state
        feedButton.setSelected(false);
        matchesButton.setSelected(false);
        healthButton.setSelected(false);
        profileButton.setSelected(false);

        // Set the selected button and update icon
        switch (position) {
            case 0:
                feedButton.setSelected(true);
                feedButton.setImageResource(R.drawable.ic_feed);
                break;
            case 1:
                matchesButton.setSelected(true);
                matchesButton.setImageResource(R.drawable.ic_matches);
                break;
            case 2:
                healthButton.setSelected(true);
                healthButton.setImageResource(R.drawable.ic_health);
                break;
            case 3:
                profileButton.setSelected(true);
                profileButton.setImageResource(R.drawable.ic_profile);
                break;
        }
        // Reset unselected icons
        if (position != 0) feedButton.setImageResource(R.drawable.ic_feed);
        if (position != 1) matchesButton.setImageResource(R.drawable.ic_matches);
        if (position != 2) healthButton.setImageResource(R.drawable.ic_health);
        if (position != 3) profileButton.setImageResource(R.drawable.ic_profile);
    }

    // Custom PagerAdapter for fragments
    private class HomePagerAdapter extends FragmentStatePagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.d("MainActivity", "Loading FeedFragment");
                    return new FeedFragment();
                case 1:
                    Log.d("MainActivity", "Loading MatchesFragment");
                    return new MatchesFragment();
                case 2:
                    Log.d("MainActivity", "Loading HealthFragment");
                    return new HealthFragment();
                case 3:
                    Log.d("MainActivity", "Loading ProfileFragment");
                    return new ProfileFragment();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}