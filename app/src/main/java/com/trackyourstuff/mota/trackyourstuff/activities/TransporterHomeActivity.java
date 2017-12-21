package com.trackyourstuff.mota.trackyourstuff.activities;

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
import android.widget.TextView;

import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Transporter;
import com.trackyourstuff.mota.trackyourstuff.services.NotificationService;

public class TransporterHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Transporter user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transporter);

        // Getting transporter
        user = getIntent().getExtras().getParcelable("Transporter");

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

    @Override
    protected void onStart() {
        super.onStart();

        // Start notifications service
        Intent notificationServiceIntent = new Intent(this, NotificationService.class);
        startService(notificationServiceIntent);
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

        if (id == R.id.nav_manage) {
            Intent tracker = new Intent(this, TrackerActivity.class);
            tracker.putExtra("Transporter", user);
            startActivity(tracker);
        } else if (id == R.id.nav_camera) {
            Intent orders = new Intent(this, OrdersActivity.class);
            orders.putExtra("Transporter", user);
            startActivity(orders);
        } else if (id == R.id.nav_gallery) {
            Intent tracker = new Intent(this, QRCodeActivity.class);
            tracker.putExtra("Transporter", user);
            startActivity(tracker);
        } else if (id == R.id.nfc_transporter) {
            Intent nfc_transporter = new Intent(this, NFCDisplayActivity.class);
            nfc_transporter.putExtra("Transporter", user);
            startActivity(nfc_transporter);
        } else if (id == R.id.itenerary) {
            Intent itenerary = new Intent(this, IteneraryActivity.class);
            itenerary.putExtra("Transporter", user);
            startActivity(itenerary);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
