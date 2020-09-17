package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverMainActivity extends AppCompatActivity {

    LocationManager locationManager;

    LocationListener locationListener;

    ListView listView;

    ArrayList<String> requests = new ArrayList<>();

    ArrayList<Double> requestLatitudes = new ArrayList<>();
    ArrayList<Double> requsetLongitudes = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>();


    ArrayAdapter<String> arrayAdapter;

    public void updateListView(Location location) {
        if (location != null) {


            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            final ParseGeoPoint geoPointLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            query.whereNear("location", geoPointLocation);

            query.whereDoesNotExist("driverUsername");
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        requests.clear();
                        requsetLongitudes.clear();
                        requestLatitudes.clear();
                        if(objects.size() > 0){
                            for(ParseObject o : objects) {
                                ParseGeoPoint geoPoint = (ParseGeoPoint) o.get("location");
                                if (geoPoint != null) {
                                    Double distance = geoPointLocation.distanceInKilometersTo(geoPoint);
                                    Double distance1dp = (double) Math.round(distance * 10) / 10;
                                    requests.add(distance1dp.toString() + "km");
                                    requestLatitudes.add(geoPoint.getLatitude());
                                    requsetLongitudes.add(geoPoint.getLongitude());
                                    usernames.add(o.getString("username"));

                                }
                            }
                            arrayAdapter.notifyDataSetChanged();

                        }
                    }
                }
            });

        }else {
            requests.add("No acctive requests nearby");
            arrayAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location laastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    updateListView(laastKnownLocation);
                }

            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        listView = (ListView) findViewById(R.id.requestListView);

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, requests);

        requests.clear();

        requests.add("Getting nerby requests....");

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location laastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (requestLatitudes.size() > i && requsetLongitudes.size() > i && laastKnownLocation != null && usernames.size() > i) {
                        Intent intent = new Intent(getApplicationContext(), DriverActivityMain.class);
                        intent.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent.putExtra("requestLongitude", requsetLongitudes.get(i));
                        intent.putExtra("driverLatitude", laastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude", laastKnownLocation.getLongitude());
                        intent.putExtra("username", usernames.get(i));

                        startActivity(intent);

                    }
                }

            }
        });



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);
                ParseUser user = ParseUser.getCurrentUser();
                ParseGeoPoint geogeo = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                user.put("location", geogeo);
                user.saveInBackground();

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
                updateListView(laastKnownLocation);
            }

        }
    }

    }
