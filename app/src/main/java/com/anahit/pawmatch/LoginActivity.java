package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.anahit.pawmatch.BuildConfig; // Corrected import
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, backToSignUpButton, testUserButton;
    private ImageView togglePasswordVisibility;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    // Test user credentials
    private static final String TEST_EMAIL = "individualproject2025@gmail.com";
    private static final String TEST_PASSWORD = "Samsung2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (BuildConfig.DEBUG) {
            try {
                auth.useEmulator("10.0.2.2", 9099); // Emulator for local testing
            } catch (Exception e) {
                Toast.makeText(this, "Firebase emulator setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // Check if user is already logged in
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirect to MainActivity for flow check
            finish();
            return;
        }

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        loginButton = findViewById(R.id.loginButton);
        backToSignUpButton = findViewById(R.id.backToSignUpButton);
        testUserButton = findViewById(R.id.testUserButton);
        progressBar = findViewById(R.id.progressBar);

        // Password visibility toggle
        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Login button click
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
            backToSignUpButton.setEnabled(false);
            testUserButton.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        backToSignUpButton.setEnabled(true);
                        testUserButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            if (auth.getCurrentUser() != null) {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirect to MainActivity
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "User not found. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Back to sign-up button click
        backToSignUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        // Test user login functionality
        testUserButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            backToSignUpButton.setEnabled(false);
            testUserButton.setEnabled(false);

            auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            backToSignUpButton.setEnabled(true);
                            testUserButton.setEnabled(true);

                            startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirect to MainActivity
                            finish();
                        } else {
                            auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                                    .addOnCompleteListener(createTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        loginButton.setEnabled(true);
                                        backToSignUpButton.setEnabled(true);
                                        testUserButton.setEnabled(true);

                                        if (createTask.isSuccessful()) {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirect to MainActivity
                                            finish();
                                        } else {
                                            String errorMessage = createTask.getException() != null ? createTask.getException().getMessage() : "Unknown error";
                                            Toast.makeText(LoginActivity.this, "Failed to create test user: " + errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        });
    }
}