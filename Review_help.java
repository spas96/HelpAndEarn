package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Review_help extends Activity {

    private TextView title;
    private TextView description;
    private ImageView img;
    private TextView publisher;
    private TextView award;
    private TextView address;
    private TextView date;
    private TextView time;
    private TextView already_applied;
    private Button help_button;
    private String title_clicked;
    private Double latitude;
    private Double longitude;
    List<Address> Addresses;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ImageView back;

    private Geocoder geocoder;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_help);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title_clicked = extras.getString("title");
        }

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        back = (ImageView) findViewById(R.id.back_review);
        geocoder = new Geocoder(this, Locale.getDefault());

        title = (TextView) findViewById(R.id.title_review_help);
        description = (TextView) findViewById(R.id.description_review_help);
        img = (ImageView) findViewById(R.id.pic_help);
        publisher = (TextView) findViewById(R.id.publisher_review);
        award = (TextView) findViewById(R.id.award_review);
        address = (TextView) findViewById(R.id.address_review);
        date = (TextView) findViewById(R.id.date_review);
        time = (TextView) findViewById(R.id.time_review);
        already_applied = (TextView) findViewById(R.id.already_applied);
        help_button = (Button) findViewById(R.id.help_button);


        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);

        progressDialog.setMessage("Loading info..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                title.setText(title_clicked);
                description.setText(dataSnapshot.child("locations").child(title_clicked).child("description").getValue().toString());
                publisher.setText(dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).child("information").child("name").getValue().toString() + " " +
                        dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).child("information").child("last_name").getValue().toString());
                award.setText("£" + dataSnapshot.child("locations").child(title_clicked).child("award").getValue().toString());
                latitude = Double.parseDouble(dataSnapshot.child("locations").child(title_clicked).child("latitude").getValue().toString());
                longitude = Double.parseDouble(dataSnapshot.child("locations").child(title_clicked).child("longitude").getValue().toString());
                try {
                    Addresses = geocoder.getFromLocation(latitude,longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                address.setText(Addresses.get(0).getAddressLine(0).toString());
                date.setText(dataSnapshot.child("locations").child(title_clicked).child("date").getValue().toString());
                time.setText(dataSnapshot.child("locations").child(title_clicked).child("time").getValue().toString());

                File file = new File("/data/data/com.example.spas.HelpAndEarn/app_Images/" + title_clicked+date.getText().toString().replace("/","")+time.getText().toString()+".jpg");
                if(file.exists()) {
                    img.setImageURI(Uri.parse("/data/data/com.example.spas.HelpAndEarn/app_Images/" + title_clicked+date.getText().toString().replace("/","")+time.getText().toString()+".jpg"));
                }else{
                    setPic();
                }
                if (dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("jobs_applied").child(title_clicked).exists()){
                    help_button.setText("CANCEL JOB");
                    already_applied.setVisibility(View.VISIBLE);
                }else{
                    help_button.setText("I CAN HELP");
                    already_applied.setVisibility(View.INVISIBLE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        help_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("jobs_applied").child(title_clicked).exists()){

                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("jobs_applied").child(title_clicked).removeValue();

                            help_button.setText("I CAN HELP");
                            already_applied.setVisibility(View.INVISIBLE);
                        }else{
                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("jobs_applied").child(title_clicked).setValue(".");
                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mRef.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").child(dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").getChildrenCount()+"").child("date").setValue(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                                    mRef.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").child(dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").getChildrenCount()+"").child("title").setValue("Help response");
                                    mRef.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").child(dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").getChildrenCount()+"").child("description").setValue(dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("name").getValue().toString() + " " +
                                            dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("last_name").getValue().toString() + " can help you with "+ title_clicked +"! Click to check more!");
                                    mRef.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").child(dataSnapshot.child(dataSnapshot.child("locations").child(title_clicked).child("user").getValue().toString().replace(".",",")).
                                            child("notifications").getChildrenCount()+"").child("user").setValue(mAuth.getCurrentUser().getEmail().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            help_button.setText("CANCEL JOB");
                            already_applied.setVisibility(View.VISIBLE);
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
                Review_help.this.finish();
            }
        });


    }

    private void setPic(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Locations").child(title_clicked+date.getText().toString().replace("/","")+time.getText().toString());
        progressDialog1.setMessage("Loading image! Please wait!");
        progressDialog1.setCanceledOnTouchOutside(false);
        try {
            progressDialog1.show();
            final File localFile = File.createTempFile("images", "jpg");
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    img.setImageBitmap(bitmap);
                    saveImg(bitmap);
                    progressDialog1.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    img.setImageResource(R.drawable.ic_image_black_24dp);
                    progressDialog1.dismiss();
                }
            });
        } catch (IOException e ) {
            img.setImageResource(R.drawable.ic_image_black_24dp);
        }
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
        file = new File(file, title_clicked + date.getText().toString().replace("/", "") + time.getText().toString() + ".jpg");

        try {
            //    A writable sink for bytes.


            OutputStream stream = null;
            // That writes bytes to a file.

            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            stream.flush();

            stream.close();


        } catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

    }



}
