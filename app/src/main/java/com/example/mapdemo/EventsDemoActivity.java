/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This shows how to listen to some {@link GoogleMap} events.
 */
public class EventsDemoActivity extends AppCompatActivity
        implements OnMapClickListener, OnMapLongClickListener, OnCameraChangeListener,
        OnMapReadyCallback, View.OnClickListener {

    private TextView mTapTextView;

    private TextView mCameraTextView;

    private Button btnEnable;

    private Button btnDisable;

    private GoogleMap mMap;

    private LocationManager mLocationManager;

    private MyLocationListener mMyLocationListener;

    private String mLocationProvider;

    private LatLng currentLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_demo);

        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mCameraTextView = (TextView) findViewById(R.id.camera_text);
        btnEnable = (Button) findViewById(R.id.btnEnable);
        btnDisable = (Button) findViewById(R.id.btnDisable);

        btnEnable.setOnClickListener(this);
        btnDisable.setOnClickListener(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        currentLatLng = new LatLng(10.781927, 106.675786);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Marker"));

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {
        currentLatLng = point;
        mTapTextView.setText("tapped, point=" + point);

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point).title("Marker"));
    }

    private void setLocationManager() {
        mMyLocationListener = new MyLocationListener();
        mLocationProvider = LocationManager.GPS_PROVIDER;
        if (mLocationManager.getProvider(mLocationProvider) == null) {
            mLocationManager.addTestProvider(mLocationProvider, true, true, true, true, true, true, true, 0, 5);
        }
        mLocationManager.setTestProviderEnabled(mLocationProvider, true);
        mLocationManager.requestLocationUpdates(mLocationProvider, 0l, 0f, mMyLocationListener);
    }

    private void setFakeLocation() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (!checkGPS()) {
            return;
        }
        if (!checkMockLocationMode()) {
            return;
        }

//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMyLocationListener = new MyLocationListener();
        mLocationProvider = LocationManager.GPS_PROVIDER;
        if (mLocationManager.getProvider(mLocationProvider) == null) {
            mLocationManager.addTestProvider(mLocationProvider, false, false, false,
                    false, false, false, false, 0, 1);
        }
        mLocationManager.setTestProviderEnabled(
                mLocationProvider, true);
        mLocationManager.setTestProviderStatus(mLocationProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
        mLocationManager.requestLocationUpdates(mLocationProvider, 0l, 0f, mMyLocationListener);

        final Location loc = new Location(mLocationProvider);
        loc.setLatitude(currentLatLng.latitude);
        loc.setLongitude(currentLatLng.longitude);
        loc.setAltitude(0);
//        loc.setSpeed(55.55f);
        loc.setTime(System.currentTimeMillis());
        loc.setAccuracy(1f);
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
//        loc.setElapsedRealtimeNanos(System.nanoTime());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocationManager.setTestProviderLocation(mLocationProvider, loc);
            }
        }, 1000);
    }

    private void clearFakeLocation() {
        if (mLocationManager != null) {
            mLocationManager.clearTestProviderLocation(mLocationProvider);
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mTapTextView.setText("long pressed, point=" + point);
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        mCameraTextView.setText(position.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnable:
                setFakeLocation();
                break;
            case R.id.btnDisable:
                clearFakeLocation();
                break;
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(EventsDemoActivity.this, "onLocationChanged\n" + location.getLatitude() + "\n" + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    // ###Check if GPS provider is enabled
    private boolean checkGPS() {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Enable GPS");
            dialog.setPositiveButton("Open location setting",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
            dialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {

                        }
                    });
            dialog.show();
            return false;
        } else {
            return true;
        }
    }

    // ###Check if mock location is enabled
    private boolean checkMockLocationMode() {
        boolean isMockLocation = false;

        //if marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                AppOpsManager opsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED);
            } catch (Exception e) {

            }
        } else {
            // in marshmallow this will always return true
            isMockLocation = !Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }

        if (!isMockLocation) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Enable mock location");
            dialog.setPositiveButton("Open developer setting",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
            dialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                        }
                    });
            dialog.show();
        }

        return isMockLocation;
    }
}
