package kth.jjve.moveable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import kth.jjve.moveable.datastorage.Settings;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    /*------------------- LOG -------------------*/
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    /*------------------ VIEWS ------------------*/
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;
    private SwitchCompat switch1, switch2;

    /*---------------- SETTINGS -----------------*/
    private Settings cSettings;
    private String samplingFrequency;
    private boolean cAcc = true;
    private boolean cGyro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*------------------ HOOKS --------------*/
        drawerLayout = findViewById(R.id.drawer_layout_settings);
        toolbar = findViewById(R.id.settings_toolbar);
        navigationView = findViewById(R.id.nav_view_settings);
        Button button1 = findViewById(R.id.button_prefs_Save);
        spinner = findViewById(R.id.dropdown_settings_frequencies);
        switch1 = findViewById(R.id.switchAcc);
        switch2 = findViewById(R.id.switchGyro);

        /*----------------- TOOLBAR -------------*/
        setSupportActionBar(toolbar);

        /*----------------- SPINNER -------------*/
        adapter = ArrayAdapter.createFromResource(this, R.array.frequencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /*------------------ MENU ---------------*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.nav_open,
                R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);

        /*-------------- PREFERENCES -----------*/
        getSettings();

        /*-------------- LISTENERS -------------*/
        button1.setOnClickListener(v -> {
            setSettings();
            Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });

        spinner.setOnItemSelectedListener(this);

        switch1.setOnCheckedChangeListener(((buttonView, isChecked) -> cAcc = isChecked));
        switch2.setOnCheckedChangeListener(((buttonView, isChecked) -> cGyro = isChecked));
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_settings);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home){
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        samplingFrequency = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void getSettings(){
        //Method that saves the settings
        deserialise("settings.ser");

        if(cSettings != null){
            //Todo: get settings here
            int spinnerPosition = adapter.getPosition(cSettings.getFrequency());
            spinner.setSelection(spinnerPosition);
            cAcc = cSettings.getAcc();
            cGyro = cSettings.getGyro();
        }

        switch1.setChecked(cAcc);
        switch2.setChecked(cGyro);
    }

    private void setSettings(){
        //Method that sets the settings
        if(cSettings == null){
            cSettings = new Settings(samplingFrequency, cAcc, cGyro);
        }else{
            cSettings.setFrequency(samplingFrequency);
            cSettings.setBtType(cAcc, cGyro);
        }

        serialise("settings.ser", cSettings);

    }

    private void deserialise(String s) {
        // Method to deserialise input file
        try{
            FileInputStream fin = openFileInput(s);

            // Wrapping our stream
            ObjectInputStream oin = new ObjectInputStream(fin);

            // Reading in our object
            cSettings = (Settings) oin.readObject();

            // Closing our object stream which also closes the wrapped stream
            oin.close();

        } catch (Exception e) {
            Log.i(LOG_TAG, "Error is " + e);
            e.printStackTrace();
        }
    }

    private void serialise(String s, Object o) {
        //Method to serialise input file
        try{
            FileOutputStream fos = openFileOutput(s, Context.MODE_PRIVATE);

            // Wrapping our file stream
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // Writing the serializable object to the file
            oos.writeObject(o);

            // Closing our object stream which also closes the wrapped stream.
            oos.close();
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception is " + e);
            e.printStackTrace();
        }
    }


}
