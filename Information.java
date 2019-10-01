package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Information extends Activity {

    private ImageView back;
    private ImageView save;
    private TextView email;
    private TextView names;
    private TextView bdate;
    private EditText info;
    private RelativeLayout layout;
    private LinearLayout scrollView;
    private boolean conn = false;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.information);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        layout = (RelativeLayout) findViewById(R.id.info_layout);
        scrollView = (LinearLayout) findViewById(R.id.info_scroll);
        back = (ImageView) findViewById(R.id.back_info);
        save = (ImageView) findViewById(R.id.save_info);

        email = (TextView) findViewById(R.id.info_email);
        names = (TextView) findViewById(R.id.info_names);
        bdate = (TextView) findViewById(R.id.info_bdate);
        info = (EditText) findViewById(R.id.info_info);

        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);


        if (isNetworkConnected()) {
            conn = true;
            progressDialog1.setMessage("Loading info..");
            progressDialog1.setCanceledOnTouchOutside(false);
            progressDialog1.show();

            mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("information").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    names.setText(dataSnapshot.child("name").getValue().toString() + " " + dataSnapshot.child("last_name").getValue().toString());
                    bdate.setText(dataSnapshot.child("birth_date").getValue().toString());
                    if (!dataSnapshot.child("info").getValue().toString().isEmpty()) {
                        info.setText(dataSnapshot.child("info").getValue().toString());
                    }
                    progressDialog1.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            conn = false;
            Toast.makeText(Information.this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }

        email.setText(mAuth.getCurrentUser().getEmail());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Information.this.finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(conn) {
                    progressDialog.setMessage("Saving..");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".", ",")).child("information").child("info").setValue(info.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Information.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Information.this, "Updating information failed!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }else{
                    Toast.makeText(Information.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
