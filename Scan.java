package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class Scan extends Activity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
       // setContentView(R.layout.agreement);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        if(isNetworkConnected()) {
            IntentIntegrator integrator = new IntentIntegrator(Scan.this);
            integrator.initiateScan();
        }else{
            Toast.makeText(Scan.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            Scan.this.finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult.getContents() != null) {
            final String[] elements = scanResult.getContents().toString().split("#");
            if(isNetworkConnected()){
                if(elements.length==3) {
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Toast.makeText(Scan.this, elements[2] + "#" + elements[0], Toast.LENGTH_SHORT).show();
                            if (dataSnapshot.child(elements[2].replace(".", ",")).child("jobs_future").child(elements[0]).exists() || dataSnapshot.child(elements[2].replace(".", ",")).child("jobs_current").child(elements[0]).exists()) {
                                if ((dataSnapshot.child("locations").child(elements[0]).child("date").getValue().toString()+" "+dataSnapshot.child("locations").child(elements[0]).child("time").getValue().toString()).equals(elements[1]) && dataSnapshot.child("locations").child(elements[0]).child("user").getValue().toString().equals(mAuth.getCurrentUser().getEmail().toString())) {
                                    //mRef.child(elements[2].replace(".", ",")).child("jobs_future").child(elements[0])
                                    if(dataSnapshot.child(elements[2].replace(".", ",")).child("approved").getValue().toString().equals("0")) {
                                        mRef.child(elements[2].replace(".", ",")).child("approved").setValue(1);
                                    }else{
                                        mRef.child(elements[2].replace(".", ",")).child("approved").setValue(2);
                                    }
                                    Intent intent = new Intent(Scan.this, Profile.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Scan.this, "Failed to authenticate1!", Toast.LENGTH_SHORT).show();
                                    Scan.this.finish();
                                }
                            } else {
                                Toast.makeText(Scan.this, "Failed to authenticate2!", Toast.LENGTH_SHORT).show();
                                Scan.this.finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Scan.this, "Failed to authenticate3!", Toast.LENGTH_SHORT).show();
                    Scan.this.finish();
                }
            }else{
                Scan.this.finish();
            }
        }else{
            Scan.this.finish();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
