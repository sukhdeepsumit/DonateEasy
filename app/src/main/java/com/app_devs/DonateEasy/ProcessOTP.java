package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//debug

public class ProcessOTP extends AppCompatActivity {
    EditText getOTP;
    AppCompatButton verify;
    String phoneNumber, otpID;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    FusedLocationProviderClient locationProviderClient;
    CollectionReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_o_t_p);

        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        getOTP = findViewById(R.id.editText);
        verify = findViewById(R.id.verify);
        phoneNumber = getIntent().getStringExtra("phone");

        reference = FirebaseFirestore.getInstance().collection("Users");

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sendOTP();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getOTP.getText().toString().isEmpty())
                    Toast.makeText(ProcessOTP.this, "Blank Field can't be processed", Toast.LENGTH_LONG).show();
                else if (getOTP.getText().toString().length() != 6)
                    Toast.makeText(ProcessOTP.this, "Invalid OTP", Toast.LENGTH_LONG).show();
                else {
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(otpID, getOTP.getText().toString());
                    signInWithPhoneAuthCredential(phoneAuthCredential);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void sendOTP() {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                otpID = s;
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(ProcessOTP.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            getLocation();
                            startActivity(new Intent(ProcessOTP.this, Dashboard.class));
                            finish();
                            Toast.makeText(ProcessOTP.this, "Logged In", Toast.LENGTH_SHORT).show();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(ProcessOTP.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(ProcessOTP.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getExactLocation();
        } else {
            ActivityCompat.requestPermissions(ProcessOTP.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            getExactLocation();
        }
    }

    private void getExactLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    Geocoder geocoder = new Geocoder(ProcessOTP.this, Locale.getDefault());
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

        reference.document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).set(map);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            finish();
            startActivity(intent);
        }
    }
}