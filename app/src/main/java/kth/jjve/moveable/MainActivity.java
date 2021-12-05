package kth.jjve.moveable;
/*
This activity is the home screen of the app
The homescreen allows the user to connect to a bluetooth device
The homescreen will also show data when its there

Names: Jitse van Esch & Elisa Perini
Date: 12.12.21
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import kth.jjve.moveable.dialogs.SaveDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SaveDialog.SaveDialogListener, SensorEventListener {

    /*--------------------------- VIEW ----------------------*/
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /*------------------------- PREFS ---------------------*/

    /*---------------- INTERNAL SENSORS -------------------*/
    //TODO both variables were private final in android documentation, why?
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    double ax, ay, az;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------------------- Hooks ----------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        ImageView buttonBluetooth1 = findViewById(R.id.iv_main_bluetoothSearch);
        ImageView buttonBluetooth2 = findViewById(R.id.iv_main_bluetoothDisabled);
        ImageView buttonBluetooth3 = findViewById(R.id.iv_main_bluetoothEnabled);
        Button buttonRecord = findViewById(R.id.button_main_record);
        Button buttonSave = findViewById(R.id.button_main_stop);
        ImageView graph = findViewById(R.id.iv_main_datagraph);

        /*---------------------- Settings ----------------------*/

        /*--------------------- Tool bar --------------------*/
        setSupportActionBar(toolbar);

        /*---------------Navigation drawer menu -------------*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        /*---------------- INT SENSORS ----------------------*/
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Log.i(LOG_TAG, "accelerometer not found found");
        }

        /*-------------- On Click Listener ------------------*/
        buttonBluetooth1.setOnClickListener(this::onClick);
        buttonBluetooth2.setOnClickListener(this::onClick);
        buttonBluetooth3.setOnClickListener(this::onClick);
        buttonRecord.setOnClickListener(v -> {
            graph.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Recording has started", Toast.LENGTH_SHORT).show();
            //Todo: insert method to start recording here
            Log.i(LOG_TAG, "Recording has started");
        });
        buttonSave.setOnClickListener(v -> {
            //Todo: add stuff to stop recording here
            graph.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Recording has stopped", Toast.LENGTH_SHORT).show();
            openSaveDialog();
            Log.i(LOG_TAG, "Recording has stopped and is being saved");
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        navigationView.setCheckedItem(R.id.nav_home);
        Log.i(LOG_TAG, "onResume happens");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openBluetoothDialog(){
        Toast.makeText(getApplicationContext(), "Here will be a bluetooth selection", Toast.LENGTH_SHORT).show();
    }

    private void onClick(View v) {
        openBluetoothDialog();
    }

    /*||||||||||| SAVE DIALOG |||||||||||*/

    public void openSaveDialog() {
        // Method that will open the dialog for saving
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void applyName(String name) {
        // Method to get the wanted filename from the dialog into the activity
        // Todo: save the data here using the name somehow
    }

    @Override
    public void savingCancelled() {
        Toast.makeText(getApplicationContext(), "saving cancelled", Toast.LENGTH_SHORT).show();
    }

    /* SENSOR ACTIVITY MEHODS */

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}