package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class Dashboard extends AppCompatActivity {
    AppCompatButton logOut;

    FusedLocationProviderClient fusedLocationProviderClient;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    CollectionReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("Users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

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

    private void getLocation() {
        //check if permissions are given
        if (checkPermissions()) {
            //check if location isn't enabled
            if (!isLocationEnabled()) {
                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);
            }
            else {
                getAddress();
            }
        }
        else { requestPermissions(); }
    }

    private boolean checkPermissions() {
        return  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION },
                44);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void getAddress() {
        //getting last location from FusedLocationClient object
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();

            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(Dashboard.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    String country = String.valueOf(addresses.get(0).getCountryName());
                    String city = String.valueOf(addresses.get(0).getLocality());
                    String locality = String.valueOf(addresses.get(0).getAddressLine(0));

                    Log.i("CITY_NAME", city);

                    addUser(country, city, locality);
                }
                catch (IOException e) {
                    Toast.makeText(this, "Location error", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {
            switch (requestCode) {
                case 1 : super.onResume();
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLocation();
        }
    }
}