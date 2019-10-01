package com.example.spas.HelpAndEarn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;


public class Agreement extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.agreement);


        Button ok = (Button) findViewById(R.id.dialog_ok);
        Button cancel = (Button) findViewById(R.id.dialog_cancel);


        ok.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Agreement.this,MapsActivity.class);
                startActivity(intent);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                finish();
                System.exit(0);

            }
        });
    }

}
