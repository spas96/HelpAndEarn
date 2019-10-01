package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;


public class Account_settings extends Activity {

    private RelativeLayout accountSettings_layout;
    private LinearLayout accScroll;
    private ImageView back;
    private ImageView save;
    private TextView acc_email;

    private EditText oldPass;
    private EditText newPass;
    private EditText oldmWord;
    private EditText newmWord;
    private TextView selectedDistance;
    private SeekBar distanceSeekbar;

    private boolean changes = false;
    private boolean successfull_changes = false;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_settings);


        progressDialog = new ProgressDialog(this);
        accountSettings_layout = (RelativeLayout) findViewById(R.id.accountSettings_layout);
        accScroll = (LinearLayout) findViewById(R.id.scrollAcc);
        back = (ImageView) findViewById(R.id.back);
        save = (ImageView) findViewById(R.id.save);
        acc_email = (TextView) findViewById(R.id.acc_email);
        oldPass = (EditText) findViewById(R.id.oldpassword);
        newPass = (EditText) findViewById(R.id.newpassword);
        oldmWord = (EditText) findViewById(R.id.oldmword);
        newmWord = (EditText) findViewById(R.id.newmword);
        selectedDistance = (TextView) findViewById(R.id.selected_distance);
        distanceSeekbar = (SeekBar) findViewById(R.id.distance_seekbar);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();


        acc_email.setText(mAuth.getCurrentUser().getEmail());

        progressDialog.setMessage("Saving! Please wait!");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                distanceSeekbar.setProgress(Integer.parseInt(dataSnapshot.getValue().toString()));
                progressDialog.dismiss();
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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Account_settings.this.finish();
            }
        });

        accScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        accountSettings_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

    }


    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void save(){
        changes = false;
        int changes1 = 0;
        successfull_changes = false;

        progressDialog.setMessage("Saving! Please wait!");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(!TextUtils.isEmpty(oldPass.getText().toString())){
            changes = true;
            final FirebaseUser user = mAuth.getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass.getText().toString());

            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                removeWrongIcon(oldPass,R.drawable.ic_lock_white_24dp);
                                if(isPasswordValid(newPass.getText().toString())) {
                                    removeWrongIcon(newPass,R.drawable.ic_lock_white_24dp);
                                    user.updatePassword(newPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Account_settings.this,"Password updated successfully!",Toast.LENGTH_SHORT).show();
                                                successfull_changes = true;
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(Account_settings.this,Profile.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(Account_settings.this,"Error! Password not updated!",Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }else{
                                    setWrongIcon(newPass,R.drawable.ic_lock_white_24dp);
                                    Toast.makeText(Account_settings.this,"Password must contain letters and numbers!",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            } else {
                                setWrongIcon(oldPass,R.drawable.ic_lock_white_24dp);
                                Toast.makeText(Account_settings.this,"Authentication failed! Wrong old password!",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }else{
            if(!TextUtils.isEmpty(newPass.getText().toString())){
                changes=true;
                setWrongIcon(oldPass,R.drawable.ic_lock_white_24dp);
                Toast.makeText(Account_settings.this,"Authentication failed! Wrong old password!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }


        if(!TextUtils.isEmpty(oldmWord.getText().toString())){
            changes = true;
            mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("account").child("memorable_word").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (oldmWord.getText().toString().equals(dataSnapshot.getValue().toString())) {
                        removeWrongIcon(oldmWord, R.drawable.ic_security_white_24dp);
                        if (ismWordValid(newmWord.getText().toString())) {
                            removeWrongIcon(newmWord, R.drawable.ic_security_white_24dp);
                            mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("account").child("memorable_word").setValue(newmWord.getText().toString());
                            if(!successfull_changes) {
                                Intent intent = new Intent(Account_settings.this, Profile.class);
                                startActivity(intent);
                            }
                            successfull_changes = true;
                            oldmWord.setText("");
                            Toast.makeText(Account_settings.this, "Memorable word updated successfully!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            setWrongIcon(newmWord, R.drawable.ic_security_white_24dp);
                            Toast.makeText(Account_settings.this, "Memorable word must be a single word!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } else {
                        setWrongIcon(oldmWord, R.drawable.ic_security_white_24dp);
                        Toast.makeText(Account_settings.this, "Authentication failed! Wrong old memorable word!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }else{
            if(!TextUtils.isEmpty(newmWord.getText().toString())){
                changes=true;
                setWrongIcon(oldmWord,R.drawable.ic_lock_white_24dp);
                Toast.makeText(Account_settings.this,"Authentication failed! Wrong old memorable word!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }

        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(Integer.parseInt(dataSnapshot.getValue().toString())!=distanceSeekbar.getProgress()){
                    mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").setValue(distanceSeekbar.getProgress());
                    if(!successfull_changes) {
                        Intent intent = new Intent(Account_settings.this, Profile.class);
                        startActivity(intent);
                    }
                    Toast.makeText(Account_settings.this,"Distance updated successfully!",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else{
                    if(!changes && !successfull_changes){
                        Toast.makeText(Account_settings.this,"No changes were made!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public static boolean isPasswordValid(String password) {
        String expression = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean ismWordValid(String password) {
        String expression = "^[A-Za-z]+$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    private void setWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.wrong, 0);
    }
    private void removeWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

}
