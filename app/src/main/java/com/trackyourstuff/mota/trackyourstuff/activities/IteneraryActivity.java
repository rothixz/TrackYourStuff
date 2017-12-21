package com.trackyourstuff.mota.trackyourstuff.activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Transporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IteneraryActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    GeoApiContext directionsAPI;
    private DatabaseReference ordersRef;
    private DatabaseReference clientsRef;
    private DatabaseReference transportersRef;
    private Transporter my_transporter;
    private ArrayList<String> order_ids;
    private int flag_addresses = 0;
    Map<String, com.google.maps.model.LatLng> order_address = new HashMap<>();
    ArrayList<com.google.maps.model.LatLng> addresses = new ArrayList<com.google.maps.model.LatLng>();
    public static final int MY_PERMISSIONS_FINE_LOCATION = 99;
    private FusedLocationProviderClient mFusedLocationClient;
    private com.google.maps.model.LatLng myloc;
    private Toolbar toolbar;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itenerary);

        // Getting client
        my_transporter = getIntent().getExtras().getParcelable("Transporter");

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

        // Transporters reference
        transportersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/transporters");

        directionsAPI = getGeoContext();

        // Location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    }

    private void getOrder_ids() {
        final com.google.firebase.database.Query user_transporter = transportersRef.orderByChild("id").equalTo(my_transporter.getId());

        user_transporter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        order_ids = (ArrayList<String>) issue.child("orders").getValue();
                        Iterator itr = order_ids.iterator();
                        while(itr.hasNext()){
                            String order_id = (String) itr.next();
                            order_address.put(order_id, null);
                            getOrderAddress(order_id, order_ids.size());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getOrderAddress(final String order_id, final int orders_no) {
        final com.google.firebase.database.Query orders_transporter = ordersRef.orderByChild("id").equalTo(order_id);
        orders_transporter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        final String client_id = issue.child("client_id").getValue().toString();
                        Log.d("Itenerary", "getOrderAddress de " + order_id + " com client ID: " + client_id);
                        final com.google.firebase.database.Query client = clientsRef.orderByChild("id").equalTo(client_id);

                        client.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                        double client_address_lat = (double) issue.child("address").child("lat").getValue();
                                        double client_address_lng = (double) issue.child("address").child("lng").getValue();
                                        Log.d("Itenerary", "Ja entrei num client :" + issue.child("id").getValue().toString() + " e vou dar add das coordenada: " + client_address_lat +", " + client_address_lng );
                                        com.google.maps.model.LatLng client_address_latlng = new com.google.maps.model.LatLng(client_address_lat, client_address_lng);
                                        order_address.put(order_id, client_address_latlng);
                                        Log.d("Itenerary", "adicionei á order_address isto: " + order_id + "-" + client_address_latlng.toString() );
                                        flag_addresses++;
                                        if(flag_addresses == orders_no){
                                            Log.d("Itenerary", "Sou o last e tenho a order_address assim: " + order_address);
                                            for(com.google.maps.model.LatLng element: order_address.values()){
                                                addresses.add(element);
                                            }

                                            try {
                                                getDirections(myloc, addresses);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            } catch (ApiException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("Itenerary", "datasnapshot de client não existe");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    Log.d("Itenerary", "Snapshot doesn't exist");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void getDirections(com.google.maps.model.LatLng origin, ArrayList<com.google.maps.model.LatLng> waypoints) throws InterruptedException, ApiException, IOException {
        com.google.maps.model.LatLng destination = waypoints.get(0);
        String final_waypoints = "";
        if(!waypoints.isEmpty()){
            for(com.google.maps.model.LatLng elem : waypoints){
                final_waypoints = final_waypoints + elem.lat + "," + elem.lng + "|";
            }

            final_waypoints = final_waypoints.substring(0, final_waypoints.length() -1);
        }

        DirectionsResult result = DirectionsApi.newRequest(directionsAPI)
                .mode(TravelMode.DRIVING)
                .origin(origin)
                .waypoints(final_waypoints)
                .optimizeWaypoints(true)
                .destination(origin)
                .await();

        addMarkersToMap(result);
        Log.d("Itenerary", getEndLocationTitle(result));
        addPolyline(result);
    }

    private void addMarkersToMap(DirectionsResult results) {
        Log.d("Itenerary", ""+ results.routes[0].legs[0].startLocation.lat);
        if(mMap == null){
            Log.d("Itenerary", "mapa ta null");
        }
        int i=0;
        for(DirectionsLeg elem: results.routes[0].legs){
            Log.d("Itenerary", "Vou adicioanr este marker" + elem.startLocation.toString());
            i++;
            if(i == 1){
                mMap.addMarker(new MarkerOptions().position(new LatLng(elem.startLocation.lat,elem.startLocation.lng)).title("My location: " + elem.startLocation.toString()));
                mMap.addMarker(new MarkerOptions().position(new LatLng(elem.endLocation.lat,elem.endLocation.lng)).title("Deliver here : " + elem.endLocation.toString()).snippet(getEndLocationTitle(results)));
            } else if(i == results.routes[0].legs.length){
                mMap.addMarker(new MarkerOptions().position(new LatLng(elem.endLocation.lat,elem.endLocation.lng)).title("My final Destination: " + elem.endLocation.toString()));
            } else{
                mMap.addMarker(new MarkerOptions().position(new LatLng(elem.endLocation.lat,elem.endLocation.lng)).title("Deliver here : " + elem.endLocation.toString()).snippet(getEndLocationTitle(results)));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myloc.lat, myloc.lng)));
    }

    private String getEndLocationTitle (DirectionsResult results){
        return "Time :"+ results.routes[0].legs[0] .duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void addPolyline(DirectionsResult results) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMap != null){
            checkLocationPermission();

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                myloc = new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude());
                                getOrder_ids();
                            }
                        }
                    });


        }
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(IteneraryActivity.this,
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

