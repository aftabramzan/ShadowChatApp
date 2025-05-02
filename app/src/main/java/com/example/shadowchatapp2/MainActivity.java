package com.example.shadowchatapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FrameLayout fragmentContainer;

    private FloatingActionButton fabCreate;
    private int currentUserUAID;
    ExtendedFloatingActionButton fabChatAI;

    ImageView search_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);
        search_icon = findViewById(R.id.search_icon);
        fabChatAI = findViewById(R.id.fab_chat_ai);
        fabCreate = findViewById(R.id.fab_create_post);


        search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        currentUserUAID = getIntent().getIntExtra("UAID", -1);

        // If UAID not in intent, try SharedPreferences
        if (currentUserUAID == -1) {
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            currentUserUAID = prefs.getInt("uaid", -1);
        }

        if (currentUserUAID == -1) {
            // If still no UAID, go back to login
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Default Fragment on Start
        loadFragment(new HomeFragment());

        // Bottom Nav Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.nav_explore) {
                loadFragment(new ExploreFragment());
            } else if (id == R.id.nav_chat) {
                loadFragment(new ChatFragment());
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            } else {
                return false;
            }

            return true;
        });

        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent for CreatePostActivity
                Intent createPostIntent = new Intent(MainActivity.this, CreatePostActivity.class);

                // Pass the UAID
                createPostIntent.putExtra("uaid", currentUserUAID);

                // Start the activity
                startActivity(createPostIntent);
            }
        });

        fabChatAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch ChatBotActivity
                Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent);
            }
        });
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }

}




