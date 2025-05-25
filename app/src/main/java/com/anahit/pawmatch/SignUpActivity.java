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

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;
    private Button signUpButton, backToSignInButton, testUserButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final long TIMEOUT_MS = 10000; // 10 seconds timeout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize ExecutorService and Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        auth = FirebaseAuth.getInstance();
        // Using production Firebase servers (emulator removed)

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        signUpButton = findViewById(R.id.signUpButton);
        backToSignInButton = findViewById(R.id.backToSignInButton);
        testUserButton = findViewById(R.id.testUserButton);
        progressBar = findViewById(R.id.progressBar);

        togglePasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility(passwordEditText, togglePasswordVisibility, isPasswordVisible);
        });

        toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            updatePasswordVisibility(confirmPasswordEditText, toggleConfirmPasswordVisibility, isConfirmPasswordVisible);
        });

        signUpButton.setOnClickListener(v -> registerUser());
        backToSignInButton.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        testUserButton.setOnClickListener(v -> loginTestUser());
    }

    private void updatePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

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
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        showLoading(true);
        final Runnable timeoutRunnable = () -> {
            showLoading(false);
            Toast.makeText(this, "Sign-up failed: Timeout or network error", Toast.LENGTH_LONG).show();
        };
        mainHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        executorService.execute(() -> {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        mainHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                        mainHandler.post(() -> {
                            if (task.isSuccessful()) {
                                auth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            showLoading(false);
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                                auth.signOut();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to send verification email: " + verifyTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                showLoading(false);
                                String errorMessage = "Sign-up failed: ";
                                if (task.getException() != null) {
                                    if (task.getException() instanceof FirebaseAuthException) {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(errorCode)) {
                                            errorMessage += "Email already in use";
                                        } else if ("ERROR_WEAK_PASSWORD".equals(errorCode)) {
                                            errorMessage += "Password is too weak";
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
        signUpButton.setEnabled(!isLoading);
        testUserButton.setEnabled(!isLoading);
        backToSignInButton.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}