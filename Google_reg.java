package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Google_reg extends Activity {

    private Button save;
    private TextView signin;
    private RelativeLayout greg_layout;
    private EditText name;
    private EditText lname;
    private EditText bdate;
    private TextView error;
    private Calendar calendar;
    private Date maxDate;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_register);


        save = (Button) findViewById(R.id.save_details);
        signin = (TextView) findViewById(R.id.login_greg);
        greg_layout = (RelativeLayout) findViewById(R.id.greg_layout);
        name = (EditText) findViewById(R.id.gname);
        lname = (EditText) findViewById(R.id.glname);
        bdate = (EditText) findViewById(R.id.gbdate);
        error = (TextView) findViewById(R.id.gerror);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -16);
        maxDate = calendar.getTime();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();


        greg_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        bdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(Google_reg.this, android.R.style.Theme_Holo_Light_Dialog ,date,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(maxDate.getTime());

                dialog.show();


            }
        });

        save.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(isNameValid(name.getText().toString())){
                    removeWrongIcon(name,R.drawable.ic_person_white_24dp);
                    if(isNameValid(lname.getText().toString())){
                        removeWrongIcon(lname,R.drawable.ic_person_white_24dp);
                        if(isDateValid(bdate.getText().toString())){
                            removeWrongIcon(bdate,R.drawable.ic_date_range_white_24dp);
                            setDetailsDatabase();
                            Toast.makeText(Google_reg.this, "Your details are set successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Google_reg.this,MapsActivity.class);
                            startActivity(intent);
                        }else{
                            setWrongIcon(bdate,R.drawable.ic_date_range_white_24dp);
                            error.setText("Format: dd/mm/yyyy");
                        }
                    }else{
                        setWrongIcon(lname,R.drawable.ic_person_white_24dp);
                        error.setText("Invalid last name");
                    }
                }else{
                    setWrongIcon(name,R.drawable.ic_person_white_24dp);
                    error.setText("Invalid first name");
                }

            }
        });
        signin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(Google_reg.this,Login.class);
                startActivity(intent);

            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        bdate.setText(sdf.format(calendar.getTime()));
    }

    private void setWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.wrong, 0);
    }
    private void removeWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    public static boolean isNameValid(String name) {
        String expression = "^[A-Z][-a-zA-Z]+$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static boolean isDateValid(String date) {
        String expression = "^([0-2][0-9]||3[0-1])/(0[0-9]||1[0-2])/([0-9][0-9])?[0-9][0-9]$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    private void setDetailsDatabase(){
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("name").setValue(name.getText().toString());
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("last_name").setValue(lname.getText().toString());
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("birth_date").setValue(bdate.getText().toString());
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("info").setValue("");
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("information").child("rating").setValue(0);
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").setValue(30);
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child("old").setValue(0);
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("approved").setValue(0);
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
