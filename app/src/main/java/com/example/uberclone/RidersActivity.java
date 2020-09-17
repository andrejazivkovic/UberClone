package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class RidersActivity extends FragmentActivity implements OnMapReadyCallback {
    Handler handler = new Handler();
    TextView textView;
    public void checkForUpdates(){


        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0 ){
                        ParseQuery<ParseUser> query1 = ParseUser.getQuery();
                        query1.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                        textView.setText("Your driver is on the way!");
                        button.setVisibility(View.INVISIBLE);
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkForUpdates();
                        }
                    }, 2000);
                }
            }
        });



    }

    public void updateMap(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

    }

    private GoogleMap mMap;

    LocationManager locationManager;

    LocationListener locationListener;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location laastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    assert laastKnownLocation != null;
                    updateMap(laastKnownLocation);
                }

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riders);
        textView = (TextView) findViewById(R.id.text_view_id);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            Location laastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(laastKnownLocation !=null){
                updateMap(laastKnownLocation);
            }

        }
    }
    Boolean flag = false;
    Button button;
    @SuppressLint("SetTextI18n")
    public void UberCall(View view){
        button = (Button) findViewById(R.id.CallUber);
        if(flag){
            flag=false;
            button.setText("Call Uber");
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    for(ParseObject o : objects){
                        o.deleteInBackground();
                    }
                }
            });


        }else {
            flag=true;
            button.setText("Cancel Uber");
            checkForUpdates();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkForUpdates();
                }
            }, 2000);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location laastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (laastKnownLocation != null) {
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(laastKnownLocation.getLatitude(), laastKnownLocation.getLongitude());
                    request.put("location", parseGeoPoint);
                    request.saveInBackground();
                } else {
                    Toast.makeText(getApplicationContext(), "Cant find location. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

        }


        }
        public void LogOut(View view){

        Button logout = (Button) findViewById(R.id.logout);

        ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }



    }
