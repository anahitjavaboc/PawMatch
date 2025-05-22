package com.anahit.pawmatch;

import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;
import com.anahit.pawmatch.adapters.HomePagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Follow system theme for day/night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Setup ViewPager and TabHost
        TabHost tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();

        ViewPager viewPager = findViewById(R.id.view_pager);
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
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
}