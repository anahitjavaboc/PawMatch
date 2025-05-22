package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, backToSignUpButton, testUserButton;
    private ProgressBar progressBar;

    // Test user credentials
    private static final String TEST_EMAIL = "individualproject2025@gmail.com";
    private static final String TEST_PASSWORD = "Samsung2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (BuildConfig.DEBUG) {
            auth.useEmulator("10.0.2.2", 9099); // Emulator for local testing
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        backToSignUpButton = findViewById(R.id.backToSignUpButton);
        testUserButton = findViewById(R.id.testUserButton);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Enter a valid email");
                emailEditText.requestFocus();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                passwordEditText.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
                                // Redirect to OwnerProfileCreationActivity instead of MainActivity
                                startActivity(new Intent(LoginActivity.this, OwnerProfileCreationActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        backToSignUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        // Add test user login functionality
        testUserButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            testUserButton.setEnabled(false);

            auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        testUserButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            // For test user, we'll skip email verification
                            startActivity(new Intent(LoginActivity.this, OwnerProfileCreationActivity.class));
                            finish();
                        } else {
                            // If test user doesn't exist, create it
                            auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                                    .addOnCompleteListener(createTask -> {
                                        if (createTask.isSuccessful()) {
                                            // Skip email verification for test user
                                            startActivity(new Intent(LoginActivity.this, OwnerProfileCreationActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to create test user: " + createTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        });
    }
}