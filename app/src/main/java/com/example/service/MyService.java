package com.example.service;

import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.mapdemo.R;

/**
 * Created by user on 1/27/2016.
 */
public class MyService extends Service {

    private WindowManager manager;
    private CustomViewGroup view;
    private WindowManager.LayoutParams localLayoutParams;
    private Handler handler;
    private Handler handler_add;
    private Handler handler_remove;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                String s = msg.getData().getString("message");
                Toast.makeText(MyService.this, s, Toast.LENGTH_SHORT).show();
            }
        };

        handler_add = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                manager.addView(view, localLayoutParams);
//                Log.e("service", "adview");
            }
        };

        handler_remove = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                manager.removeView(view);
                view = null;
//                Log.e("service", "removeview");
            }
        };

        startThread();

//        Log.e("service", "service onStartCommand");

        return Service.START_STICKY;
    }

    private void sendMessage(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("message", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void startThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        doIt();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void doIt() {
//        Log.e("service", "service doit");
        SharedPreferences preferences = getSharedPreferences("screen", MODE_PRIVATE);
        boolean is_show = preferences.getBoolean("is_show", false);
        boolean is_change = preferences.getBoolean("is_change", true);
        if (is_show) {
            if (view == null) {
                prepareView();

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "abcd");
                message.setData(bundle);
                handler_add.sendMessage(message);
            } else {
                if (is_change) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "abcd");
                    message.setData(bundle);
                    handler_remove.sendMessage(message);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("is_change", false);
                    editor.commit();
                }
            }
        } else {
            if (view != null) {
//                manager.removeView(view);
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("message", "abcd");
                message.setData(bundle);
                handler_remove.sendMessage(message);

//                sendMessage("service REMOVE_VIEW");
            }
        }
    }

    private void prepareView() {
        SharedPreferences preferences = getSharedPreferences("screen", MODE_PRIVATE);

        manager = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));

        localLayoutParams = new WindowManager.LayoutParams(100, 100, 2007, 8, -2);
//        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
//        localLayoutParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        localLayoutParams.gravity = Gravity.CENTER;
        localLayoutParams.flags =
                // can touch or click
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                        // this is to enable the notification to recieve touch events
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |

                        // Draws over status bar
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;


//        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        localLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.width = (int) ((50) * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.height = (int) ((50) * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;

        view = new CustomViewGroup(this);
        view.setBackgroundResource(R.drawable.dr_circle_red);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences localSharedPreferences = getApplicationContext().getSharedPreferences("prefs", 0);
                double lat = Double.valueOf(localSharedPreferences.getString("lat", "10.777831")).doubleValue();
                double ln = Double.valueOf(localSharedPreferences.getString("ln", "106.681524")).doubleValue();
                SharedPreferences.Editor editor = localSharedPreferences.edit();
                editor.putString("lat", String.valueOf(lat + 0.0002));
                editor.putString("ln", String.valueOf(ln));
                editor.commit();
//                setFakeLocation();
                Toast.makeText(MyService.this, lat + "\n" + ln, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service", "service onDestroy");
    }

    private void setFakeLocation() {
        if (!checkGPS()) {
            return;
        }

        if (!checkMockLocationMode()) {
            return;
        }

        ServiceHelper.startService(this);
    }

    // ###Check if GPS provider is enabled
    private boolean checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
