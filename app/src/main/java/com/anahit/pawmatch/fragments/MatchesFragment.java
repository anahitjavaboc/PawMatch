package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.MatchesAdapter;
import com.anahit.pawmatch.models.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {

    private RecyclerView matchesRecyclerView;
    private MatchesAdapter matchesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        matchesRecyclerView = view.findViewById(R.id.matchesRecyclerView);
        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Match> dummyMatches = new ArrayList<>();
        dummyMatches.add(new Match(
                "1", "user1", "user2", "pet1",
                "Luna", "Alice",
                null, System.currentTimeMillis(), "Matched"
        ));
        dummyMatches.add(new Match(
                "2", "user3", "user4", "pet2",
                "Max", "Bob",
                null, System.currentTimeMillis() - 86400000, "Pending"
        ));

        matchesAdapter = new MatchesAdapter(dummyMatches, match -> {
            // Handle click on match item
            // For now, just log it or use a Toast
            // Toast.makeText(getContext(), "Clicked on " + match.getPetName(), Toast.LENGTH_SHORT).show();
        });

        matchesRecyclerView.setAdapter(matchesAdapter);

        return view;
    }
}
