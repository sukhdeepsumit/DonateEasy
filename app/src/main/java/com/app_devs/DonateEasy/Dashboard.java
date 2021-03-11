package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Dashboard extends AppCompatActivity {

    AppCompatButton logOut;

    FusedLocationProviderClient fusedLocationProviderClient;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    CollectionReference reference;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("Users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocation();

        logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Toast.makeText(Dashboard.this, "Logged Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    private void checkLocation() {
        if (ActivityCompat.checkSelfPermission(Dashboard.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
            Log.i("PERMISSION_CHECK", "GRANTED");
        }
        else {
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            Log.i("LOCATION_CHECK", String.valueOf(location));
            try {
                checkNullLocation(location);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    String country = String.valueOf(addresses.get(0).getCountryName());
                    String city = String.valueOf(addresses.get(0).getLocality());
                    String locality = String.valueOf(addresses.get(0).getAddressLine(0));

                    Log.i("CITY_CHECK", city);

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

        firestore.collection("Users").document().set(map);
    }

    private void checkNullLocation(Location location) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            if (location == null) {
                latch.countDown();
            }
        }).start();

        boolean done = latch.await(1, TimeUnit.SECONDS);
        Log.i("DONE_LATCH", String.valueOf(done));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("PERMISSION_CHECK", "GRANTED");
                checkLocation();
            }
        }
    }
}