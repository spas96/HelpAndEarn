package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Pre_review_help extends Activity {

    private ListView myList;
    private CustomAdapter customAdapter;
    private String[] job_titles;
    private String[] job_descriptions;
    private String title_clicked;
    private Double latitude_clicked;
    private Double longitude_clicked;
    private TextView label;
    private ImageView back;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private ProgressDialog progressDialog;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_review_help);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title_clicked = extras.getString("title");
        }


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        label = (TextView) findViewById(R.id.heading);

        //dobavi ogranichenie za dobavqne
        job_titles = new String[10];
        job_descriptions = new String[10];

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mRef.child("locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                latitude_clicked = Double.parseDouble(dataSnapshot.child(title_clicked).child("latitude").getValue().toString());
                longitude_clicked = Double.parseDouble(dataSnapshot.child(title_clicked).child("longitude").getValue().toString());
                latitude_clicked = Math.round(latitude_clicked * 1000.0) / 1000.0;
                longitude_clicked = Math.round(longitude_clicked * 1000.0) / 1000.0;
                int count=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(Math.round(Double.parseDouble(snapshot.child("latitude").getValue().toString()) * 1000.0) / 1000.0 == latitude_clicked &&  Math.round(Double.parseDouble(snapshot.child("longitude").getValue().toString()) * 1000.0) / 1000.0 == longitude_clicked && !snapshot.child("user").getValue().toString().equals(mAuth.getCurrentUser().getEmail().toString())){
                        job_titles[count] = snapshot.getKey().toString();
                        job_descriptions[count] = snapshot.child("description").getValue().toString();
                        count++;
                    }
                }
                label.setText(count + " requests for help found at the same place");
                if (count == 1){
                    progressDialog.dismiss();
                    Intent intent = new Intent(Pre_review_help.this,Review_help.class);
                    intent.putExtra("title",title_clicked);
                    startActivity(intent);
                    Pre_review_help.this.finish();
                }else{
                    progressDialog.dismiss();

                    myList.setAdapter(customAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


            job_titles[0] = title_clicked;

            customAdapter = new CustomAdapter();
            myList = (ListView) findViewById(R.id.help_requests);


            back = (ImageView) findViewById(R.id.back_pre_review);

            //  FILES = new String[5];


            //  progressDialog = new ProgressDialog(this);


            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                    Intent intent = new Intent(Pre_review_help.this,Review_help.class);
                    intent.putExtra("title",job_titles[i]);
                    startActivity(intent);

                    Toast.makeText(Pre_review_help.this, job_titles[i], Toast.LENGTH_SHORT).show();
                }
            });

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Pre_review_help.this.finish();
                }
            });

    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            int z=0;
            for (int i=0;i<job_titles.length;i++){
                if(job_titles[i]!=null){
                    z++;
                }
            }
            return z;

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
            view = getLayoutInflater().inflate(R.layout.pre_review_list, null);

            TextView title = (TextView) view.findViewById(R.id.pre_review_title);
            TextView description = (TextView) view.findViewById(R.id.pre_review_description);
            title.setText(job_titles[i]);
            description.setText(job_descriptions[i]);

            return view;
        }
    }


}
