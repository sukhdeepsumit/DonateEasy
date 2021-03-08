package com.app_devs.DonateEasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ProcessOTP extends AppCompatActivity {
    EditText getOTP;
    AppCompatButton verify;
    String phoneNumber, otpID;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_o_t_p);

        progressBar=findViewById(R.id.progressBar);
        mAuth=FirebaseAuth.getInstance();
        getOTP=findViewById(R.id.editText);
        verify=findViewById(R.id.verify);
        phoneNumber=getIntent().getStringExtra("phone");

        sendOTP();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getOTP.getText().toString().isEmpty())
                    Toast.makeText(ProcessOTP.this,"Blank Field can't be processed",Toast.LENGTH_LONG).show();
                else if (getOTP.getText().toString().length()!=6)
                    Toast.makeText(ProcessOTP.this,"Invalid OTP",Toast.LENGTH_LONG).show();
                else
                {
                    PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(otpID,getOTP.getText().toString());
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
                                otpID=s;


                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                signInWithPhoneAuthCredential(phoneAuthCredential);
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(ProcessOTP.this,e.getMessage(),Toast.LENGTH_LONG).show();

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
                            startActivity(new Intent(ProcessOTP.this,Dashboard.class));
                            finish();
                            Toast.makeText(ProcessOTP.this,"Logged In",Toast.LENGTH_SHORT).show();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(ProcessOTP.this,"Error",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}