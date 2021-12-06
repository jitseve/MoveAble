package kth.jjve.moveable;
/*
This activity is the home screen of the app
The homescreen allows the user to connect to a bluetooth device
The homescreen will also show data when its there

Names: Jitse van Esch & Elisa Perini
Date: 12.12.21
 */

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import kth.jjve.moveable.dialogs.BluetoothActivity;
import kth.jjve.moveable.dialogs.SaveDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SaveDialog.SaveDialogListener {

    /*--------------------------- VIEW ----------------------*/
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView bluetoothSettings;
    private ImageView bluetoothDisabled;
    private ImageView bluetoothEnabled;

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /*------------------------- PREFS ---------------------*/

    /*------------------------- BLUETOOTH ---------------------*/
    private BluetoothDevice mSelectedDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------------------- Hooks ----------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        bluetoothSettings = findViewById(R.id.iv_main_bluetoothSearch);
        bluetoothDisabled = findViewById(R.id.iv_main_bluetoothDisabled);
        bluetoothEnabled = findViewById(R.id.iv_main_bluetoothEnabled);
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

        /*-------------- On Click Listener ------------------*/
        bluetoothSettings.setOnClickListener(this::onClick);
        bluetoothDisabled.setOnClickListener(this::onClick);
        bluetoothEnabled.setOnClickListener(this::onClick);
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
        navigationView.setCheckedItem(R.id.nav_home);
        Log.i(LOG_TAG, "onResume happens");
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

    private void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        activityLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 69){
                        Intent intent = result.getData();
                        if (intent != null){
                            //Extract data
                            mSelectedDevice = intent.getParcelableExtra(BluetoothActivity.SELECTED_DEVICE);
                            bluetoothSettings.setVisibility(View.INVISIBLE);
                            if (mSelectedDevice != null){
                                bluetoothDisabled.setVisibility(View.INVISIBLE);
                                bluetoothEnabled.setVisibility(View.VISIBLE);
                            } else {
                                bluetoothEnabled.setVisibility(View.INVISIBLE);
                                bluetoothDisabled.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        bluetoothSettings.setVisibility(View.INVISIBLE);
                        bluetoothEnabled.setVisibility(View.INVISIBLE);
                        bluetoothDisabled.setVisibility(View.VISIBLE);
                    }
                }
            });


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
}