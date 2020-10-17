package com.example.ninoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    public static boolean stopServiceCalled = false;
    private static final int PERMISSION_REQUEST_CODE = 102;
    public final static String APP_LOG_TAG = "MainActivity";
    private EditText codeEditText = null;
    private EditText childNumEditText = null;
    private TextView codeTv = null;
    private TextView childNumTv = null;
    private TextView welcomeTv = null;
    private Button nextButton = null;
    private String code_text;
    private String child_text;
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(APP_LOG_TAG,"in onCreate");

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.v(APP_LOG_TAG, "GPS Provider disabled");
            // TO DO - ASK TO ENABLE GPS
        }


        getAllViewReferences();
        checkLocationPermissions();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateUiBack(){


        Log.v(APP_LOG_TAG,"in update UI back");

        codeEditText.setVisibility(View.VISIBLE);
        codeEditText.setText("");
        childNumEditText.setVisibility(View.VISIBLE);
        childNumEditText.setText("");
        codeTv.setText("Enter code");
        childNumTv.setText("Enter child number");
        nextButton.setVisibility(View.VISIBLE);
        welcomeTv.setText("Welcome to Nino App");

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


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                stopServiceCalled = false;

                code_text = codeEditText.getText().toString();
                child_text = childNumEditText.getText().toString();

                Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, onClick");
                Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, onClick, code_text = "+code_text);
                Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, onClick, child_num_text = "+child_text);

                if(code_text.isEmpty()&&child_text.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a valid code and child number to continue", Toast.LENGTH_LONG).show();
                }else if(code_text.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a valid code to continue", Toast.LENGTH_LONG).show();
                }else if(child_text.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a valid child number to continue", Toast.LENGTH_LONG).show();
                }else{

                    Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, in else block");

                    i = new Intent(MainActivity.this,TrackerService.class);
                    i.putExtra("uniq_id",code_text);
                    i.putExtra("child_num",child_text);


                    Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, in else, code_text = "+code_text);
                    Log.v(MainActivity.APP_LOG_TAG,"in Main Activity, starttrackerservice, in else, child_num = "+child_text);

                    startService(i);

                    updateUI();


                }
            }
        });


        // TO DO - CALL FINISH TO END ACTIVITY
        //finish();
    }

    private void getAllViewReferences() {

        codeEditText = (EditText) findViewById(R.id.editTextCode);
        childNumEditText = (EditText) findViewById(R.id.editTextChild);
        codeTv = (TextView) findViewById(R.id.textViewCode);
        childNumTv = (TextView) findViewById(R.id.textViewChild);
        nextButton = (Button) findViewById(R.id.next_button);
        welcomeTv = (TextView) findViewById(R.id.textViewWelcome);

    }

    private void updateUI() {

        codeEditText.setVisibility(View.GONE);
        childNumEditText.setVisibility(View.GONE);
        codeTv.setText("Code input = "+code_text);
        childNumTv.setText("Child num input = "+child_text);
        nextButton.setVisibility(View.GONE);
        welcomeTv.setText("SUCCESS");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuRefresh:
                onRefreshClicked();
                break;
        }

        return true;
    }

    private void onRefreshClicked() {
        Toast.makeText(this, "Refresh Clicked", Toast.LENGTH_SHORT).show();



        Log.v(APP_LOG_TAG,"in on refresh, going for kill 1 ");

        if(TrackerService.serviceRunning) {

            Log.v(APP_LOG_TAG,"in on refresh, Service is running, calling stop service ");
            stopService(i);
            stopServiceCalled = true;
        }



        String path = code_text + "/" + child_text;



        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Log.v(MainActivity.APP_LOG_TAG, "in onRefreshClicked, data base ref = "+databaseReference);

        if(databaseReference!=null){
            Log.v(MainActivity.APP_LOG_TAG, "in onRefreshClicked, going for kill 2  = kill DB ");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Log.v(APP_LOG_TAG,"in on Data change, removing value");
                        Log.v(APP_LOG_TAG,"in on Data change, removing value, code text = "+code_text);
                        Log.v(APP_LOG_TAG,"in on Data change, removing value, child num text = "+child_text);
                        dataSnapshot.getRef().child(code_text).child(child_text).setValue(null);
                        dataSnapshot.getRef().child(code_text).child(child_text).removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        Log.v(APP_LOG_TAG,"in on refresh, going for kill 3 - update ui back to older levels");

        updateUiBack();
    }
}