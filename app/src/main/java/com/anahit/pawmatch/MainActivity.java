package com.anahit.pawmatch;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.anahit.pawmatch.fragments.FeedFragment;
import com.anahit.pawmatch.fragments.HealthFragment;
import com.anahit.pawmatch.fragments.MatchesFragment;
import com.anahit.pawmatch.fragments.ProfileFragment;
import com.anahit.pawmatch.models.Pet;
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
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private ViewPager2 viewPager;
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

        // Request notification permission for Android 13+
        requestNotificationPermission();

        checkUserProfile();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                // Now that permission is granted, schedule notifications
                scheduleVetAppointmentNotifications();
            } else {
                Log.w(TAG, "Notification permission denied");
                Toast.makeText(this, "Notification permission denied. Vet reminders won't work.", Toast.LENGTH_LONG).show();
            }
        }
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
                            runOnUiThread(() -> {
                                setupUI();
                                scheduleVetAppointmentNotifications();
                            });
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

    private void scheduleVetAppointmentNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot schedule notifications without permission");
            return;
        }

        executorService.execute(() -> {
            petsRef.orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                        Pet pet = petSnapshot.getValue(Pet.class);
                        if (pet != null && pet.getVetAppointments() != null) {
                            for (Map.Entry<String, Pet.VetAppointment> entry : pet.getVetAppointments().entrySet()) {
                                String apptKey = entry.getKey();
                                Pet.VetAppointment appt = entry.getValue();
                                long reminderTimestamp = appt.getReminderTimestamp();

                                if (reminderTimestamp > System.currentTimeMillis()) {
                                    Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
                                    intent.putExtra("pet_name", pet.getName());
                                    intent.putExtra("appt_date", appt.getDate());
                                    intent.putExtra("appt_key", apptKey);

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                            MainActivity.this,
                                            apptKey.hashCode(),
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                    );

                                    try {
                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimestamp, pendingIntent);
                                        Log.d(TAG, "Scheduled vet appointment reminder for " + pet.getName() + " at " + reminderTimestamp);
                                    } catch (SecurityException e) {
                                        Log.e(TAG, "Failed to schedule alarm: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Error fetching pets for notifications: " + error.getMessage(), error.toException());
                }
            });
        });
    }

    private void setupUI() {
        viewPager = findViewById(R.id.view_pager);
        feedButton = findViewById(R.id.feed_button);
        matchesButton = findViewById(R.id.matches_button);
        healthButton = findViewById(R.id.health_button);
        profileButton = findViewById(R.id.profile_button);

        HomePagerAdapter pagerAdapter = new HomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        String fragmentToLoad = getIntent().getStringExtra("fragment_to_load");
        int initialPosition = 0; // Default to Feed
        if ("profile".equals(fragmentToLoad)) {
            initialPosition = 3; // Profile is at position 3
        }
        viewPager.setCurrentItem(initialPosition, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "ViewPager switched to position: " + position);
                updateButtonState(position);
                // Refresh ProfileFragment if it's selected
                if (position == 3) {
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentByTag("f" + viewPager.getCurrentItem());
                    if (fragment instanceof ProfileFragment) {
                        ((ProfileFragment) fragment).refreshProfile();
                    }
                }
            }
        });

        feedButton.setOnClickListener(v -> {
            Log.d(TAG, "Feed button clicked");
            viewPager.setCurrentItem(0, false);
            updateButtonState(0);
        });

        matchesButton.setOnClickListener(v -> {
            Log.d(TAG, "Matches button clicked");
            viewPager.setCurrentItem(1, false);
            updateButtonState(1);
        });

        healthButton.setOnClickListener(v -> {
            Log.d(TAG, "Health button clicked");
            viewPager.setCurrentItem(2, false);
            updateButtonState(2);
        });

        profileButton.setOnClickListener(v -> {
            Log.d(TAG, "Profile button clicked");
            viewPager.setCurrentItem(3, false);
            updateButtonState(3);
        });

        updateButtonState(initialPosition);
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

    private class HomePagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        public HomePagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
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
                    return new FeedFragment(); // Fallback to FeedFragment
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String fragmentToLoad = intent.getStringExtra("fragment_to_load");
        if ("profile".equals(fragmentToLoad) && viewPager != null) {
            viewPager.setCurrentItem(3, false);
            updateButtonState(3);
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