package com.example.s23010340.authentication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.client.ClientDatabaseHelper;
import com.example.s23010340.authentication.labour.LabourDatabaseHelper;

public class ForgotPasswordActivity extends AppCompatActivity {
    public static final String EXTRA_ROLE = "extra_role";
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        role = getIntent().getStringExtra(EXTRA_ROLE);
        if (role == null) {
            role = SessionManager.ROLE_CLIENT;
        }

        EditText emailInput = findViewById(R.id.email_input);
        EditText newPasswordInput = findViewById(R.id.new_password_input);
        EditText confirmPasswordInput = findViewById(R.id.confirm_password_input);
        Button resetButton = findViewById(R.id.reset_button);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String newPass = newPasswordInput.getText().toString().trim();
            String confirmPass = confirmPasswordInput.getText().toString().trim();

            if (email.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (SessionManager.ROLE_CLIENT.equals(role)) {
                resetClientPassword(email, newPass);
            } else {
                resetLabourPassword(email, newPass);
            }
        });
    }

    private void resetClientPassword(String email, String newPass) {
        ClientDatabaseHelper db = new ClientDatabaseHelper(this);
        if (db.checkUserExists(email)) {
            if (db.updatePassword(email, newPass)) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetLabourPassword(String email, String newPass) {
        LabourDatabaseHelper db = new LabourDatabaseHelper(this);
        if (db.checkUserExists(email)) {
            if (db.updatePassword(email, newPass)) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
        }
    }
}
