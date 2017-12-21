package com.trackyourstuff.mota.trackyourstuff.services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.trackyourstuff.mota.trackyourstuff.IProgressService;
import com.trackyourstuff.mota.trackyourstuff.R;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProgressService extends Service {
    GeoApiContext directionsAPI;
    private double progress = 0;
    private int i = 0;
    private CountDownTimer mCountDownTimer;
    private DatabaseReference ordersRef;
    private DatabaseReference clientsRef;
    private DatabaseReference locationsRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String order_id;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        directionsAPI = getGeoContext();
        startProgressMake();
        return START_STICKY;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void startProgressWay() {
        Log.d("Service", "startProgressWay");
        final String currentUser = mAuth.getCurrentUser().getUid();

        // Get orders Ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        // Get clients Ref
        clientsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/clients");

        // Get locations Ref
        locationsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("raw-locations");


        final com.google.firebase.database.Query orders = ordersRef.orderByChild("client_id").equalTo(currentUser);

        orders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("transporter_id").getValue() != null) {
                            Log.d("Service", "orders");
                            order_id = issue.child("id").getValue().toString();
                            final String transporter_id = issue.child("transporter_id").getValue().toString();

                            final com.google.firebase.database.Query address = clientsRef.orderByChild("id").equalTo(currentUser);
                            address.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                            Log.d("Service", "address");
                                            final double client_address_lat = Double.parseDouble(issue.child("address").child("lat").getValue().toString());
                                            final double client_address_lng = Double.parseDouble(issue.child("address").child("lng").getValue().toString());

                                            DatabaseReference transporter_loc = locationsRef.child(transporter_id + "/0");
                                            transporter_loc.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final double transporter_loc_lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                                                    final double transporter_loc_lng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                                                    DirectionsResult result = null;
                                                    Log.d("Service", "transporter_loc");
                                                    try {
                                                        result = DirectionsApi.newRequest(directionsAPI)
                                                                .mode(TravelMode.DRIVING)
                                                                .origin(new com.google.maps.model.LatLng(transporter_loc_lat, transporter_loc_lng))
                                                                .destination(new com.google.maps.model.LatLng(client_address_lat, client_address_lng))
                                                                .await();
                                                    } catch (ApiException e) {
                                                        e.printStackTrace();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    if (result != null) {
                                                        i = 0;
                                                        final long duration = getDuration(result);
                                                        final double tick_no = ((duration*1000)/100);
                                                        final double progress_int = 70 / tick_no;
                                                        mCountDownTimer = new CountDownTimer(duration*1000, 100) {
                                                            @Override
                                                            public void onTick(long millisUntilFinished) {
                                                                progress += progress_int;
                                                                Log.d("Service", "Tick of Progress WAY" + i + " tempo para acabra " +  millisUntilFinished + " duration = " + duration + "" +
                                                                        "progress em double " + progress
                                                                        + "  progress em int " + (int) progress
                                                                        + " progress por tick " + progress_int
                                                                        );

                                                                updateProgress((int) progress);

                                                            }

                                                            @Override
                                                            public void onFinish() {
                                                                progress = 100;
                                                                updateProgress((int) progress);
                                                            }
                                                        };

                                                        mCountDownTimer.start();
                                                    } else {
                                                        Log.d("Service", "O API retornou null!");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

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
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mStub;
    }

    private void startProgressMake() {
        i = 0;

        mCountDownTimer=new CountDownTimer(10000,500) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Service", "Tick of Progress Make"+ i + millisUntilFinished);
                i++;
                progress = i*30/(10000/500);
                updateProgress((int) progress);

            }

            @Override
            public void onFinish() {
                i++;
                progress = 30;
                updateProgress((int) progress);
                startProgressWay();
            }
        };

        mCountDownTimer.start();
    }

    private void updateProgress(final int progress){
        // Get orders Ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        final String currentUser = mAuth.getCurrentUser().getUid();
        final com.google.firebase.database.Query orders = ordersRef.orderByChild("client_id").equalTo(currentUser);

        orders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("transporter_id").getValue() != null) {
                            order_id = issue.child("id").getValue().toString();
                            Log.d("Service", "UPDATE PARA " + progress);
                            issue.child("progress").getRef().setValue(progress);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private long getDuration (DirectionsResult results){
        return results.routes[0].legs[0] .duration.inSeconds;
    }

    IProgressService.Stub mStub = new  IProgressService.Stub() {


        @Override
        public long getProgress() throws RemoteException {
            long a = 0;
            return a;
        }

        @Override
        public void startProgress() throws RemoteException {
        }
    };
}