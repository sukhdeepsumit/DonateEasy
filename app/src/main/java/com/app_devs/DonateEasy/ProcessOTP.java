package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app_devs.DonateEasy.Users.Dashboard;
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

import java.util.concurrent.TimeUnit;

//debug

public class ProcessOTP extends AppCompatActivity {
    EditText getInput1,getInput2,getInput3,getInput4,getInput5,getInput6;
    AppCompatButton verify;
    String phoneNumber, otpID,otp;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView resend;

    FusedLocationProviderClient locationProviderClient;
    CollectionReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_o_t_p);

        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();

        getInput1=findViewById(R.id.input1);
        getInput2=findViewById(R.id.input2);
        getInput3=findViewById(R.id.input3);
        getInput4=findViewById(R.id.input4);
        getInput5=findViewById(R.id.input5);
        getInput6=findViewById(R.id.input6);
        verify = findViewById(R.id.verify);
        resend=findViewById(R.id.resend);

        phoneNumber = getIntent().getStringExtra("phone");
        TextView phoneText=findViewById(R.id.getPhone);
        phoneText.setText(phoneNumber);


        reference = FirebaseFirestore.getInstance().collection("Users");
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sendOTP();


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getInput1.getText().toString().trim().isEmpty()||
                        getInput2.getText().toString().trim().isEmpty()||
                        getInput3.getText().toString().trim().isEmpty()||
                        getInput4.getText().toString().trim().isEmpty()||
                        getInput5.getText().toString().trim().isEmpty()||
                        getInput6.getText().toString().trim().isEmpty())
                {
                    Toast.makeText(ProcessOTP.this,"Invalid OTP",Toast.LENGTH_LONG).show();
                    return;
                }
                otp=    getInput1.getText().toString()+
                            getInput2.getText().toString()+
                                getInput3.getText().toString()+
                                    getInput4.getText().toString()+
                                        getInput5.getText().toString()+
                                             getInput6.getText().toString();


                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(otpID, otp);
                    signInWithPhoneAuthCredential(phoneAuthCredential);
                    progressBar.setVisibility(View.GONE);

            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });
    }
    private void setUpOtpInputs()
    {
        getInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {   }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                {
                    getInput2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {   }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                {
                    getInput3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {   }
        });
        getInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                {
                    getInput4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                {
                    getInput5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getInput5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty())
                {
                    getInput6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                                setUpOtpInputs();
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
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