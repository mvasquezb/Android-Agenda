package com.pmvb.agenda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

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

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initFirebaseAuth();

        boolean logout = getIntent().getBooleanExtra("logout", false);
        if (logout) {
            logOutUser();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            onLoginSuccess();
        }

        loginBtn.setOnClickListener(view -> login());
    }

    private void login() {
        // Validation
        if (!validate()) {
            onLoginFailed();
            return;
        }
        loginBtn.setEnabled(false);

        // Authentication Progress Dialog
        showProgressDialog();

        // Authentication
        handleAuth();
    }

    private void handleAuth() {
//        registerUser();
        loginUser(true);
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

    public void onLoginSuccess() {
        loginBtn.setEnabled(true);
        redirectHome();
    }

    private void redirectHome() {
        Intent success = new Intent(getApplicationContext(), EventListActivity.class);
        startActivity(success);
        finish();
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser() {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        registerUser(email, password);
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                this,
                task -> {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        onLoginFailed(task.getException().getMessage());
                    } else {
                        onLoginSuccess();
                    }
                    hideProgressDialog();
                });
    }

    private void loginUser() {
        loginUser(false);
    }

    private void loginUser(boolean autoRegister) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        loginUser(email, password, autoRegister);
    }

    private void loginUser(String email, String password, boolean autoRegister) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                this,
                task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        onLoginSuccess();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Exception ex = task.getException();
                        if (ex instanceof FirebaseAuthInvalidUserException) {
                            registerUser();
                        } else {
                            onLoginFailed(ex.getMessage());
                        }
                    }
                    hideProgressDialog();
                }
        );
    }

    public void onLoginFailed() {
        onLoginFailed(getString(R.string.login_failed));
    }

    public void onLoginFailed(String message) {
        Snackbar.make(
                emailText,
                message,
                Snackbar.LENGTH_LONG).show();
        loginBtn.setEnabled(true);
    }

    public void logOutUser() {
        mAuth.signOut();
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setMessage("Authenticating...");
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    public void onBackPressed() {
        // Disable going back to EventListActivity
        moveTaskToBack(true);
    }
}
