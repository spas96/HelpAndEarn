package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Files extends Activity {

    private ListView myList;
    private CustomAdapter customAdapter;
    private String[] FILES;
    private RelativeLayout files_layout;
    private ImageView back;
    private EditText attach;
    private EditText file_name;
    private Uri filePath;
    private FirebaseAuth mAuth;
    private Button upload;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private int count_files = 0;
    private boolean name_exist = false;
    private String del;
    private boolean conn = false;

    private ProgressDialog progressDialog;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files);

        myList = (ListView) findViewById(R.id.files_list);
        customAdapter = new CustomAdapter();
        files_layout = (RelativeLayout) findViewById(R.id.files_layout);
        back = (ImageView) findViewById(R.id.back_files);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        attach = (EditText) findViewById(R.id.upload);
        file_name = (EditText) findViewById(R.id.file_name);
        upload = (Button) findViewById(R.id.upload_file);
        FILES = new String[5];


        progressDialog = new ProgressDialog(this);


        getFiles();





        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Files.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Do you want to delete "+ FILES[i] +"?");
                del = "" + FILES[i];
                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference ref = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Documents").child(mAuth.getCurrentUser().getEmail()).child(FILES[i]);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        try{
                            ref.delete();
                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("files").child(del).removeValue();
                            getFiles();
                    /*       mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if(snapshot.getKey().equals(del)){
                                            ref.delete();
                                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("files").child(snapshot.getKey()).removeValue();
                                            getFiles();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });*/
                            Toast.makeText(Files.this, "File deleted!", Toast.LENGTH_SHORT).show();

                        }
                        catch (Exception e){
                            Toast.makeText(Files.this, "Can't delete file!", Toast.LENGTH_SHORT).show();
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

                //Toast.makeText(Files.this,FILES[i],Toast.LENGTH_SHORT).show();
            }

        });

        files_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Files.this.finish();
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count_files<5) {
                    mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(file_name.getText().toString())){
                                setWrongIcon(file_name,R.drawable.ic_mode_edit_white_24dp);
                                if(file_name.getText().toString().equals("")){
                                    Toast.makeText(Files.this, "Please specify file name!", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(Files.this, "File with this name already exist!", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                uploadImage();
                                removeWrongIcon(file_name,R.drawable.ic_mode_edit_white_24dp);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Files.this, "You reach max number of files! Delete some files before uploading new!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return count_files;

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
            view = getLayoutInflater().inflate(R.layout.list, null);

            TextView file = (TextView) view.findViewById(R.id.item_file);
            file.setText(FILES[i]);

            return view;
        }
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
            setAttachedIcon(attach,R.drawable.ic_attach_file_white_24dp);
        }else {
            removeAttachedIcon(attach,R.drawable.ic_attach_file_white_24dp);
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReferenceFromUrl("gs://helpandearn-727b2.appspot.com/Documents").child(mAuth.getCurrentUser().getEmail()).child(file_name.getText().toString());
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".", ",")).child("files").child(file_name.getText().toString()).setValue(".");
                            progressDialog.dismiss();
                            Toast.makeText(Files.this, "Successfully uploaded!", Toast.LENGTH_SHORT).show();
                            file_name.setText("");
                            filePath = null;
                            hideKeyboard(file_name.getRootView());
                            removeAttachedIcon(attach, R.drawable.ic_attach_file_white_24dp);
                            getFiles();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Files.this, "Failed uploading file!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            Toast.makeText(Files.this, "Please choose file!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFiles(){
        progressDialog.setMessage("Loading files..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    conn = true;
                } else {
                    conn = false;

                    Toast.makeText(Files.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        mRef.child(mAuth.getCurrentUser().getEmail().toString().replace(".",",")).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(conn) {
                    Arrays.fill(FILES, null);
                    count_files = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FILES[count_files] = snapshot.getKey().toString();
                        count_files++;
                    }
                    myList.setAdapter(customAdapter);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.wrong, 0);
    }
    private void removeWrongIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    private void setAttachedIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, R.drawable.ic_check_circle_24dp, 0);
    }
    private void removeAttachedIcon(EditText field,int left){
        field.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }



}
