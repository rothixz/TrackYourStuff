package com.trackyourstuff.mota.trackyourstuff.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.activities.ClientHomeActivity;

/**
 * Created by mota on 10/29/17.
 */

public class NotificationService extends Service {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DatabaseReference clientsRef;
    private DatabaseReference locationsRef;
    private DatabaseReference ordersRef;

    private String order_id;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        Log.d("Service", mAuth.getCurrentUser().getEmail().split("@")[1]);

        if (mAuth.getCurrentUser().getEmail().split("@")[1].equalsIgnoreCase("transporter.com")) {
            Log.d("Service", "ENTREI AQUI");
            notificationsTransporter();
        }

        final com.google.firebase.database.Query client_address = clientsRef.orderByChild("id").equalTo(currentUser);
        clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        final double client_address_lat = Double.parseDouble(issue.child("address").child("lat").getValue().toString());
                        final double client_address_lng = Double.parseDouble(issue.child("address").child("lng").getValue().toString());

                        final com.google.firebase.database.Query orders = ordersRef.orderByChild("client_id").equalTo(currentUser);
                        orders.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                        if (issue.child("transporter_id").getValue() != null && issue.child("progress").getValue() != null) {
                                            if (Integer.parseInt(issue.child("progress").getValue().toString()) == 30) {
                                                postNotify(30);
                                            }

                                            final String transporter_id = issue.child("transporter_id").getValue().toString();

                                            DatabaseReference locationsRef_transporter = locationsRef.child(transporter_id).child("0");
                                            locationsRef_transporter.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final double transporter_loc_lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                                                    final double transporter_loc_lng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                                                    LatLng transporter_latlng = new LatLng(transporter_loc_lat, transporter_loc_lng);
                                                    LatLng client_latlng = new LatLng(client_address_lat, client_address_lng);
                                                    if (CalculationByDistance(transporter_latlng, client_latlng) <= 1) {
                                                        postNotify(50);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;

        return km;
    }

    private void postNotify(int progress) {
        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder = null;
        Intent resultIntent;
        if (progress == 30) {
            mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.bus_white)
                            .setContentTitle("TrackYourStuff")
                            .setContentText("Your delivery is on the way!");
            resultIntent = new Intent(this, ClientHomeActivity.class);
        } else if (progress == 50) {
            mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.bus_white)
                            .setContentTitle("TrackYourStuff")
                            .setContentText("Your delivery is nearby. Heads up!");
            resultIntent = new Intent(this, ClientHomeActivity.class);
        } else if (progress == 60) {
            mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.bus_white)
                            .setContentTitle("TrackYourStuff")
                            .setContentText("There are new orders available!");
            resultIntent = new Intent(this, ClientHomeActivity.class);
        } else {
                return;
        }



// Creates an explicit intent for an Activity in your app


// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ClientHomeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to NotificationManager.cancel().
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void notificationsTransporter() {
        ordersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postNotify(60);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
