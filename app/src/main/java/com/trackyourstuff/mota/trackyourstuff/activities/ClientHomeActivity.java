package com.trackyourstuff.mota.trackyourstuff.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Client;
import com.trackyourstuff.mota.trackyourstuff.services.NotificationService;

public class ClientHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ProgressBar mProgressBar;
    private Client user;
    private DatabaseReference productsRef;
    private DatabaseReference ordersRef;
    private StorageReference storageRef;
    private DatabaseReference locationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_client);

        // Getting client
        user = getIntent().getExtras().getParcelable("Client");

        // Setting toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get storage ref
        storageRef = FirebaseStorage.
                getInstance().
                getReference();


        // Get orders ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        // Get products ref
        productsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("products/pizzas/");

        // Get locations ref
        locationsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("raw-locations");

        // Orders progress
        listenForProgress();
    }

    public void setProgressBar(int progress) {
        mProgressBar = (ProgressBar) findViewById(R.id.determinateBar);
        TextView mETA = (TextView) findViewById(R.id.eta);
        mETA.setText("Delivering ETA: " + (int) ((100 - progress) * 0.42) + " minutes!");
        mProgressBar.setProgress(progress);
    }

    // Back button press close drawer first
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Set username and name on drawer menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView Name = (TextView) findViewById(R.id.name);
        TextView Username = (TextView) findViewById(R.id.username);

        Name.setText(user.getName());
        Username.setText(user.getUsername());
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Drawer menu item handle
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.catalog) {
            Intent catalog = new Intent(this, CatalogActivity.class);
            catalog.putExtra("Client", user);
            startActivity(catalog);
        } else if (id == R.id.maps) {
            Intent maps = new Intent(this, MapsActivity.class);
            maps.putExtra("Client", user);
            startActivity(maps);
        } else if (id == R.id.nfc_client) {
            Intent nfc_client = new Intent(this, NFCActivity.class);
            nfc_client.putExtra("Client", user);
            startActivity(nfc_client);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start notifications service
        Intent notificationServiceIntent = new Intent(this, NotificationService.class);
        startService(notificationServiceIntent);
    }

    // Checks if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Updates UI based on the progress of an order
    private void listenForProgress() {
        final com.google.firebase.database.Query orders = ordersRef.orderByChild("client_id").equalTo(user.getId());

        orders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("progress").getValue() != null) {
                            int progress = Integer.parseInt(issue.child("progress").getValue().toString());
                            setProgressBar(progress);
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