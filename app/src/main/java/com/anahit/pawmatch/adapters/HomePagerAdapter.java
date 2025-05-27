package com.anahit.pawmatch.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.anahit.pawmatch.fragments.MatchesFragment;
import com.anahit.pawmatch.fragments.ProfileFragment;
import com.anahit.pawmatch.fragments.SwipeFragment;
import com.anahit.pawmatch.fragments.HealthFragment;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new SwipeFragment();  // Feed
            case 1: return new MatchesFragment();
            case 2: return new HealthFragment();
            case 3: return new ProfileFragment();
            default: return new SwipeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Updated to 4 fragments
    }
}