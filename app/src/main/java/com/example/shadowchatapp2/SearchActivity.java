package com.example.shadowchatapp2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.privacysandbox.tools.core.model.Method;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchUsersActivity";
    private static final String LOCAL_USERS_KEY = "local_users";
    private static final String USER_SESSION = "UserSession";
    private static final String USER_PROFILE = "user_session";

    private TextInputEditText searchInput;
    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private UserSearchAdapter userAdapter;
    private List<User> users;
    private SharedPreferences sharedPreferences;
    private Gson gson;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Initialize views
        searchInput = findViewById(R.id.et_search);
        usersRecyclerView = findViewById(R.id.rv_users);
        progressBar = findViewById(R.id.progress_bar);
        noResultsText = findViewById(R.id.tv_no_results);
        ImageButton backButton = findViewById(R.id.btn_back);

        // Initialize SharedPreferences and Gson
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();

        // Initialize RecyclerView
        users = new ArrayList<>();
        userAdapter = new UserSearchAdapter(users);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        // Set up click listeners
        backButton.setOnClickListener(v -> finish());

        // Set up search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    searchUsersLocally(s.toString());
                } else {
                    users.clear();
                    userAdapter.notifyDataSetChanged();
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsersLocally(String query) {
        Log.d(TAG, "Searching for: " + query);

        // Get current user from UserSession
        SharedPreferences userSessionPrefs = getSharedPreferences(USER_SESSION, MODE_PRIVATE);
        String currentUsername = userSessionPrefs.getString("username", "");
        String currentUserId = userSessionPrefs.getString("uaid", "");

        Log.d(TAG, "Current user - Username: " + currentUsername + ", ID: " + currentUserId);

        // Get current user's profile image
        SharedPreferences userProfilePrefs = getSharedPreferences(USER_PROFILE, MODE_PRIVATE);
        String currentUserProfileImage = userProfilePrefs.getString("profile_image", "");

        Log.d(TAG, "Current user profile image: " + (currentUserProfileImage != null && !currentUserProfileImage.isEmpty()));

        // Create a list to store all users
        List<User> allUsers = new ArrayList<>();

        // Add current user if available
        if (!currentUsername.isEmpty() && !currentUserId.isEmpty()) {
            User currentUser = new User(currentUsername, currentUsername, currentUserProfileImage);
            allUsers.add(currentUser);
            Log.d(TAG, "Added current user to search list");
        }

        // Get locally stored users from cache
        String localUsersJson = sharedPreferences.getString(LOCAL_USERS_KEY, "[]");
        Log.d(TAG, "Local users JSON: " + localUsersJson);

        Type type = new TypeToken<List<User>>(){}.getType();
        List<User> cachedUsers = gson.fromJson(localUsersJson, type);

        if (cachedUsers != null && !cachedUsers.isEmpty()) {
            allUsers.addAll(cachedUsers);
            Log.d(TAG, "Added " + cachedUsers.size() + " cached users to search list");
        }

        // Filter users based on query
        List<User> filteredUsers = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        for (User user : allUsers) {
            if (user.getUsername().toLowerCase().contains(lowercaseQuery) ||
                    user.getName().toLowerCase().contains(lowercaseQuery)) {
                filteredUsers.add(user);
                Log.d(TAG, "Found matching user: " + user.getUsername());
            }
        }

        Log.d(TAG, "Total filtered users: " + filteredUsers.size());

        // Update UI with local results
        users.clear();
        users.addAll(filteredUsers);
        userAdapter.notifyDataSetChanged();
        noResultsText.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}