package com.example.shadowchatapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterationActivity extends AppCompatActivity {


    private final String BASE_URL = "https://shadowtalk-omega.vercel.app/api/register";
    private TextView tv_login;
    private OkHttpClient client = new OkHttpClient();
    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button signUp;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registeration);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // If user is already logged in, navigate to MainActivity
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(RegisterationActivity.this, MainActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        signUp = findViewById(R.id.btnSign);
        tv_login = findViewById(R.id.tv_login);

        tv_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterationActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signUp.setOnClickListener(v -> saveData(
                etEmail.getText().toString(),
                etUsername.getText().toString(),
                etPassword.getText().toString(),
                etConfirmPassword.getText().toString()
        ));
    }

    public void saveData(String email, String username, String password, String confirmPassword) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegisterationActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String json = "{" +
                "\"email\": \"" + email + "\"," +
                "\"username\": \"" + username + "\"," +
                "\"password\": \"" + password + "\"," +
                "\"confirm_password\": \"" + confirmPassword + "\"" +
                "}";

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterationActivity.this, "Server connection failed", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(res);

                            if (jsonObject.has("uaid")) {
                                int userId = jsonObject.getInt("uaid");

                                // Save user ID and login state
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("user_id", userId);
                                editor.putString("uaid", String.valueOf(userId));
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();

                                Toast.makeText(RegisterationActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();

                                etEmail.setText("");
                                etUsername.setText("");
                                etPassword.setText("");
                                etConfirmPassword.setText("");

                                Intent intent = new Intent(RegisterationActivity.this, UserProfileActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterationActivity.this, "Missing user ID in response", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterationActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterationActivity.this, "Registration failed: " + res, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}