package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Spas on 7/15/2018.
 */

public class Profile extends Activity {
    private ImageView logout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private BottomNavigationView mMainNav;
    private ImageView pic;
    private ImageView add;
    private TextView name;
    private StorageReference mStorage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    private Button account;
    private Button files;
    private Button information;
    private Button jobs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();



        pic = (ImageView) findViewById(R.id.profile_image);
        add = (ImageView) findViewById(R.id.add);
        name = (TextView) findViewById(R.id.name);
        mStorage = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);
        account = (Button) findViewById(R.id.account);
        files = (Button) findViewById(R.id.files);
        information = (Button) findViewById(R.id.information);
        jobs = (Button) findViewById(R.id.jobs);


        nav();

        logout();

        File file = new File("/data/data/com.example.spas.HelpAndEarn/app_Images/"+mAuth.getCurrentUser().getEmail()+".jpg");
        if(file.exists()) {
            pic.setImageURI(Uri.parse("/data/data/com.example.spas.HelpAndEarn/app_Images/" + mAuth.getCurrentUser().getEmail() + ".jpg"));
        }else{
            setPic();
        }

        //deleteee!!!

        if(isNetworkConnected()){
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name.setText(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("information").child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }

        pic.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isNetworkConnected()) {
                    change_pic();
                }else{
                    Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        add.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isNetworkConnected()) {
                    change_pic();
                }else{
                    Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild("memorable_word")) {
                                    Intent intent = new Intent(Profile.this, Account_settings.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(Profile.this, Google_acc_settings.class);
                                    startActivity(intent);
                                }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }else{
                        Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(isNetworkConnected()) {
                Intent intent = new Intent(Profile.this, Files.class);
                startActivity(intent);
            }else{
                Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
            }
        });

        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(isNetworkConnected()) {
                Intent intent = new Intent(Profile.this, Information.class);
                startActivity(intent);
            }else{
                Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
            }
        });

        jobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(isNetworkConnected()) {
                Intent intent = new Intent(Profile.this,Jobs.class);
                startActivity(intent);
            }else{
                Toast.makeText(Profile.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
            }
        });

    }

    private void logout(){
        logout = (ImageView) findViewById(R.id.logout);


        logout.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        try{
                            mAuth.signOut();
                            Toast.makeText(Profile.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Profile.this,Login.class);
                            startActivity(intent);
                        }
                        catch (Exception e){
                            Toast.makeText(Profile.this, "Logging out was unsuccessful!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    private void nav(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnve);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        mMainNav = (BottomNavigationView) findViewById(R.id.bnve);

        mMainNav.getMenu().getItem(3).setChecked(true);

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        item.setChecked(true);
                        Intent intent = new Intent(Profile.this,MapsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_scan:
                        item.setChecked(true);
                        Intent intent1 = new Intent(Profile.this,Scan.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_notice:
                        item.setChecked(true);
                        Intent intent2 = new Intent(Profile.this,Notifications.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_account:
                        item.setChecked(true);
                        break;

                }
                return false;
            }
        });
    }

    private void change_pic(){
        if(verifyPermitions()) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_INTENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


            if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
                progressDialog.setMessage("Uploading!");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri uri = data.getData();
                StorageReference filepath = mStorage.child("Photos").child(mAuth.getCurrentUser().getEmail());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        setPic();
                       // Glide.with(getApplicationContext()).load(mStorage.child("Photos/"+mAuth.getCurrentUser().getEmail()+".jpg").getDownloadUrl().getResult()).into(pic);

                        progressDialog.dismiss();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Fail!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            }

    }

    private boolean verifyPermitions(){
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0]) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(Profile.this,permissions,1);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        verifyPermitions();
    }

    private void setPic(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Photos").child(mAuth.getCurrentUser().getEmail());
        progressDialog1.setMessage("Loading image! Please wait!");
        progressDialog1.setCanceledOnTouchOutside(false);
        try {
            progressDialog1.show();
            final File localFile = File.createTempFile("images", "jpg");
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    pic.setImageBitmap(bitmap);
                    saveImg(bitmap);
                   // Toast.makeText(Profile.this, "Success!", Toast.LENGTH_SHORT).show();
                    progressDialog1.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    pic.setImageResource(R.drawable.ic_image_black_24dp);
                    progressDialog1.dismiss();
                }
            });
        } catch (IOException e ) {
            pic.setImageResource(R.drawable.ic_image_black_24dp);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void saveImg(Bitmap bitmap){
                /*
                    ContextWrapper
                        Proxying implementation of Context that simply delegates all of its calls
                        to another Context. Can be subclassed to modify behavior without
                        changing the original Context.
                */
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());



        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

                      //  Constructs a new File using the specified directory path and file name,
                       // placing a path separator between the two.


        // Create a file to save the image
        file = new File(file, mAuth.getCurrentUser().getEmail()+".jpg");

        try{
                        //    A writable sink for bytes.


            OutputStream stream = null;
                           // That writes bytes to a file.

            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            stream.flush();

            stream.close();


        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }




    }
}