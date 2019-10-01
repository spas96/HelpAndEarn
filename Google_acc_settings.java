package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Google_acc_settings extends Activity {

    private ImageView back;
    private ImageView save;
    private TextView email;

    private ProgressDialog progressDialog;

    private TextView selectedDistance;
    private SeekBar distanceSeekbar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_account_settings);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();


        selectedDistance = (TextView) findViewById(R.id.selected_distance_g);
        distanceSeekbar = (SeekBar) findViewById(R.id.distance_seekbar_g);

        progressDialog = new ProgressDialog(this);

        back = (ImageView) findViewById(R.id.g_acc_back);
        save = (ImageView) findViewById(R.id.g_acc_save);
        email = (TextView) findViewById(R.id.g_acc_email);

        email.setText(mAuth.getCurrentUser().getEmail().toString());

        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                distanceSeekbar.setProgress(Integer.parseInt(dataSnapshot.getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        distanceSeekbar.setMax(200);
        selectedDistance.setText(distanceSeekbar.getProgress() + " km");
        distanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(distanceSeekbar.getProgress() < 1) {
                    seekBar.setProgress(1);
                }
                selectedDistance.setText(distanceSeekbar.getProgress() + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                selectedDistance.setText(distanceSeekbar.getProgress() + " km");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Saving! Please wait!");
                progressDialog.show();
                mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(Integer.parseInt(dataSnapshot.getValue().toString())!=distanceSeekbar.getProgress()){
                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").setValue(distanceSeekbar.getProgress());
                            Intent intent = new Intent(Google_acc_settings.this, Profile.class);
                            startActivity(intent);
                            Toast.makeText(Google_acc_settings.this,"Distance updated successfully!",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }else{
                            Toast.makeText(Google_acc_settings.this,"No changes were made!",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Google_acc_settings.this,Profile.class);
                startActivity(intent);
            }
        });
    }

}
