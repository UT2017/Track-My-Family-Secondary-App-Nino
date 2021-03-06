package com.example.ninoapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrackerService extends Service {

    private boolean onTaskRemovedCalled = false;
    private String uniq_id;
    private String child_num;
    public static boolean serviceRunning = false;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient client;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, in on Bind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, on start command");

        Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, on start command, intent = "+intent);

        serviceRunning = true;

        if(intent!=null){
            if(intent.hasExtra("uniq_id")){
                Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, on start command, intent has extra uniq id");
                uniq_id = intent.getStringExtra("uniq_id");
            }
            if(intent.hasExtra("child_num")){
                Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, on start command, intent has extra child_num");
                child_num = intent.getStringExtra("child_num");
            }
        }
        requestLocationUpdates();
        onTaskRemovedCalled = false;

        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(MainActivity.APP_LOG_TAG,"in tracker service, onCreate");

    }

    @Override
    public boolean stopService(Intent name) {
        Log.v(MainActivity.APP_LOG_TAG,"in tracker service, onstop service");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(MainActivity.APP_LOG_TAG,"in tracker service, onDestroy");


        if(MainActivity.stopServiceCalled){
            Log.v(MainActivity.APP_LOG_TAG,"in tracker service, stop service called = true");
            client.removeLocationUpdates(locationCallback);
            this.stopSelf();
            return;
        }

        if(!onTaskRemovedCalled) {

            Log.v(MainActivity.APP_LOG_TAG,"in tracker service, onTask removed not called");

            Intent restartService = new Intent("RestartService");
            sendBroadcast(restartService);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        onTaskRemovedCalled = true;
        Log.v(MainActivity.APP_LOG_TAG,"in tracker service, ontask removed");


        if(MainActivity.stopServiceCalled){
            Log.v(MainActivity.APP_LOG_TAG,"in tracker service, ontask removed, stop service called = true");
            return;
        }else {
            Log.v(MainActivity.APP_LOG_TAG,"in tracker service, ontask removed, stop service called = false, flow 2");
            Intent restartService = new Intent("RestartService");
            sendBroadcast(restartService);
        }
    }

    private void requestLocationUpdates() {

        Log.v(MainActivity.APP_LOG_TAG,"in tracker service, requetLocationUpdates");

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        final String path = uniq_id + "/" + child_num;


        Log.v(MainActivity.APP_LOG_TAG,"in Trackerservice, in request location updates, path = "+path);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            Log.v(MainActivity.APP_LOG_TAG,"in service Location permission not granted - returning");

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //
            return;
        }


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Log.v(MainActivity.APP_LOG_TAG,"in tracker service, in  onLocationResult");
                Log.v(MainActivity.APP_LOG_TAG, "in tracker service, in onLocationResult, path = "+path);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(path);
                Log.v(MainActivity.APP_LOG_TAG, "in tracker service, in onLocationResult, data base ref = "+databaseReference);

                Location location = locationResult.getLastLocation();
                Log.v(MainActivity.APP_LOG_TAG, "in tracker service, in onLocationResult, location = "+location);

                if(location!=null){
                    Log.v(MainActivity.APP_LOG_TAG, "in tracker service, in onLocationResult, location is not null, setting value");
                    databaseReference.setValue(location);
                }
            }
        };

        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}
