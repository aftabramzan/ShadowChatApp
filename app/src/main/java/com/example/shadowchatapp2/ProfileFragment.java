package com.example.shadowchatapp2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;


public class ProfileFragment extends Fragment {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.nav_view);
        menuButton = view.findViewById(R.id.menu_button);

        // Set up navigation drawer
        setupNavigationDrawer();

        return view;
    }

    private void setupNavigationDrawer() {
        // Menu button click listener
        menuButton.setOnClickListener(v -> drawerLayout.open());

        // Navigation item click listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_edit_profile) {
                // Handle edit profile
                Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                // Handle settings
                Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_privacy) {
                // Handle privacy
                Toast.makeText(getContext(), "Privacy clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                // Handle logout
                logout();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Update navigation header with user info
        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView usernameText = headerView.findViewById(R.id.nav_header_username);
        TextView emailText = headerView.findViewById(R.id.nav_header_email);

        // TODO: Replace with actual user data
        usernameText.setText("John Doe");
        emailText.setText("john.doe@example.com");
    }

    private void logout() {
        // Clear UserSession
        SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();

        // Clear user_session
        SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.clear();
        editor2.apply();

        // Navigate to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}