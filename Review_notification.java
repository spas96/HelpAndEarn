package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
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


public class Review_notification extends Activity {

    private TextView description;
    private ImageView img;
    private TextView publisher;
    private TextView rating;
    private Button approve;
    private String notification_clicked;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ImageView back;
    private ListView myList;
    private CustomAdapter customAdapter;
    private String[] reviews_rating;
    private String[] reviews_description;
    private String[] reviews_date;
    private static int size;
    private TextView label;
    private String[] split;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_notification);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            split = extras.getString("user").split("/");
            notification_clicked = split[0];
        }

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        back = (ImageView) findViewById(R.id.back_review_notification);


        customAdapter = new CustomAdapter();
        myList = (ListView) findViewById(R.id.reviews_list);

        description = (TextView) findViewById(R.id.description_review_notification);
        img = (ImageView) findViewById(R.id.pic_user_notification);
        publisher = (TextView) findViewById(R.id.publisher_review_notification);
        rating = (TextView) findViewById(R.id.rating_review);
        approve = (Button) findViewById(R.id.approve_button);
        label = (TextView) findViewById(R.id.label);


        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);

        progressDialog.setMessage("Loading info..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(notification_clicked.replace(".",",")).child("jobs_future").child(split[1]).exists()){
                    approve.setText("DECLINE");
                }else{
                    approve.setText("APPROVE");
                }
                size = (int) dataSnapshot.child(notification_clicked.replace(".",",")).child("reviews").getChildrenCount();
                if(size>0) {
                    reviews_date = new String[size];
                    reviews_description = new String[size];
                    reviews_rating = new String[size];
                    for (int i = size-1; i >= 0; i--) {
                        reviews_rating[i] = dataSnapshot.child(notification_clicked.replace(".", ",")).child("reviews").child(size - i + "").child("rating").getValue().toString();
                        reviews_description[i] = dataSnapshot.child(notification_clicked.replace(".", ",")).child("reviews").child(size - i + "").child("description").getValue().toString();
                        reviews_date[i] = dataSnapshot.child(notification_clicked.replace(".", ",")).child("reviews").child(size - i + "").child("date").getValue().toString();
                    }
                }else{
                    label.setText("No reviews");
                }
                description.setText(dataSnapshot.child(notification_clicked.replace(".",",")).child("information").child("info").getValue().toString());
                setPic(notification_clicked);
                publisher.setText(dataSnapshot.child(notification_clicked.replace(".",",")).child("information").child("name").getValue().toString() + " " +
                        dataSnapshot.child(notification_clicked.replace(".",",")).child("information").child("last_name").getValue().toString());
                rating.setText(dataSnapshot.child(notification_clicked.replace(".",",")).child("information").child("rating").getValue().toString()+"/5");




                myList.setAdapter(customAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(approve.getText().equals("APPROVE")){
                    mRef.child(notification_clicked.replace(".",",")).child("jobs_future").child(split[1]).setValue(mAuth.getCurrentUser().getEmail().toString());
                    mRef.child(notification_clicked.replace(".",",")).child("jobs_applied").child(split[1]).removeValue();
                    approve.setText("DECLINE");
                }else{
                    mRef.child(notification_clicked.replace(".",",")).child("jobs_future").child(split[1]).removeValue();
                    approve.setText("APPROVE");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Review_notification.this.finish();
            }
        });


    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return size;

        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.notifications_list, null);


            view.setBackgroundColor(0x50f3ae1b);
            TextView title = (TextView) view.findViewById(R.id.notification_title);
            TextView description = (TextView) view.findViewById(R.id.notification_descrition);
            TextView date = (TextView) view.findViewById(R.id.notification_date);
            title.setText(reviews_rating[i]+"/5");
            description.setText(reviews_description[i]);
            date.setText(reviews_date[i]);

            return view;
        }
    }

    private void setPic(String name){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Photos").child(name);
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




}
