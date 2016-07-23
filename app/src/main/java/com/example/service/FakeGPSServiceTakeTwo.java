package com.example.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class FakeGPSServiceTakeTwo extends Service {
    boolean highAccuracy;
    LocationManager locationManager;
    Timer timer;

    /* renamed from: com.fakegps.mock.service.FakeGPSServiceTakeTwo.1 */
    class C03551 extends TimerTask {
//        final /* synthetic */ double val$lat;
//        final /* synthetic */ double val$ln;

        C03551() {
//        C03551(double d, double d2) {
//            this.val$lat = d;
//            this.val$ln = d2;
        }

        public void run() {
            FakeGPSServiceTakeTwo.this.setLocation();
//            FakeGPSServiceTakeTwo.this.setLocation(this.val$lat, this.val$ln);
        }
    }

    public FakeGPSServiceTakeTwo() {
        this.highAccuracy = false;
    }

    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void onDestroy() {
        try {
            stop();
        } catch (Exception e) {
        }
    }

    private void pauseThread() {
        try {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
            try {
                this.locationManager.setTestProviderEnabled("gps", false);
            } catch (Exception e) {
            }
            try {
                this.locationManager.removeTestProvider("gps");
            } catch (Exception e2) {
            }
        } catch (Exception e3) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.getProvider("gps") != null) {
                try {
                    locationManager.setTestProviderEnabled("gps", false);
                    locationManager.removeTestProvider("gps");
                } catch (Exception e4) {
                }
            }
        }
    }

    private void stop() {
        pauseThread();
        stopForeground(true);
        stopSelf();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getIntExtra(Constants.RESPONSE_TYPE, 1) == 1) {
            stop();
        } else if (intent != null) {
            if (intent.getIntExtra(Constants.RESPONSE_TYPE, 1) == 2) {
                pauseThread();
                startFaking();
            }
        } else {
            pauseThread();
            startFaking();
        }
        return 1;
    }

    private void startFaking() {
        this.highAccuracy = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("high_accuracy", false);
//        SharedPreferences localSharedPreferences = getApplicationContext().getSharedPreferences("prefs", 0);
//        double lat = Double.valueOf(localSharedPreferences.getString("lat", "10.777831")).doubleValue();
//        double ln = Double.valueOf(localSharedPreferences.getString("ln", "106.681524")).doubleValue();
        this.locationManager.addTestProvider("gps", false, false, false, false, false, false, false, 1, 1);
        this.locationManager.setTestProviderEnabled("gps", true);
        this.timer = new Timer();
        this.timer.schedule(new C03551(), 500, 2000);
    }

    @SuppressLint({"NewApi"})
    private void setLocation() {
        try {
            Location location = new Location("gps");
            if (VERSION.SDK_INT >= 17) {
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            SharedPreferences localSharedPreferences = getApplicationContext().getSharedPreferences("prefs", 0);
            double lat = Double.valueOf(localSharedPreferences.getString("lat", "10.777831")).doubleValue();
            double ln = Double.valueOf(localSharedPreferences.getString("ln", "106.681524")).doubleValue();
            location.setLatitude(lat);
            location.setLongitude(ln);
            location.setAccuracy(3.0f);
            location.setAltitude(0.0d);
            location.setTime(System.currentTimeMillis());
            location.setBearing(0.0f);
            this.locationManager.setTestProviderLocation("gps", location);
            Log.d("setLocation", System.currentTimeMillis() + " " + lat);
        } catch (Exception e) {
        }
    }
}
