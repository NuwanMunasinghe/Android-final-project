package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.s23010340.R;
import com.example.s23010340.authentication.PasswordToggleUtils;
import com.example.s23010340.authentication.SessionManager;
import com.example.s23010340.authentication.ForgotPasswordActivity;
import java.util.concurrent.Executor;

public class LabourSignInActivity extends AppCompatActivity {
    private LabourDatabaseHelper databaseHelper;
    private BiometricPrompt biometricPrompt;
    private String pendingEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_sign_in);

        databaseHelper = new LabourDatabaseHelper(this);

        EditText emailInput = findViewById(R.id.labour_email);
        EditText passwordInput = findViewById(R.id.labour_password);
        PasswordToggleUtils.attach(passwordInput);

        setupBiometricPrompt();

        findViewById(R.id.labour_sign_in_container).setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean valid = databaseHelper.isValidUser(email, password);
            if (valid) {
                pendingEmail = email;
                checkBiometricAndPrompt();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.labour_go_sign_up).setOnClickListener(view -> {
            startActivity(new Intent(this, LabourSignUpActivity.class));
        });

        findViewById(R.id.labour_forgot_password).setOnClickListener(view -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra(ForgotPasswordActivity.EXTRA_ROLE, SessionManager.ROLE_LABOUR);
            startActivity(intent);
        });
    }

    private void checkBiometricAndPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(buildPromptInfo());
        } else {
            if (pendingEmail != null) {
                performLogin(pendingEmail);
            } else {
                Toast.makeText(this, "Biometric not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SessionManager sessionManager = new SessionManager(LabourSignInActivity.this);
                    
                    if (pendingEmail != null) {
                        performLogin(pendingEmail);
                    } else {
                        String lastEmail = sessionManager.getLastEmail();
                        String lastRole = sessionManager.getLastRole();
                        if (!lastEmail.isEmpty() && SessionManager.ROLE_LABOUR.equals(lastRole)) {
                            performLogin(lastEmail);
                        } else {
                            Toast.makeText(LabourSignInActivity.this, "Please sign in with password first", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        Toast.makeText(LabourSignInActivity.this, errString, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void performLogin(String email) {
        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        new SessionManager(this).saveLabourSession(email);
        Intent intent = new Intent(this, LabourAvailabilityActivity.class);
        intent.putExtra(LabourAvailabilityActivity.EXTRA_LABOUR_EMAIL, email);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private BiometricPrompt.PromptInfo buildPromptInfo() {
        return new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity Verification")
            .setSubtitle("Confirm it's you to continue")
            .setNegativeButtonText("Cancel")
            .build();
    }
}
