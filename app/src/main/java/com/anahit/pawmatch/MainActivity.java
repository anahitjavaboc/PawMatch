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

    private static final String TAG = "MainActivity";
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

        executorService = Executors.newSingleThreadExecutor();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, redirecting to login.");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        userId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        checkUserProfile();
    }

    private void checkUserProfile() {
        executorService.execute(() -> {
            try {
                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.i(TAG, "No user profile found, redirecting to OwnerProfileCreationActivity.");
                            Intent intent = new Intent(MainActivity.this, OwnerProfileCreationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            checkPetProfile();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error checking user profile: " + error.getMessage(), error.toException());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in checkUserProfile: " + e.getMessage(), e);
            }
        });
    }

    private void checkPetProfile() {
        executorService.execute(() -> {
            try {
                petsRef.orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.i(TAG, "No pet profile found, redirecting to PetProfileCreationActivity.");
                            Intent intent = new Intent(MainActivity.this, PetProfileCreationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            runOnUiThread(() -> setupUI());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error checking pet profile: " + error.getMessage(), error.toException());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in checkPetProfile: " + e.getMessage(), e);
            }
        });
    }

    @SuppressWarnings("deprecation") // Suppress warning for FragmentStatePagerAdapter
    private void setupUI() {
        viewPager = findViewById(R.id.view_pager);
        feedButton = findViewById(R.id.feed_button);
        matchesButton = findViewById(R.id.matches_button);
        healthButton = findViewById(R.id.health_button);
        profileButton = findViewById(R.id.profile_button);

        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "ViewPager switched to position: " + position);
                updateButtonState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        feedButton.setOnClickListener(v -> {
            Log.d(TAG, "Feed button clicked");
            viewPager.setCurrentItem(0);
            updateButtonState(0);
        });

        matchesButton.setOnClickListener(v -> {
            Log.d(TAG, "Matches button clicked");
            viewPager.setCurrentItem(1);
            updateButtonState(1);
        });

        healthButton.setOnClickListener(v -> {
            Log.d(TAG, "Health button clicked");
            viewPager.setCurrentItem(2);
            updateButtonState(2);
        });

        profileButton.setOnClickListener(v -> {
            Log.d(TAG, "Profile button clicked");
            viewPager.setCurrentItem(3);
            updateButtonState(3);
        });

        updateButtonState(0);
    }

    private void updateButtonState(int position) {
        feedButton.setSelected(false);
        matchesButton.setSelected(false);
        healthButton.setSelected(false);
        profileButton.setSelected(false);

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
        if (position != 0) feedButton.setImageResource(R.drawable.ic_feed);
        if (position != 1) matchesButton.setImageResource(R.drawable.ic_matches);
        if (position != 2) healthButton.setImageResource(R.drawable.ic_health);
        if (position != 3) profileButton.setImageResource(R.drawable.ic_profile);
    }

    private class HomePagerAdapter extends FragmentStatePagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.d(TAG, "Loading FeedFragment");
                    return new FeedFragment();
                case 1:
                    Log.d(TAG, "Loading MatchesFragment");
                    return new MatchesFragment();
                case 2:
                    Log.d(TAG, "Loading HealthFragment");
                    return new HealthFragment();
                case 3:
                    Log.d(TAG, "Loading ProfileFragment");
                    return new ProfileFragment();
                default:
                    Log.w(TAG, "Invalid position: " + position);
                    return null; // Maintain original behavior
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