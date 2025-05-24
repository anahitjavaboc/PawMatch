package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.anahit.pawmatch.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;
    private Button signUpButton, backToSignInButton, testUserButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        if (BuildConfig.DEBUG) {
            auth.useEmulator("10.0.2.2", 9099);
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        signUpButton = findViewById(R.id.signUpButton);
        backToSignInButton = findViewById(R.id.backToSignInButton);
        testUserButton = findViewById(R.id.testUserButton); // Test User Button
        progressBar = findViewById(R.id.progressBar);

        // Set up visibility toggles for password fields
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

        // Test Mode Login for IndividualProject2025
        testUserButton.setOnClickListener(v -> {

            String testEmail = System.getenv("TEST_EMAIL");
            String testPassword = System.getenv("TEST_PASSWORD");



            progressBar.setVisibility(View.VISIBLE);
            testUserButton.setEnabled(false);

            auth.signInWithEmailAndPassword(testEmail, testPassword)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        testUserButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Logged in as Test User", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Test Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void updatePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length()); // Keeps cursor position unchanged
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

        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    signUpButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        auth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
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
                        Toast.makeText(this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
