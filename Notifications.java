package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
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


public class Notifications extends Activity {

    private ListView myList;
    private CustomAdapter customAdapter;
    private String[] notification_titles;
    private String[] notification_descriptions;
    private String[] notification_dates;
    private String[] notification_users;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private static int last;
    private static int size;

    private TextView label;

    private BottomNavigationView mMainNav;

    private ProgressDialog progressDialog;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        customAdapter = new CustomAdapter();
        myList = (ListView) findViewById(R.id.notifications_list);

        label = (TextView) findViewById(R.id.notification_label);
        label.setVisibility(View.INVISIBLE);





        nav();

        if(isNetworkConnected()){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait!");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    last = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").getChildrenCount() - 1 - Integer.parseInt(dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child("old").getValue().toString());
                    size = (int) dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").getChildrenCount() - 1;
                    notification_titles = new String[size];
                    notification_descriptions = new String[size];
                    notification_dates = new String[size];
                    notification_users = new String[size];

                    mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child("old").setValue(size);

                    for(int i=size-1;i>=0;i--){
                        notification_titles[i] = dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child((size-i)+"").child("title").getValue().toString();
                        notification_descriptions[i] = dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child((size-i)+"").child("description").getValue().toString();
                        notification_dates[i] = dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child((size-i)+"").child("date").getValue().toString();
                        notification_users[i] = dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child((size-i)+"").child("user").getValue().toString();
                    }

                    int z=0;
                    for (int i=0;i<notification_titles.length;i++){
                        if(notification_titles[i]!=null){
                            z++;
                        }
                    }
                    if(z==0){
                        label.setText("No notifications!");
                        label.setVisibility(View.VISIBLE);
                    }else{
                        label.setVisibility(View.INVISIBLE);
                    }



                    myList.setAdapter(customAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            progressDialog.dismiss();
        }else{
            label.setText("No internet connection!");
            label.setVisibility(View.VISIBLE);
        }



        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(isNetworkConnected()) {
                    Intent intent = new Intent(Notifications.this, Review_notification.class);
                    intent.putExtra("user", notification_users[i]+"/"+notification_descriptions[i].substring(notification_descriptions[i].indexOf(" can help you with ") + 19,notification_descriptions[i].indexOf("! Click to check more!")));
                    startActivity(intent);
                }else{
                    Toast.makeText(Notifications.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void nav(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnve);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        mMainNav = (BottomNavigationView) findViewById(R.id.bnve);

        mMainNav.getMenu().getItem(2).setChecked(true);

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        item.setChecked(true);
                        Intent intent = new Intent(Notifications.this,MapsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_scan:
                        item.setChecked(true);
                        Intent intent1 = new Intent(Notifications.this,Scan.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_notice:
                        item.setChecked(true);
                        //Toast.makeText(Profile.this, "Notice clicked", Toast.LENGTH_SHORT).show();
                        Log.i("Info", "Notice");
                        break;
                    case R.id.nav_account:
                        item.setChecked(true);
                        Intent intent3 = new Intent(Notifications.this,Profile.class);
                        startActivity(intent3);
                        break;

                }
                return false;
            }
        });
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            int z=0;
            for (int i=0;i<notification_titles.length;i++){
                if(notification_titles[i]!=null){
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
            view = getLayoutInflater().inflate(R.layout.notifications_list, null);

            if(i<last) {
                view.setBackgroundColor(0xFFf3ae1b);
            }

            TextView title = (TextView) view.findViewById(R.id.notification_title);
            TextView description = (TextView) view.findViewById(R.id.notification_descrition);
            TextView date = (TextView) view.findViewById(R.id.notification_date);
            title.setText(notification_titles[i]);
            description.setText(notification_descriptions[i]);
            date.setText(notification_dates[i]);

            return view;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}
