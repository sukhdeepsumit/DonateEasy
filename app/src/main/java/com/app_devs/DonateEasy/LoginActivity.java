package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    //push check again

    GoogleSignInButton googleSignInButton;
    ProgressDialog progressDialog;

    AppCompatButton getOTP;
    EditText phoneNum;
    CountryCodePicker countryCodePicker;

    private GoogleSignInClient myGoogleSignInClient;
    private final static int RC_SIGN_IN = 007;

    FirebaseAuth firebaseAuth;

    LocationSettingsRequest.Builder builder;
    private final int REQUEST_CHECK_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getLocation();

        firebaseAuth = FirebaseAuth.getInstance();

        getOTP = findViewById(R.id.getOTP);
        getOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(phoneNum.getText().toString()))
                {
                    phoneNum.setError("Empty field");
                    phoneNum.requestFocus();
                }
                else {
                    Intent intent = new Intent(LoginActivity.this, ProcessOTP.class);
                    intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus().trim());
                    startActivity(intent);
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Khadjaa penchodd ...");

        countryCodePicker = findViewById(R.id.ccp);
        phoneNum = findViewById(R.id.editTextTextPersonName);
        countryCodePicker.registerCarrierNumberEditText(phoneNum);

        googleSignInButton = findViewById(R.id.googleSignIn);

        createRequest();

        googleSignInButton.setOnClickListener(view -> {
            SignInWithGoogle();
        });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        myGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void SignInWithGoogle() {
        Intent signInIntent = myGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                progressDialog.dismiss();
                finish();
                startActivity(intent);
            } else {
                Toast.makeText(this, "Authentication Failed !!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocation() {
        LocationRequest request = new LocationRequest()
                .setFastestInterval(1500)
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(this, task -> {
            try {
                task.getResult(ApiException.class);
            }
            catch (ApiException e) {
                switch (e.getStatusCode())
                {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED :
                        try
                        {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_CODE);
                        }
                        catch (IntentSender.SendIntentException sendIntentException)
                        {
                            sendIntentException.printStackTrace();
                        }
                        catch (ClassCastException ignored) {  }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: break;
                }
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            finish();
            startActivity(intent);
        }
    }
}