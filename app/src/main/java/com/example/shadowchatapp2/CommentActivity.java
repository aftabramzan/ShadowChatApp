package com.example.shadowchatapp2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private EditText etComment;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private String postId;
    private String userId;
    private OkHttpClient client;
    private static final String BASE_URL = "https://shadowtalk-omega.vercel.app/api";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);
        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);

        // Get post ID from intent
        postId = getIntent().getStringExtra("post_id");
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = String.valueOf(prefs.getInt("uaid", -1));
        if (userId.equals("-1")) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this);
        recyclerView.setAdapter(adapter);

        // Setup click listeners
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> postComment());

        // Load comments
        loadComments();
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Request request = new Request.Builder()
                .url(BASE_URL + "/get-comments/" + postId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Toast.makeText(CommentActivity.this,
                            "Failed to load comments: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                });

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(CommentActivity.this,
                            "Failed to load comments: " + response.code(),
                            Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.getBoolean("success")) {
                        JSONArray commentsArray = jsonResponse.getJSONArray("comments");
                        List<Comment> comments = new ArrayList<>();

                        for (int i = 0; i < commentsArray.length(); i++) {
                            JSONObject commentJson = commentsArray.getJSONObject(i);
                            Comment comment = new Comment();
                            comment.setCommentId(commentJson.getInt("ComID"));
                            comment.setPid(commentJson.getInt("PID"));
                            comment.setUaid(commentJson.getInt("UAID"));
                            comment.setCommentText(commentJson.getString("Comment_text"));
                            comment.setCreatedAt(commentJson.getString("Created_at"));
                            String userName = commentJson.optString("user_name", "Anonymous");
                            if (userName == null || userName.equals("null") || userName.trim().isEmpty()) {
                                userName = "Anonymous";
                            }
                            comment.setUserName(userName);
                            comment.setProfileImage(commentJson.getString("profile_image"));
                            comments.add(comment);
                        }
                        // Update the commentsCount for this post in GlobalPosts
                        try {
                            GlobalPosts.getInstance(CommentActivity.this).updateCommentsCount(postId, comments.size());
                        } catch (Exception e) {
                            Log.e("CommentActivity", "Failed to update comments count: " + e.getMessage());
                        }

                        runOnUiThread(() -> adapter.updateComments(comments));
                    } else {
                        runOnUiThread(() -> {
                            try {
                                Toast.makeText(CommentActivity.this,
                                        jsonResponse.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> Toast.makeText(CommentActivity.this,
                            "Error parsing response: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void postComment() {
        String commentText = etComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent commenting on posts with a temporary or invalid postId
        int pidInt;
        try {
            pidInt = Integer.parseInt(postId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cannot comment on unsaved or invalid post.", Toast.LENGTH_LONG).show();
            return;
        }

        btnSend.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("pid", pidInt);
            jsonBody.put("uaid", Integer.parseInt(userId));
            jsonBody.put("comment_text", commentText);
            jsonBody.put("s_id", 1); // Default sentiment ID
            jsonBody.put("sentiment_score", 0.0);
            Log.d("CommentActivity", "Request body: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                Toast.makeText(CommentActivity.this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), jsonBody.toString());

        Request request = new Request.Builder()
                .url(BASE_URL + "/add-comment")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    Toast.makeText(CommentActivity.this,
                            "Failed to post comment: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                });

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error message";
                    Log.e("CommentActivity", "Error response: " + errorBody);
                    runOnUiThread(() -> Toast.makeText(CommentActivity.this,
                            "Failed to post comment: " + response.code() + " - " + errorBody,
                            Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d("CommentActivity", "Response body: " + responseBody);
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.getBoolean("success")) {
                        runOnUiThread(() -> {
                            etComment.setText("");
                            loadComments(); // Reload comments to show the new one
                        });
                    } else {
                        runOnUiThread(() -> {
                            try {
                                Toast.makeText(CommentActivity.this,
                                        jsonResponse.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (JSONException | IOException e) {
                    Log.e("CommentActivity", "Error parsing response", e);
                    runOnUiThread(() -> Toast.makeText(CommentActivity.this,
                            "Error parsing response: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}