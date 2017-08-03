package com.pmvb.tektonentry;

import android.app.ProgressDialog;
import android.app.usage.UsageEvents;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email)
    TextInputEditText emailText;
    @BindView(R.id.input_password)
    TextInputEditText passwordText;
    @BindView(R.id.btn_login)
    Button loginBtn;
    @BindView(R.id.link_signup)
    TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        boolean logout = getIntent().getBooleanExtra("logout", false);
        if (logout) {
            logOutUser();
        }

        loginBtn.setOnClickListener((view) -> {
            login();
        });

//        signupLink.setOnClickListener((view) -> {
//            // Start signup activity
//            Intent signup = new Intent(this, SignupActivity)
//        });
    }

    private void login() {
        // Validation
        if (!validate()) {
            onLoginFailed();
            return;
        }
        loginBtn.setEnabled(false);

        // Authentication Progress Dialog
        final ProgressDialog dialog = new ProgressDialog(
                this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Authenticating...");
        dialog.show();

        // Authentication

        // Post Handler
        new android.os.Handler().postDelayed(() -> {
            // Callback, login by default
            onLoginSuccess();
            dialog.dismiss();
        }, 1500);
    }

    private boolean validate() {
        // Basic validation
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("Password must be between 4 and 10 characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                onLoginSuccess();
            }
        }
    }

    public void onLoginSuccess() {
        loginBtn.setEnabled(true);
        SharedPreferences.Editor prefsEditor = getSharedPreferences(
                getString(R.string.prefs_file_key), MODE_PRIVATE).edit();
        prefsEditor.putBoolean(getString(R.string.loggedIn_pref_key), true);
        prefsEditor.apply();
        Intent success = new Intent(getApplicationContext(), EventListActivity.class);
        startActivity(success);
        finish();
    }

    public void onLoginFailed() {
        Snackbar.make(findViewById(R.id.input_email), "Login Failed", Snackbar.LENGTH_LONG).show();
        loginBtn.setEnabled(true);
    }

    public void logOutUser() {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(
                getString(R.string.prefs_file_key), MODE_PRIVATE).edit();
        prefsEditor.putBoolean(getString(R.string.loggedIn_pref_key), false);
        prefsEditor.apply();
    }
}