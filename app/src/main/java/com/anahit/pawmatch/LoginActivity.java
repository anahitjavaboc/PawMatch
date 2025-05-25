package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ImageView togglePasswordVisibility;
    private Button loginButton, backToSignUpButton, testUserButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final long TIMEOUT_MS = 10000; // 10 seconds timeout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ExecutorService and Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        auth = FirebaseAuth.getInstance();
        // Using production Firebase servers (emulator removed)

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        loginButton = findViewById(R.id.loginButton);
        backToSignUpButton = findViewById(R.id.backToSignUpButton);
        testUserButton = findViewById(R.id.testUserButton);
        progressBar = findViewById(R.id.progressBar);

        togglePasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        loginButton.setOnClickListener(v -> loginUser());
        backToSignUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        testUserButton.setOnClickListener(v -> loginTestUser());
    }

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
        } else {
            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void loginUser() {
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

        showLoading(true);
        final Runnable timeoutRunnable = () -> {
            showLoading(false);
            Toast.makeText(this, "Login failed: Timeout or network error", Toast.LENGTH_LONG).show();
        };
        mainHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        executorService.execute(() -> {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        mainHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                        mainHandler.post(() -> {
                            showLoading(false);
                            if (task.isSuccessful()) {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    auth.signOut();
                                    Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                String errorMessage = "Login failed: ";
                                if (task.getException() != null) {
                                    if (task.getException() instanceof FirebaseAuthException) {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {
                                            errorMessage += "Incorrect password";
                                        } else if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                                            errorMessage += "User not found";
                                        } else if ("ERROR_TOO_MANY_REQUESTS".equals(errorCode)) {
                                            errorMessage += "Too many attempts, please try again later";
                                        } else {
                                            errorMessage += task.getException().getMessage();
                                        }
                                    } else {
                                        errorMessage += task.getException().getMessage();
                                    }
                                } else {
                                    errorMessage += "Unknown error";
                                }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });
        });
    }

    private void loginTestUser() {
        String testEmail = "individualproject2025@gmail.com";
        String testPassword = "Samsung2025";

        showLoading(true);
        final Runnable timeoutRunnable = () -> {
            showLoading(false);
            Toast.makeText(this, "Test Login failed: Timeout or network error", Toast.LENGTH_LONG).show();
        };
        mainHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        executorService.execute(() -> {
            auth.signInWithEmailAndPassword(testEmail, testPassword)
                    .addOnCompleteListener(task -> {
                        mainHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                        mainHandler.post(() -> {
                            showLoading(false);
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Logged in as Test User", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                String errorMessage = "Test Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        testUserButton.setEnabled(!isLoading);
        backToSignUpButton.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}