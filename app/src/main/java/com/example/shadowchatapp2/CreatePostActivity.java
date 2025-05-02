package com.example.shadowchatapp2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePostActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://shadowtalk-omega.vercel.app/api/create-post";
    private OkHttpClient client = new OkHttpClient();

    private EditText titleInput;
    private EditText contentInput;
    private MaterialButton addImageButton;
    private MaterialButton addLocationButton;
    private ImageButton backButton;
    private MaterialButton postButton;
    private ImageView postImageView;
    private ImageButton removeImageButton;
    private ImageView userProfileImage;
    private TextView usernameText;

    private Uri selectedImageUri;
    private String selectedLocation;
    private int uaid;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);


        uaid = getIntent().getIntExtra("uaid", -1);

        if (uaid == -1) {
            // If no UAID, try getting from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            uaid = prefs.getInt("uaid", -1);

            if (uaid == -1) {
                Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.et_post_title);
        contentInput = findViewById(R.id.et_post_content);
        addImageButton = findViewById(R.id.btn_add_photo);
        addLocationButton = findViewById(R.id.btn_add_video);
        backButton = findViewById(R.id.btn_back);
        postButton = findViewById(R.id.btn_post);
        postImageView = findViewById(R.id.iv_post_image);

        userProfileImage = findViewById(R.id.iv_user_profile);
        usernameText = findViewById(R.id.tv_username);

        // Set username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        usernameText.setText(username);

        // Load and display profile image from SharedPreferences
        String profileImage = prefs.getString("profile_image", "");
        if (profileImage != null && !profileImage.isEmpty()) {
            Glide.with(this)
                    .load("data:image/jpeg;base64," + profileImage)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(userProfileImage);
        } else {
            userProfileImage.setImageResource(R.drawable.profile_placeholder);
        }
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addImageButton.setOnClickListener(v -> openImagePicker());


        addLocationButton.setOnClickListener(v -> {
            selectedLocation = "New York, USA";
            Toast.makeText(CreatePostActivity.this,
                    "Location set to: " + selectedLocation, Toast.LENGTH_SHORT).show();
        });

        postButton.setOnClickListener(v -> createPost());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Display the selected image
            postImageView.setVisibility(View.VISIBLE);


            Glide.with(this)
                    .load(selectedImageUri)
                    .into(postImageView);
        }
    }

    private void createPost() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter post title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter post content", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        postButton.setEnabled(false);
        postButton.setText("Posting...");

        // Log what we're about to send for debugging
        Log.d(TAG, "Attempting to create post with UAID: " + uaid);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Content: " + content);

        // Create JSON payload with all required fields
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("uaid", uaid);
            jsonBody.put("title", title);
            jsonBody.put("content", content);
            // Add these if your backend requires them
            jsonBody.put("sentiment_score", 0.0); // Default value or calculate sentiment
            jsonBody.put("s_id", 1); // Default value or get from somewhere
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
            resetPostButton();
            return;
        }

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString()
        );

        // Create the request
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMsg = "Request failed: " + e.getMessage();
                Log.e(TAG, errorMsg);

                runOnUiThread(() -> {
                    Toast.makeText(CreatePostActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    resetPostButton();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Server response: " + responseBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Log the raw response for debugging
                            Log.d(TAG, "Raw server response: " + responseBody);

                            // Create a new Post object
                            JSONObject responseJson = new JSONObject(responseBody);
                            Log.d(TAG, "Parsed JSON response: " + responseJson.toString());

                            String postId;
                            if (responseJson.has("post_id")) {
                                postId = String.valueOf(responseJson.getInt("post_id"));
                            } else {
                                Toast.makeText(CreatePostActivity.this, "Failed to get post ID from server.", Toast.LENGTH_LONG).show();
                                resetPostButton();
                                return;
                            }
                            String username = usernameText.getText().toString();

                            Log.d(TAG, "Creating new post with ID: " + postId + ", username: " + username);

                            Post newPost = new Post(
                                    postId,
                                    String.valueOf(uaid),
                                    username,
                                    "", // user image URL
                                    content,
                                    selectedImageUri != null ? selectedImageUri.toString() : null,
                                    selectedLocation,
                                    System.currentTimeMillis()
                            );

                            // Add the post to GlobalPosts
                            GlobalPosts.getInstance(CreatePostActivity.this).addPost(newPost);

                            Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing response: " + e.getMessage());
                            Log.e(TAG, "Stack trace: ", e);
                            Toast.makeText(CreatePostActivity.this,
                                    "Error processing response: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CreatePostActivity.this,
                                "Error: " + response.code() + " - " + responseBody,
                                Toast.LENGTH_SHORT).show();
                    }
                    resetPostButton();
                });
            }
        });
    }
    private void resetPostButton() {
        postButton.setEnabled(true);
        postButton.setText("Post");
    }
}