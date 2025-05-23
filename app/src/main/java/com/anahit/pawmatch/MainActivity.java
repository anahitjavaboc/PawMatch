package com.anahit.pawmatch;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import com.anahit.pawmatch.adapters.HomePagerAdapter;

public class MainActivity extends AppCompatActivity {

    private TabHost tabHost;
    private ViewPager viewPager;
    private ImageButton feedButton, matchesButton, healthButton, profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Follow system theme for day/night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Initialize views
        tabHost = findViewById(android.R.id.tabhost);
        viewPager = findViewById(R.id.view_pager);
        feedButton = findViewById(R.id.feed_button);
        matchesButton = findViewById(R.id.matches_button);
        healthButton = findViewById(R.id.health_button);
        profileButton = findViewById(R.id.profile_button);

        // Setup ViewPager and TabHost
        tabHost.setup();
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Setup tabs
        tabHost.addTab(tabHost.newTabSpec("feed").setIndicator("Feed").setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("matches").setIndicator("Matches").setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("health").setIndicator("Health").setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("profile").setIndicator("Profile").setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        }));

        // Link TabHost with ViewPager
        tabHost.setOnTabChangedListener(tabId -> {
            int position = tabHost.getCurrentTab();
            viewPager.setCurrentItem(position);
            updateButtonState(position);
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
                updateButtonState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Set click listeners for bottom navigation buttons
        feedButton.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
            tabHost.setCurrentTab(0);
            updateButtonState(0);
        });

        matchesButton.setOnClickListener(v -> {
            viewPager.setCurrentItem(1);
            tabHost.setCurrentTab(1);
            updateButtonState(1);
        });

        healthButton.setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
            tabHost.setCurrentTab(2);
            updateButtonState(2);
        });

        profileButton.setOnClickListener(v -> {
            viewPager.setCurrentItem(3);
            tabHost.setCurrentTab(3);
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

        // Set the selected button
        switch (position) {
            case 0:
                feedButton.setSelected(true);
                break;
            case 1:
                matchesButton.setSelected(true);
                break;
            case 2:
                healthButton.setSelected(true);
                break;
            case 3:
                profileButton.setSelected(true);
                break;
        }
    }
}