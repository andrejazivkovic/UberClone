package com.example.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    public void getStarted(View view) {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch sw = (Switch) findViewById(R.id.switch1);
        Intent intent;
        String tip = "rider";
        if (!sw.isChecked()) {
            intent = new Intent(getApplicationContext(), RidersActivity.class);

        } else {
            tip = "driver";
            intent = new Intent(getApplicationContext(), DriverMainActivity.class);
        }
                ParseUser user = ParseUser.getCurrentUser();
                user.put("riderOrDriver", tip);
                user.saveInBackground();


        Log.i("Info", Objects.requireNonNull(ParseUser.getCurrentUser().get("riderOrDriver")).toString());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        Log.i("Info", "Annonimys Login Success");
                    } else {
                        Log.i("Info", "Annonimys Login Failed");
                    }
                }
            });
        }else {

            if(ParseUser.getCurrentUser().get("riderOrDriver") != null){
                if(Objects.equals(ParseUser.getCurrentUser().get("riderOrDriver"), "rider")){
                        Intent intent = new Intent(getApplicationContext(), RidersActivity.class);
                }else {
                    Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
                }

                Log.i("Info", Objects.requireNonNull(ParseUser.getCurrentUser().get("riderOrDriver")).toString());

            }
        }


        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
