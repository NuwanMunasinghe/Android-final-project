package com.example.s23010340.authentication.client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.s23010340.client.ClientDashboardActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.PasswordToggleUtils;
import com.example.s23010340.authentication.SessionManager;
import com.example.s23010340.authentication.ForgotPasswordActivity;
import java.util.concurrent.Executor;

public class ClientSignInActivity extends AppCompatActivity {
    private ClientDatabaseHelper databaseHelper;
    private BiometricPrompt biometricPrompt;
    private String pendingEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_sign_in);

        databaseHelper = new ClientDatabaseHelper(this);

        EditText emailInput = findViewById(R.id.client_email);
        EditText passwordInput = findViewById(R.id.client_password);
        PasswordToggleUtils.attach(passwordInput);

        setupBiometricPrompt();

        findViewById(R.id.client_sign_in_container).setOnClickListener(view -> {
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

        findViewById(R.id.client_go_sign_up).setOnClickListener(view -> {
            startActivity(new Intent(this, ClientSignUpActivity.class));
        });

        findViewById(R.id.client_forgot_password).setOnClickListener(view -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra(ForgotPasswordActivity.EXTRA_ROLE, SessionManager.ROLE_CLIENT);
            startActivity(intent);
        });
    }

    private void checkBiometricAndPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(buildPromptInfo());
        } else {
            // If biometric not available but password was valid, just log in
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
                    SessionManager sessionManager = new SessionManager(ClientSignInActivity.this);
                    
                    if (pendingEmail != null) {
                        // User just clicked login and verified fingerprint
                        performLogin(pendingEmail);
                    } else {
                        // User clicked fingerprint icon
                        String lastEmail = sessionManager.getLastEmail();
                        String lastRole = sessionManager.getLastRole();
                        if (!lastEmail.isEmpty() && SessionManager.ROLE_CLIENT.equals(lastRole)) {
                            performLogin(lastEmail);
                        } else {
                            Toast.makeText(ClientSignInActivity.this, "Please sign in with password first", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        Toast.makeText(ClientSignInActivity.this, errString, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void performLogin(String email) {
        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
        new SessionManager(this).saveClientSession(email);
        openClientDashboard();
    }

    private BiometricPrompt.PromptInfo buildPromptInfo() {
        return new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity Verification")
            .setSubtitle("Confirm it's you to continue")
            .setNegativeButtonText("Cancel")
            .build();
    }

    private void openClientDashboard() {
        Intent intent = new Intent(this, ClientDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
