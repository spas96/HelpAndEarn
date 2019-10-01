package com.example.spas.HelpAndEarn;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    public static final int REQUEST_LOCATION_CODE = 99;
    private boolean first_zoom=false;
    private BottomNavigationView bottomNavigationView;
    private ImageButton add;
    private Button infoCount;
    private RelativeLayout publish_layout;

    private Display display;
    private Point size;
    private LayoutInflater inflater;
    private PopupWindow pw;
    private ImageView back_publish;
    private EditText title_publish;
    private EditText description_publish;
    private EditText date_help;
    private EditText time_help;
    private EditText award_help;
    private EditText img_help;
    private Uri filePath;
    private Button publish_button;
    CameraPosition cameraPosition;
    Location targetLocation;

    private Calendar calendar;
    private Date minDate;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private ProgressDialog progressDialog;
    private Circle circle;
    private static int radius_distance;
    private int other_help=0;
    private int helpInRadius=0;
    private boolean connection=false;

    private Double[] latitude;
    private Double[] longitude;
    private String[] title;
    private String[] snippet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pw = new PopupWindow(inflater.inflate(R.layout.publish_need, null, false),size.x-(size.x/100*15),size.y-(size.y/100*30), true);


        progressDialog = new ProgressDialog(this);


        if(isNetworkConnected()) {
            connection = true;
            progressDialog.setMessage("Please wait..");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long notification_count = dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").getChildrenCount()-1;
                    long old_notifications = Integer.parseInt(dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child("old").getValue().toString());
                    if(notification_count>old_notifications){
                        addBadge(notification_count-old_notifications);
                    }
                    radius_distance=Integer.parseInt(dataSnapshot.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").getValue().toString());
                    if (dataSnapshot.hasChild("locations")) {
                        latitude = new Double[(int) dataSnapshot.child("locations").getChildrenCount()];
                        longitude = new Double[(int) dataSnapshot.child("locations").getChildrenCount()];
                        title = new String[(int) dataSnapshot.child("locations").getChildrenCount()];
                        snippet = new String[(int) dataSnapshot.child("locations").getChildrenCount()];
                        targetLocation = new Location("");
                        other_help=0;
                        for (DataSnapshot snapshot : dataSnapshot.child("locations").getChildren()) {
                            if(!snapshot.child("user").getValue().toString().equals(mAuth.getCurrentUser().getEmail().toString())) {
                                title[other_help] = snapshot.getKey();
                                snippet[other_help] = snapshot.child("description").getValue().toString();
                                latitude[other_help] = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                                longitude[other_help] = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                                other_help++;
                            }
                        }



                        first_zoom=true;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Toast.makeText(MapsActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            connection = false;
        }
     /*   latitude[0]=43.73332;
        longitude[0]=23.79992;
        latitude[1]=43.7;
        longitude[1]=23.8;
        title[0]="Title1";
        snippet[0]="Snippet1Snippet1Snippet1Snippet1Snippet1";
        title[1]="Title2";
        snippet[1]="Snippet2";*/


        add = (ImageButton) findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                pw.showAtLocation(findViewById(R.id.maps_view), Gravity.CENTER, 0, 0);
                back_publish = (ImageView) pw.getContentView().findViewById(R.id.back_publish);
                title_publish = (EditText) pw.getContentView().findViewById(R.id.title_help);
                description_publish = (EditText) pw.getContentView().findViewById(R.id.description_help);
                img_help = (EditText) pw.getContentView().findViewById(R.id.help_img);
                date_help = (EditText) pw.getContentView().findViewById(R.id.help_date);
                time_help = (EditText) pw.getContentView().findViewById(R.id.help_time);
                date_help.setKeyListener(null);
                time_help.setKeyListener(null);
                award_help = (EditText) pw.getContentView().findViewById(R.id.award);
                publish_button = (Button) pw.getContentView().findViewById(R.id.publish_button);
                publish_layout = (RelativeLayout) pw.getContentView().findViewById(R.id.publish_layout);

                calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                minDate = calendar.getTime();


                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabelDate();
                    }

                };

                img_help.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseImage();
                    }
                });
                time_help.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog dialog2 = new TimePickerDialog(MapsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                time_help.setText( selectedHour + ":" + selectedMinute);
                            }
                        }, hour, minute, true);//Yes 24 hour time
                        dialog2.setTitle("Select Time");
                        dialog2.show();

                    }
                });
                date_help.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        DatePickerDialog dialog = new DatePickerDialog(MapsActivity.this, android.R.style.Theme_Holo_Light_Dialog ,date,
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        dialog.getDatePicker().setMinDate(minDate.getTime());
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();


                    }
                });



                publish_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboard(view);
                    }
                });

                publish_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    if(isNetworkConnected()) {
                        if (!TextUtils.isEmpty(title_publish.getText().toString())) {
                            if (!TextUtils.isEmpty(date_help.getText().toString())) {
                                if (!TextUtils.isEmpty(time_help.getText().toString())) {
                                    mRef.child("locations").addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.hasChild(title_publish.getText().toString())) {
                                                uploadImage();
                                                mRef.child("locations").child(title_publish.getText().toString()).child("description").setValue(description_publish.getText().toString());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("latitude").setValue(lastLocation.getLatitude());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("longitude").setValue(lastLocation.getLongitude());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("user").setValue(mAuth.getCurrentUser().getEmail().toString());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("date").setValue(date_help.getText().toString());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("time").setValue(time_help.getText().toString());
                                                mRef.child("locations").child(title_publish.getText().toString()).child("award").setValue(award_help.getText().toString());
                                                pw.dismiss();
                                                Toast.makeText(MapsActivity.this, "Published Successfully!", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(MapsActivity.this, "Job with this title already exist!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    Toast.makeText(MapsActivity.this, "You must specify time!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MapsActivity.this, "You must specify date!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "You must specify title!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MapsActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    }
                    }
                });

                back_publish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pw.dismiss();
                    }
                });
            }
        });
        nav();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this,Pre_review_help.class);
                intent.putExtra("title",marker.getTitle());
                startActivity(intent);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
         //   mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }


    }
    public class MarkerHolder {
        public String address;

        public MarkerHolder(String add) {
            address = add;
        }
    }
    private void addMarker(LatLng latLng, String title, String snippet){
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(snippet);

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        Marker marker = mMap.addMarker(markerOptions);
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        helpInRadius=0;
        for (int i = 0; i < other_help; i++) {
            targetLocation.setLatitude(latitude[i]);
            targetLocation.setLongitude(longitude[i]);
            if(targetLocation.distanceTo(lastLocation)<=radius_distance*1000){
                addMarker(new LatLng(latitude[i], longitude[i]), title[i], snippet[i]);
                helpInRadius++;
            }

        }
        infoCount = (Button) findViewById(R.id.info_count);
        if(helpInRadius>0) {
            infoCount.setText(Html.fromHtml(helpInRadius + " jobs found in selected radius<br/><small>Click to review</small>"));
            infoCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MapsActivity.this, "Click", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            if (connection) {
                infoCount.setText("No jobs found in selected radius");
            }else{
                infoCount.setText(Html.fromHtml("No internet connection<br/><small>Click to retry</small>"));
                infoCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNetworkConnected()) {
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });
            }
        }
        if(circle == null) {
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                    .radius(radius_distance*1000)
                    .strokeColor(Color.parseColor("#60e20000"))
                    .fillColor(Color.parseColor("#40f98900"))
                    .strokeWidth(5));
        }else{
            circle.remove();
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                    .radius(radius_distance*1000)
                    .strokeColor(Color.parseColor("#60e20000"))
                    .fillColor(Color.parseColor("#40f98900"))
                    .strokeWidth(5));

        }
        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom((int) (16 - Math.log(radius_distance*3) / Math.log(2)))
                .build();
        if (mMap != null && first_zoom) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            progressDialog.dismiss();
            first_zoom = false;
        }
    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void nav(){
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnve);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

      /*  BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bnve);
        Menu menuNav = navigation.getMenu();
        MenuItem element = menuNav.findItem(R.id.nav_notice);
        String before = element.getTitle().toString();

        String counter = Integer.toString(5);
        String s = " "+counter + "  "+before ;
        SpannableString sColored = new SpannableString( s );

        sColored.setSpan(new BackgroundColorSpan( Color.RED ), 0, counter.length()+2, 0);
        sColored.setSpan(new ForegroundColorSpan( Color.WHITE ), 0, counter.length()+2, 0);


        element.setTitle(sColored);
*/


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        item.setChecked(true);
                        Log.i("Info", "Home");
                        break;
                    case R.id.nav_scan:
                        item.setChecked(true);
                        Intent intent1 = new Intent(MapsActivity.this,Scan.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_notice:
                        item.setChecked(true);
                        Intent intent2 = new Intent(MapsActivity.this,Notifications.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_account:
                        item.setChecked(true);
                        Intent intent3 = new Intent(MapsActivity.this,Profile.class);
                        startActivity(intent3);
                        break;

                }
                return false;
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void updateLabelDate() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        date_help.setText(sdf.format(calendar.getTime()));
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            filePath = data.getData();
            setAttachedIcon(img_help,R.drawable.ic_attach_file_white_24dp);
        }else {
            removeAttachedIcon(img_help,R.drawable.ic_attach_file_white_24dp);
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Locations").child(title_publish.getText().toString()+date_help.getText().toString().replace("/","")+time_help.getText().toString());
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mRef.child("locations").child(title_publish.getText().toString()).child("img").setValue(title_publish.getText().toString()+date_help.getText().toString().replace("/","")+time_help.getText().toString());
                            progressDialog.dismiss();
                            filePath = null;
                            removeAttachedIcon(img_help, R.drawable.ic_attach_file_white_24dp);

                            title_publish.setText("");
                            description_publish.setText("");
                            Toast.makeText(MapsActivity.this, "Help successfully published!", Toast.LENGTH_SHORT).show();
                            pw.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MapsActivity.this, "Failed uploading file!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void addBadge(long count){
        if(count>0) {
            BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            View v = bottomNavigationMenuView.getChildAt(2);
            BottomNavigationItemView itemView = (BottomNavigationItemView) v;

            View badge = LayoutInflater.from(this)
                    .inflate(R.layout.counter, bottomNavigationMenuView, false);
            itemView.addView(badge);
            TextView notificationsbadge = (TextView) findViewById(R.id.notificationsbadge);
            if(count<10) {
                notificationsbadge.setText(count + "");
            }else{
                notificationsbadge.setText("9+");
            }
        }
    }

    private void setAttachedIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.ic_check_circle_24dp, 0);
    }
    private void removeAttachedIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    @Override
    public void onBackPressed()
    {

        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }




}
