/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trackyourstuff.mota.trackyourstuff.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Transporter;
import com.trackyourstuff.mota.trackyourstuff.services.TrackerService;

import static com.trackyourstuff.mota.trackyourstuff.activities.MapsActivity.MY_PERMISSIONS_FINE_LOCATION;

public class TrackerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private static final int PERMISSIONS_REQUEST = 1;
    private static String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SharedPreferences mPrefs;

    private Button mStartButton;
    private EditText mTransportIdEditText;
    private Snackbar mSnackbarPermissions;
    private Snackbar mSnackbarGps;

    private Transporter my_transporter;

    /**
     * Configures UI elements, and starts validation if inputs have previously been entered.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        // Getting transportereliverin
        my_transporter = getIntent().getExtras().getParcelable("Transporter");

        // Views
        mStartButton = (Button) findViewById(R.id.button_start);
        mTransportIdEditText = (EditText) findViewById(R.id.transport_id);

        // Listeners
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkInputFields();
            }
        });

        // Shared Preferences
        mPrefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        String transportID = mPrefs.getString(getString(R.string.transport_id), my_transporter.getId());
        mTransportIdEditText.setText(transportID);

        /*
        if (isServiceRunning(TrackerService.class)) {
            // If service already running, simply update UI.
            setTrackingStatus(R.string.tracking);
        } else if (transportID.length() > 0) {
            // Inputs have previously been stored, start validation.
            checkLocationPermission();
        } else {
            Log.d("TrackerActivity", " ELSE ");
        }
        */

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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setTrackingStatus(int status) {
        boolean tracking = status == R.string.tracking;
        mTransportIdEditText.setEnabled(!tracking);
        mStartButton.setVisibility(tracking ? View.INVISIBLE : View.VISIBLE);
        ((TextView) findViewById(R.id.title)).setText(getString(status));
    }

    /**
     * Receives status messages from the tracking service.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTrackingStatus(intent.getIntExtra(getString(R.string.status), 0));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(TrackerService.STATUS_INTENT));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    /**
     * First validation check - ensures that required inputs have been
     * entered, and if so, store them and runs the next check.
     */
    private void checkInputFields() {
        if (mTransportIdEditText.length() == 0) {
            Toast.makeText(TrackerActivity.this, R.string.missing_inputs, Toast.LENGTH_SHORT).show();
        } else {
            // Store values.
            SharedPreferences.Editor editor = mPrefs.edit();
            //editor.putString(getString(R.string.transport_id), mTransportIdEditText.getText().toString());
            editor.putString(getString(R.string.transport_id), my_transporter.getId());
            editor.apply();
            // Validate permissions.
            checkLocationPermission();
        }
    }

    /**
     * Second validation check - ensures the app has location permissions, and
     * if not, requests them, otherwise runs the next check.
     */
    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TrackerActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_LOCATION);
        }
        checkGpsEnabled();
    }

    /**
     * Third and final validation check - ensures GPS is enabled, and if not, prompts to
     * enable it, otherwise all checks pass so start the location tracking service.
     */
    private void checkGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            reportGpsError();
        } else {
            resolveGpsError();
            startLocationService();
        }
    }

    /**
     * Callback for location permission request - if successful, run the GPS check.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            // We request storage perms as well as location perms, but don't care
            // about the storage perms - it's just for debugging.
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        reportPermissionsError();
                    } else {
                        resolvePermissionsError();
                        checkGpsEnabled();
                    }
                }
            }
        }
    }

    private void startLocationService() {
        Log.d("TrackerActivity", "start location service");
        // Before we start the service, confirm that we have extra power usage privileges.
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        startService(new Intent(this, TrackerService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(this, TrackerService.class));
    }

    private void confirmStop() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_stop))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mTransportIdEditText.setEnabled(true);
                        mStartButton.setVisibility(View.VISIBLE);
                        stopLocationService();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void reportPermissionsError() {
        Snackbar snackbar = Snackbar
                .make(
                        findViewById(R.id.rootView),
                        getString(R.string.location_permission_required),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(android.provider.Settings
                                .ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(
                android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void resolvePermissionsError() {
        if (mSnackbarPermissions != null) {
            mSnackbarPermissions.dismiss();
            mSnackbarPermissions = null;
        }
    }

    private void reportGpsError() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.rootView), getString(R.string
                        .gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id
                .snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

    private void resolveGpsError() {
        if (mSnackbarGps != null) {
            mSnackbarGps.dismiss();
            mSnackbarGps = null;
        }
    }
}