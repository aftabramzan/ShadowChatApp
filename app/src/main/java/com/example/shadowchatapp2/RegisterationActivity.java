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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterationActivity extends AppCompatActivity {


    private final String BASE_URL = "http://192.168.53.148/myapp/userAuth.php";
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
            finish();  // Close RegisterationActivity after navigating
            return;  // Exit to prevent further code execution
        }

        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        signUp = findViewById(R.id.btnSign);
        tv_login = findViewById(R.id.tv_login);

        // Redirect to LoginActivity on clicking "Login" link
        tv_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterationActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Handle sign-up functionality
        signUp.setOnClickListener(v -> saveData(
                etEmail.getText().toString(),
                etUsername.getText().toString(),
                etPassword.getText().toString(),
                etConfirmPassword.getText().toString()
        ));
    }

    public void saveData(String email, String username, String password, String confirmPassword) {
        // Validation (Optional, you can add more checks)
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegisterationActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody formData = new FormBody.Builder()
                .add("email", email)
                .add("username", username)
                .add("password", password)
                .add("confirm_password", confirmPassword)
                .build();

        Request request = new Request.Builder().url(BASE_URL).post(formData).build();

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
                        Toast.makeText(RegisterationActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();

                        // Save session state to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        // Clear input fields
                        etEmail.setText("");
                        etUsername.setText("");
                        etPassword.setText("");
                        etConfirmPassword.setText("");

                        // Go to profile
                        startActivity(new Intent(RegisterationActivity.this, UserProfileActivity.class));
                        finish();  // Close RegisterationActivity
                    } else {
                        Toast.makeText(RegisterationActivity.this, "Registration failed: " + res, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}