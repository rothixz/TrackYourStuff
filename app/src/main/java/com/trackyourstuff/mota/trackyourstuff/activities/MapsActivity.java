package com.trackyourstuff.mota.trackyourstuff.activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;

import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Client;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    private Toolbar toolbar;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mSearchedLocationReference;
    private DatabaseReference ordersRef;
    private DatabaseReference clientsRef;
    public static final int MY_PERMISSIONS_FINE_LOCATION = 99;
    Marker marker;
    private Client my_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Getting client
        my_client = getIntent().getExtras().getParcelable("Client");

        // Orders reference
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders");

        // Clients reference
        clientsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/clients");

        // Listener
        checkTrackingStatus();

        Log.d("MAPSACTIVITY", "passed checkTrackingStatus");

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Return button listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void checkTrackingStatus() {
        Log.d("MAPSACTIVITY", "checkTrackingStatus");
        final com.google.firebase.database.Query user_client = clientsRef.orderByChild("id").equalTo(my_client.getId());

        user_client.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        Log.d("MAPSACTIVITY", "exists client");
                        final com.google.firebase.database.Query client_orders = ordersRef.orderByChild("client_id").equalTo(my_client.getId());
                        client_orders.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d("MAPSACTIVITY", "exists order");
                                    for (DataSnapshot issue2 : dataSnapshot.getChildren()) {
                                        if(issue2.child("status").getValue().toString().equalsIgnoreCase("delivering")){
                                            trackOrder(issue2.child("transporter_id").getValue().toString());
                                        } else {
                                            Toast.makeText(getBaseContext(), "Order not ready to track!", Toast.LENGTH_SHORT);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void trackOrder(String transporter_id) {
        Log.d("MAPSACTIVITY", "trackOrder");
        mSearchedLocationReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("raw-locations/" + transporter_id + "/0");

        mSearchedLocationReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng otherloc = new LatLng((double)dataSnapshot.child("lat").getValue(), (double) dataSnapshot.child("lng").getValue());
                marker.setPosition(otherloc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

            }
        });
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
        LatLng loc = new LatLng(2.0,2.0);
        marker = mMap.addMarker(new MarkerOptions().position(loc).title("Transporter location marker"));
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("bus_white",100,100)));

        checkLocationPermission();

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(myloc).title("My location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                        }
                    }
                });
    }


    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                return;
            }
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Override
    public void onClick(View view) {

    }
}