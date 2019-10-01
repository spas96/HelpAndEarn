package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.view.Window;
import android.widget.Button;
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


public class Jobs extends Activity {

    private ImageView back;
    private Button current;
    private Button future;
    private Button applied;
    private Button past;
    private TextView no_jobs;
    private String[] titles;
    private String[] users;
    private String[] dates;
    private int a =1;
    private CustomAdapter customAdapter;
    private ListView myList;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private static int size=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.jobs);



        back = (ImageView) findViewById(R.id.back_jobs);
        current = (Button) findViewById(R.id.current);
        future = (Button) findViewById(R.id.future);
        applied = (Button) findViewById(R.id.applied);
        past = (Button) findViewById(R.id.past);
        no_jobs = (TextView) findViewById(R.id.no_jobs_title);
        no_jobs.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        buttonsChange(current,future,applied,past);
        if(isNetworkConnected()) {
            buttonsChange(current, future, applied, past);


            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressDialog = new ProgressDialog(Jobs.this);
                    progressDialog.setMessage("Loading current jobs!");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    if(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("jobs_current").exists()) {
                        no_jobs.setVisibility(View.INVISIBLE);
                        size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").getChildrenCount();

                        titles = new String[size];
                        users = new String[size];
                        dates = new String[size];

                        int i = 0;
                        for (DataSnapshot snapshot : dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").getChildren()) {
                            titles[i] = snapshot.getKey().toString();
                            users[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("user").getValue().toString();
                            dates[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("date").getValue().toString() + " " + dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("time").getValue().toString();
                            i++;
                        }

                        loadInfo(1);

                    }else{
                        clear();
                        no_jobs.setVisibility(View.VISIBLE);
                        no_jobs.setText("No current jobs!");
                    }

                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }

        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    buttonsChange(current, future, applied, past);


                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog = new ProgressDialog(Jobs.this);
                            progressDialog.setMessage("Loading current jobs!");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            if(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("jobs_current").exists()) {
                                no_jobs.setVisibility(View.INVISIBLE);
                                size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").getChildrenCount();

                                titles = new String[size];
                                users = new String[size];
                                dates = new String[size];

                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").getChildren()) {
                                    titles[i] = snapshot.getKey().toString();
                                    users[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("user").getValue().toString();
                                    dates[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("date").getValue().toString() + " " + dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("time").getValue().toString();
                                    i++;
                                }

                                loadInfo(1);

                            }else{
                                clear();
                                no_jobs.setVisibility(View.VISIBLE);
                                no_jobs.setText("No current jobs!");
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        future.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    buttonsChange(future, current, applied, past);


                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog = new ProgressDialog(Jobs.this);
                            progressDialog.setMessage("Loading future jobs!");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            if(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("jobs_future").exists()) {
                                no_jobs.setVisibility(View.INVISIBLE);
                                size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_future").getChildrenCount();

                                titles = new String[size];
                                users = new String[size];
                                dates = new String[size];

                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_future").getChildren()) {
                                    titles[i] = snapshot.getKey().toString();
                                    users[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("user").getValue().toString();
                                    dates[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("date").getValue().toString() + " " + dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("time").getValue().toString();
                                    i++;
                                }

                                loadInfo(2);

                            }else{
                                clear();
                                no_jobs.setVisibility(View.VISIBLE);
                                no_jobs.setText("No future jobs!");
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        applied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    buttonsChange(applied, future, current, past);


                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog = new ProgressDialog(Jobs.this);
                            progressDialog.setMessage("Loading applied jobs!");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();


                            if(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("jobs_applied").exists()) {
                                no_jobs.setVisibility(View.INVISIBLE);
                                size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_applied").getChildrenCount();

                                titles = new String[size];
                                users = new String[size];
                                dates = new String[size];

                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_applied").getChildren()) {
                                    titles[i] = snapshot.getKey().toString();
                                    users[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("user").getValue().toString();
                                    dates[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("date").getValue().toString() + " " + dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("time").getValue().toString();
                                    i++;
                                }

                                loadInfo(3);

                            }else{
                                clear();
                                no_jobs.setVisibility(View.VISIBLE);
                                no_jobs.setText("No applied jobs!");
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        past.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    buttonsChange(past, future, applied, current);


                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressDialog = new ProgressDialog(Jobs.this);
                            progressDialog.setMessage("Loading past jobs!");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();


                            if(dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("jobs_past").exists()) {
                                no_jobs.setVisibility(View.INVISIBLE);
                                size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_past").getChildrenCount();

                                titles = new String[size];
                                users = new String[size];
                                dates = new String[size];

                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_past").getChildren()) {
                                    titles[i] = snapshot.getKey().toString();
                                    users[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("user").getValue().toString();
                                    dates[i] = dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("date").getValue().toString() + " " + dataSnapshot.child("locations").child(snapshot.getKey().toString()).child("time").getValue().toString();
                                    i++;
                                }

                                loadInfo(4);

                            }else{
                                clear();
                                no_jobs.setVisibility(View.VISIBLE);
                                no_jobs.setText("No past jobs!");
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Jobs.this.finish();
            }
        });
    }

    private void loadInfo(final int x){
        customAdapter = new Jobs.CustomAdapter();
        myList = (ListView) findViewById(R.id.jobs_list);
        myList.setAdapter(customAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(x<3) {
                    if (isNetworkConnected()) {
                        Intent intent = new Intent(Jobs.this, QR_code.class);
                        intent.putExtra("info", titles[i] + "#" + dates[i] + "#" + users[i]);
                        //Toast.makeText(Jobs.this, titles[i]+"#"+dates[i]+"#"+users[i], Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    } else {
                        Toast.makeText(Jobs.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void buttonsChange(Button a, Button b, Button c, Button d){

        a.setTypeface(null, Typeface.BOLD);
        a.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        a.setBackgroundResource(R.drawable.jobs_button_clicked);

        b.setTypeface(null,Typeface.NORMAL);
        b.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        b.setBackgroundResource(R.drawable.jobs_button);

        c.setTypeface(null,Typeface.NORMAL);
        c.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        c.setBackgroundResource(R.drawable.jobs_button);

        d.setTypeface(null,Typeface.NORMAL);
        d.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        d.setBackgroundResource(R.drawable.jobs_button);
    }

    private void clear() {
        if(titles!=null) {
            for (int i = 0; i < titles.length; i++) {
                titles[i] = null;
                users[i] = null;
                dates[i] = null;
            }
            size = 0;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    class CustomAdapter extends BaseAdapter{

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
            view = getLayoutInflater().inflate(R.layout.jobs_list   , null);

            TextView title = (TextView) view.findViewById(R.id.job_title);
            TextView user = (TextView) view.findViewById(R.id.job_user);
            TextView date = (TextView) view.findViewById(R.id.job_date);
            title.setText(titles[i]);
            user.setText(users[i]);
            date.setText(dates[i]);

            return view;
        }
    }

}
