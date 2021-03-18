package com.app_devs.DonateEasy.Users;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.app_devs.DonateEasy.MyProfile;
import com.app_devs.DonateEasy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DisplayNGO extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_n_g_o);
    }


//    private void checkUserExists() {
//        firebaseFirestore.collection("Users")
//                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        if (!task.getResult().exists()) {
//                            messageAlert();
//                        }
//                    }
//                });
//    }
//
//    private void messageAlert() {
//        new AlertDialog.Builder(this)
//                .setTitle("Location Error")
//                .setMessage("We cannot get your location\nDo you want to set it manually ?")
//                .setPositiveButton("YES", (dialogInterface, i) -> {
//                    startActivity(new Intent(getApplicationContext(), MyProfile.class));
//                    finish();
//                })
//                .setNegativeButton("NO", (dialogInterface, i) -> {
//                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                    finish();
//                })
//                .show();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        checkUserExists();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Dashboard.class));
        finish();
    }
}