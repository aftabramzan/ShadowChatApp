package com.example.shadowchatapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePostActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.53.148/myapp/create_post.php";
    private OkHttpClient client = new OkHttpClient();

    private EditText titleInput;
    private EditText contentInput;
    private ImageView userProfileImage;
    private TextView usernameText;
    private MaterialButton postButton;
    private ImageButton backButton;
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
        userProfileImage = findViewById(R.id.iv_user_profile);
        usernameText = findViewById(R.id.tv_username);
        postButton = findViewById(R.id.btn_post);
        backButton = findViewById(R.id.btn_back);

        // Set username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        usernameText.setText(username);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        postButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter post content", Toast.LENGTH_SHORT).show();
                return;
            }

            createPost(title, content);
        });
    }

    private void createPost(String title, String content) {
        // Show loading state
        postButton.setEnabled(false);
        postButton.setText("Posting...");

        // Create form body
        RequestBody formBody = new FormBody.Builder()
                .add("uaid", String.valueOf(uaid))
                .add("title", title)
                .add("content", content)
                .add("sentiment_score", "0.0") // You can implement sentiment analysis later
                .build();

        // Create request
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(formBody)
                .build();

        // Execute request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreatePostActivity.this,
                            "Failed to create post: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    resetPostButton();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String responseMessage = json.getString("response");

                        if (responseMessage.equals("Post created successfully")) {
                            int postId = json.getInt("post_id");
                            JSONObject userData = json.getJSONObject("user_data");

                            Post newPost = new Post(
                                    String.valueOf(postId), // id
                                    String.valueOf(uaid), // userId
                                    userData.getString("username"), // username
                                    userData.getString("profile_image"), // userProfilePic
                                    content, // content
                                    title, // imageUrl (assuming `title` is the image URL here)
                                    "", // location (since you didn't provide a location, I set it to an empty string)
                                    System.currentTimeMillis() // timestamp
                            );

                            // Add to GlobalPosts if you're using it
                            GlobalPosts.getInstance().addPost(newPost);

                            Toast.makeText(CreatePostActivity.this,
                                    "Post created successfully!",
                                    Toast.LENGTH_SHORT).show();

                            finish();
                        } else {
                            Toast.makeText(CreatePostActivity.this,
                                    responseMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(CreatePostActivity.this,
                                "Error creating post: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } finally {
                        resetPostButton();
                    }
                });
            }
        });
    }

    private void resetPostButton() {
        postButton.setEnabled(true);
        postButton.setText("Post");
    }
}