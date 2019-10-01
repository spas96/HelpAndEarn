package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.w3c.dom.Text;

/**
 * Created by Spas on 9/15/2018.
 */

public class QR_code extends Activity {

    private ImageView qrcode;
    private ImageView back;


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private MultiFormatWriter multiFormatWriter;
    private String[] split;
    private String name;
    private String date;
    private String email;
    private TextView infotext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.qr_layout);

        qrcode = (ImageView) findViewById(R.id.qr_code);
        infotext = (TextView) findViewById(R.id.info_text);
        back = (ImageView) findViewById(R.id.back_qr);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            split = extras.getString("info").split("#");
            name = split[0];
            date = split[1];
            email = split[2];
        }
        Toast.makeText(QR_code.this, name+"#"+date+"#"+email, Toast.LENGTH_SHORT).show();

        multiFormatWriter = new MultiFormatWriter();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(name+"#"+date+"#"+mAuth.getCurrentUser().getEmail(), BarcodeFormat.QR_CODE,200,200   );
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);


                    int [] allpixels = new int [bitmap.getHeight() * bitmap.getWidth()];

                    bitmap.getPixels(allpixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

                    for(int i = 0; i < allpixels.length; i++){
                        if(allpixels[i] == Color.WHITE){
                            allpixels[i] = Color.TRANSPARENT;
                        }
                    }

                    bitmap.setPixels(allpixels,0,bitmap.getWidth(),0, 0, bitmap.getWidth(),bitmap.getHeight());


                    qrcode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRef.child(mAuth.getCurrentUser().getEmail().replace(".",",")).child("approved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("1")){
                    mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").child(name).setValue(email);
                    mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_future").child(name).removeValue();
                    infotext.setText("Show this barcode to the person when you finish with your job");
                }else{
                    if (dataSnapshot.getValue().toString().equals("2")){
                        mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_past").child(name).setValue(email);
                        mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("jobs_current").child(name).removeValue();
                        mRef.child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("approved").setValue(0);
                        infotext.setText("Show this barcode to the person when you arrive");
                        Intent intent = new Intent(QR_code.this,Profile.class);
                        finish();
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QR_code.this.finish();
            }
        });
    }




}
