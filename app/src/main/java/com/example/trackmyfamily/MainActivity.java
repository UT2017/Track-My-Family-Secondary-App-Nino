package com.example.trackmyfamily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 102;
    public final static String APP_LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(APP_LOG_TAG,"in onCreate");

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.v(APP_LOG_TAG, "GPS Provider disabled");
            finish();
        }

        checkLocationPermissions();

    }


    private void checkLocationPermissions(){

        Log.v(APP_LOG_TAG,"in checkLocationPermissions");

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Log.v(MainActivity.APP_LOG_TAG,"requesting permssion");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }else{
            //IF PERMISSION IS ALREADY GRANTED
            startTrackerService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        Log.v(APP_LOG_TAG,"in onRequestPermissionsResult");


        switch(requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //PERMISSION GRANTED - CALL MAIN METHOD
                    startTrackerService();
                }else{
                    //TO DO
                    //PERMISSION DENIED
                    Log.v(APP_LOG_TAG,"Permission not present");
                    if((ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))){
                        //TO DO
                        //SHOW WHY PERMISSION IS NEEDED
                        Log.v(APP_LOG_TAG,"Permission denied but never show me again not clicked");
                    }else{
                        Log.v(APP_LOG_TAG,"User said never show me again");
                        //TO DO
                        //WHAT TO DO IF USER HAS SELECTED NEVER ASK AGAIN AND THIS PERMISSION IS CRITICAL
                    }

                }
                break;
        }
    }

    private void startTrackerService() {


        Log.v(APP_LOG_TAG,"in startTrackerService");
        startService(new Intent(this,TrackerService.class));
        //finish();
    }


}