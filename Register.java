package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends Activity {

    private EditText email;
    private EditText password;
    private EditText mword;
    private EditText name;
    private EditText lname;
    private EditText bdate;
    private TextView error;
    private Button register;
    private TextView login;
    private RelativeLayout reg_layout;
    private ProgressDialog progressDialog;
    private Calendar calendar;
    private Date maxDate;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        mword = (EditText) findViewById(R.id.mword);
        name = (EditText) findViewById(R.id.name);
        lname = (EditText) findViewById(R.id.lname);
        bdate = (EditText) findViewById(R.id.bdate);
        error = (TextView) findViewById(R.id.error);
        bdate.setKeyListener(null);
        register = (Button) findViewById(R.id.register_button);
        login = (TextView) findViewById(R.id.login);
        login.setText(Html.fromHtml("Already registered? <b> Sign in here! </b>"));
        reg_layout = (RelativeLayout) findViewById(R.id.reg_layout);
        progressDialog = new ProgressDialog(this);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -16);
        maxDate = calendar.getTime();





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
                DatePickerDialog dialog = new DatePickerDialog(Register.this, android.R.style.Theme_Holo_Light_Dialog ,date,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(maxDate.getTime());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


            }
        });



        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        register.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(checkFields()){
                    registerUser();
                }
            }
        });

        login.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this,Login.class);
                startActivity(intent);
            }
        });

        reg_layout.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

    }

    private void setWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.wrong, 0);
    }
    private void removeWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    private boolean checkFields(){
        if(!ismWordValid(mword.getText().toString())) {
            setWrongIcon(mword, R.drawable.ic_security_white_24dp);
            error.setText("Memorable word must be a single word!");
            if (!isEmailValid(email.getText().toString())) {
                setWrongIcon(email, R.drawable.ic_email_white_24dp);
                if (!isPasswordValid(password.getText().toString())) {
                    setWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                } else {
                    removeWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                }
            } else {
                removeWrongIcon(email, R.drawable.ic_email_white_24dp);
                if (!isPasswordValid(password.getText().toString())) {
                    setWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                } else {
                    removeWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                }
            }
        }else{
            removeWrongIcon(mword,R.drawable.ic_security_white_24dp);
            if (!isEmailValid(email.getText().toString())) {
                setWrongIcon(email, R.drawable.ic_email_white_24dp);
                error.setText("Enter valid email!");
                if (!isPasswordValid(password.getText().toString())) {
                    setWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                } else {
                    removeWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                }
            } else {
                removeWrongIcon(email, R.drawable.ic_email_white_24dp);
                if (!isPasswordValid(password.getText().toString())) {
                    setWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    error.setText("Password must contain letters and numbers!");
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    }
                } else {
                    removeWrongIcon(password, R.drawable.ic_lock_white_24dp);
                    if (!isNameValid(name.getText().toString())) {
                        setWrongIcon(name, R.drawable.ic_person_white_24dp);
                        error.setText("Invalid first name!");
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        }
                    } else {
                        removeWrongIcon(name, R.drawable.ic_person_white_24dp);
                        if (!isNameValid(lname.getText().toString())) {
                            setWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            error.setText("Invalid last name!");
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return false;
                            }
                        } else {
                            removeWrongIcon(lname, R.drawable.ic_person_white_24dp);
                            if (!isDateValid(bdate.getText().toString())) {
                                setWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                error.setText("Format: dd/mm/yyyy");
                                return false;
                            } else {
                                removeWrongIcon(bdate, R.drawable.ic_date_range_white_24dp);
                                return true;
                            }
                        }
                    }
                }
            }

        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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

    public static boolean isNameValid(String name) {
        String expression = "^[A-Z][-a-zA-Z]+$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static boolean isDateValid(String name) {
        String expression = "^([0-2][0-9]||3[0-1])/(0[0-9]||1[0-2])/([0-9][0-9])?[0-9][0-9]$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        bdate.setText(sdf.format(calendar.getTime()));
    }

    private void registerUser(){


        progressDialog.setMessage("Registration in progress! Please wait!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //user is succesfully registered and logged in
                            setDetailsDatabase();
                            Intent intent = new Intent(Register.this,MapsActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(Register.this, "Registration was unsuccessful! Please try again!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

    }

    private void setDetailsDatabase(){
        //Save details inserted from the user in database
        mRef.child(email.getText().toString().replace(".",",")).child("account").child("memorable_word").setValue(mword.getText().toString());
        mRef.child(email.getText().toString().replace(".",",")).child("information").child("name").setValue(name.getText().toString());
        mRef.child(email.getText().toString().replace(".",",")).child("information").child("last_name").setValue(lname.getText().toString());
        mRef.child(email.getText().toString().replace(".",",")).child("information").child("birth_date").setValue(bdate.getText().toString());
        mRef.child(email.getText().toString().replace(".",",")).child("information").child("info").setValue("");
        mRef.child(email.getText().toString().replace(".",",")).child("information").child("rating").setValue(0);
        //Save external default values for the current user
        //preventing crashes
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("account").child("distance").setValue(30);
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("notifications").child("old").setValue(0);
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("approved").setValue(0);
    }


}
