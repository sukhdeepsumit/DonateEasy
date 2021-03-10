package com.app_devs.DonateEasy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference reference;

    private FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        progressDialog.setMessage("Logging you in ...");

        countryCodePicker = findViewById(R.id.ccp);
        phoneNum = findViewById(R.id.editTextTextPersonName);
        countryCodePicker.registerCarrierNumberEditText(phoneNum);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("Users");

        googleSignInButton = findViewById(R.id.googleSignIn);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                //Log.i("CHECK_PLACE", "done");
                //FirebaseUser user = firebaseAuth.getCurrentUser();
                getLocation();
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
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getExactLocation();
        }
        else {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            getExactLocation();
        }
    }

    private void getExactLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
            Location location = task.getResult();
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(LoginActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    String country = String.valueOf(addresses.get(0).getCountryName());
                    String city = String.valueOf(addresses.get(0).getLocality());
                    String locality = String.valueOf(addresses.get(0).getAddressLine(0));

                    Log.i("CITY_NAME", city);

                    addUser(country, city, locality);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addUser(String country, String city, String locality) {
        Map<String, Object> map = new HashMap<>();

        map.put("country", country);
        map.put("city", city);
        map.put("address", locality);

        reference.document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).set(map);
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