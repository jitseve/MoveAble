package kth.jjve.moveable;
/*
This activity is the home screen of the app
The homescreen allows the user to connect to a bluetooth device
The homescreen will also show data when its there

Names: Jitse van Esch & Elisa Perini
Date: 12.12.21
 */

import static kth.jjve.moveable.utilities.VisibilityChanger.setViewVisibility;
import static kth.jjve.moveable.utilities.DisplayGraph.displayTheGraph;
import static kth.jjve.moveable.utilities.VisibilityChanger.setViewsInvisible;
import static kth.jjve.moveable.utilities.VisibilityChanger.setViewsVisible;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

//import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.navigation.NavigationView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import kth.jjve.moveable.datastorage.DataStorage;
import kth.jjve.moveable.utilities.TypeConverter;
import kth.jjve.moveable.dialogs.BluetoothActivity;
import kth.jjve.moveable.dialogs.SaveDialog;
import kth.jjve.moveable.datastorage.Settings;
import kth.jjve.moveable.utils.DataProcess;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SaveDialog.SaveDialogListener, SensorEventListener {
    /*--------------------------- VIEW ----------------------*/
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView bluetoothSettings;
    private ImageView bluetoothDisabled;
    private ImageView bluetoothEnabled;
    private TextView tempIntRotAcc, tempIntRotGyro;
//    public LineChart lineChart;
    private Toolbar toolbar;

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /*------------------------- PREFS ---------------------*/
    private Settings cSettings;
    private int cFrequencyInteger;
    private String IMU_COMMAND = "Meas/Acc/13";
    private String command_fragment = "Meas/Acc";
    private float deltaT;

    /*---------------- INTERNAL SENSORS -------------------*/
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    /*---------------- DATA PROCESS -------------------*/
    private DataProcess cDataProcess;
    private long prev_timestamp = 0;

    /*------------------------- BLUETOOTH ---------------------*/
    private BluetoothDevice mSelectedDevice = null;
    private boolean mBluetoothConnected;
    private final byte MOVESENSE_REQ = 1, MOVESENSE_RES = 2, REQUEST_ID = 99;
    private BluetoothGatt mBluetoothGatt = null;
    private byte[] command;
    public boolean resetBT = false;

    public static final UUID MOVESENSE_20_SERVICE =
            UUID.fromString("34802252-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_20_COMMAND_CHAR =
            UUID.fromString("34800001-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_20_DATA_CHAR =
            UUID.fromString("34800002-7185-4d5d-b431-630e7050e8f0");
    public static final UUID CLIENT_CHAR_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /*----------------------- HANDLER -----------------------*/
    public Handler mHandler;

    /*----------------------- DATA ----------------------*/
    DataStorage dataStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------------- HOOKS ----------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.main_toolbar);
        bluetoothSettings = findViewById(R.id.iv_main_bluetoothSearch);
        bluetoothDisabled = findViewById(R.id.iv_main_bluetoothDisabled);
        bluetoothEnabled = findViewById(R.id.iv_main_bluetoothEnabled);
        Button buttonRecord = findViewById(R.id.button_main_record);
        Button buttonSave = findViewById(R.id.button_main_stop);
        tempIntRotAcc = findViewById(R.id.temp_int_rot_from_acc);
        tempIntRotGyro = findViewById(R.id.temp_int_rot_from_gyro);
//        lineChart = findViewById(R.id.main_linechart);

        /*---------------- INIT -----------------*/
        getSettings();                  // Initialise settings
        setSupportActionBar(toolbar);   // Initialise toolbar
        initNavMenu();                  // Initialise navigation menu
        initInternalSensors();          // Initialise internal sensors
        mHandler = new Handler();       // Initialise handler
      
        /*-------------- LISTENERS --------------*/
        bluetoothSettings.setOnClickListener(this::onClick);
        bluetoothDisabled.setOnClickListener(this::onClick);
        bluetoothEnabled.setOnClickListener(this::onClick);
        // Todo: make sure that a click on this one disables bluetooth

        buttonRecord.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Recording has started", Toast.LENGTH_SHORT).show();
            acquireData();
            Log.i(LOG_TAG, "Recording has started"); });

        buttonSave.setOnClickListener(v -> {
            stopData();
            Log.i(LOG_TAG, "Recording has stopped and is being saved"); });
    }

    @Override
    protected void onResume(){
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
        getSettings();

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
    protected void onStop() {
        super.onStop();
        if (mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
            try {
                mBluetoothGatt.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
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
                    // Bluetooth connect gives a result, that is checked here
                    if (result.getResultCode() == 69){
                        Intent intent = result.getData();
                        if (intent != null){
                            //Extract data
                            mSelectedDevice = intent.getParcelableExtra(BluetoothActivity.SELECTED_DEVICE);
                            mBluetoothConnected = mSelectedDevice != null;
                            setViewVisibility(bluetoothEnabled, bluetoothDisabled, bluetoothSettings);
                        }
                    } else {
                        mBluetoothConnected = false;
                    }
                }
            });

    private void acquireData(){
        // Method to acquire the data. When connected to bluetooth, that sensor is used
        // Otherwise, internal sensors are used
        dataStorage = new DataStorage();
        cDataProcess = new DataProcess();
        setViewsVisible(tempIntRotAcc, tempIntRotGyro);
        if (mBluetoothConnected){
//            lineChart.setVisibility(View.VISIBLE);
            if (mSelectedDevice != null){
                mBluetoothGatt =
                        mSelectedDevice.connectGatt(this, false, mBtGattCallback);
            }
        } else{
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            prev_timestamp = System.currentTimeMillis(); // for 1st value of gyro rotation
        }
    }

    private void stopData(){
        // Method to stop the acquiring of data
//        setViewsInvisible(lineChart);
        if (mBluetoothConnected) {
            resetBluetooth();
        } else{
          mSensorManager.unregisterListener(this);
        }
        setViewsInvisible(tempIntRotAcc, tempIntRotGyro);
        openSaveDialog();
    }

    private void resetBluetooth(){
        if (mBluetoothGatt != null){
            resetBT = true;
            mBtGattCallback.onServicesDiscovered(mBluetoothGatt, 0);
            //Todo: don't think that this is right
            mBluetoothGatt.disconnect();
//      setViewsInvisible(lineChart);
            try {
                mBluetoothGatt.close();
                Log.i(LOG_TAG, "bluetooth gatt closed");
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception is " + e);
                e.printStackTrace();
            }
        }
    }

    /*||||||||||| BLUETOOTH CALLBACK |||||||||||*/
    private final BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // Method to check the state of the bluetooth sensor
            if (newState == BluetoothGatt.STATE_CONNECTED){
                mBluetoothGatt = gatt;
                mHandler.post(() -> setViewVisibility(bluetoothEnabled, bluetoothDisabled, bluetoothSettings));
                gatt.discoverServices();
                Log.i(LOG_TAG, "sensor is connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                mBluetoothGatt = null;
                mHandler.post(() -> setViewVisibility(bluetoothDisabled, bluetoothEnabled, bluetoothSettings));
                Log.i(LOG_TAG, "sensor is disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // Method to check if services are discovered
            if (status == BluetoothGatt.GATT_SUCCESS){
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service: services){
                    Log.i(LOG_TAG, service.getUuid().toString());
                }

                BluetoothGattService movesenseService = gatt.getService(MOVESENSE_20_SERVICE);
                if (movesenseService != null){
                    List<BluetoothGattCharacteristic> characteristics =
                            movesenseService.getCharacteristics();
                    for (BluetoothGattCharacteristic chara : characteristics){
                        Log.i(LOG_TAG, chara.getUuid().toString());
                    }

                    BluetoothGattCharacteristic commandChar =
                            movesenseService.getCharacteristic(MOVESENSE_20_COMMAND_CHAR);
                    if (resetBT) {
                        command = new byte[2];
                        command[0] = 2;
                        command[1] = 99;
                    } else{
                        command =
                                TypeConverter.stringToAsciiArray(REQUEST_ID, IMU_COMMAND);
                    }
                    commandChar.setValue(command);
                    boolean wasSuccessful = mBluetoothGatt.writeCharacteristic(commandChar);
                    Log.i("writeCharacteristic", "was successful = " + wasSuccessful);
                    if (wasSuccessful && resetBT){
                        resetBT = false;
                        mHandler.post(() -> Toast.makeText(getApplicationContext(), "Bluetooth reset", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.i(LOG_TAG, "service not found");
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // Method to check the bluetooth characteristics
            Log.i(LOG_TAG, "onCharacteristicWrite " + characteristic.getUuid().toString());
            BluetoothGattService movesenseService = gatt.getService(MOVESENSE_20_SERVICE);
            BluetoothGattCharacteristic dataCharacteristic =
                    movesenseService.getCharacteristic(MOVESENSE_20_DATA_CHAR);
            boolean success = gatt.setCharacteristicNotification(dataCharacteristic, true);
            if (success) {
                Log.i(LOG_TAG, "setCharacterNotification success");
                BluetoothGattDescriptor descriptor =
                        dataCharacteristic.getDescriptor(CLIENT_CHAR_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            } else {
                Log.i(LOG_TAG, "setCharacteristicNotification failed");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(LOG_TAG, "onCharacteristicRead " + characteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Method to get the data from the movesense
            if (MOVESENSE_20_DATA_CHAR.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                if (data[0] == MOVESENSE_RES && data[1] == REQUEST_ID) {
                    int time = TypeConverter.fourBytesToInt(data, 2);
                    int len = data.length;
                    if (command_fragment.equals("/Meas/Acc/")){
                        for (int i = 6; i < len; i+=12){
                            float accX = TypeConverter.fourBytesToFloat(data, i);
                            float accY = TypeConverter.fourBytesToFloat(data, 4 + i);
                            float accZ = TypeConverter.fourBytesToFloat(data, 8 + i);

                            cDataProcess.setAcceleration(accX, accY, accZ);
                            float rot = cDataProcess.EMWA_filter(1, 0.5F);

                            dataStorage.writeData(time, rot);

                            mHandler.post(() ->{
                                String s1 = "EMWA filter: " + Math.round(rot);
                                tempIntRotAcc.setText(s1);
                            });

                            String accStr = "" + accX + " " + accY + " " + accZ;
                            Log.i("acc data", "" + time + " " + accStr);}
                    } else if (command_fragment.equals("/Meas/Gyro/")){
                        for (int i = 6; i < len; i+=12){
                            float gyroX = TypeConverter.fourBytesToFloat(data, i);
                            float gyroY = TypeConverter.fourBytesToFloat(data, 4 + i);
                            float gyroZ = TypeConverter.fourBytesToFloat(data, 8 + i);

                            cDataProcess.setGyro(gyroX, gyroY, gyroZ, deltaT);
                            dataStorage.writeData(time, gyroY);

                            //Todo: display gyro in graph

                            String gyroStr = "" + gyroX + " " + gyroY + " " + gyroZ;
                            Log.i("gyro data", "" + time + " " + gyroStr);}
                    } else{
                        for (int i = 6; i < (len/2 +3); i+= 12){
                            int gyro_start = (len - 6) / 2;
                            float accX = TypeConverter.fourBytesToFloat(data, i);
                            float accY = TypeConverter.fourBytesToFloat(data, 4 + i);
                            float accZ = TypeConverter.fourBytesToFloat(data, 8 + i);
                            float gyroX = TypeConverter.fourBytesToFloat(data,  gyro_start + i);
                            float gyroY = TypeConverter.fourBytesToFloat(data,  gyro_start + 4+ i);
                            float gyroZ = TypeConverter.fourBytesToFloat(data, gyro_start + 4 + i);

                            cDataProcess.setAcceleration(accX, accY, accZ);
                            cDataProcess.setGyro(gyroX, gyroY, gyroZ, deltaT);
                            float emwa_rot = cDataProcess.EMWA_filter(1, 0.1F);
                            float comp_rot = cDataProcess.complimentaryFilter(1, 0.1F);

                            dataStorage.writeDataForCSV(time, emwa_rot, comp_rot);

                            mHandler.post(() -> {
                                String s1 = "EMWA filter: " + Math.round(emwa_rot);
                                tempIntRotAcc.setText(s1);
                                s1 = "Complimentary filter: " + Math.round(comp_rot);
                                tempIntRotGyro.setText(s1);
                            });

                            String accStr = "" + accX + " " + accY + " " + accZ;
                            Log.i("acc data", "" + time + " " + accStr);
                            String gyroStr = "" + gyroX + " " + gyroY + " " + gyroZ;
                            Log.i("gyro data", "" + time + " " + gyroStr);
                        }
                    }
                    if (dataStorage.getRunningTime() > 10000) stopData();
//                    displayTheGraph(dataStorage.getXGraphdata(), dataStorage.getYGraphdata(), lineChart);

                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (CLIENT_CHAR_CONFIG.equals(descriptor.getUuid()))
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // if success, we should receive data in onCharacteristicChanged
                    Log.i("onDescriptorWrite" , "GATT SUCCESSS");
                }
        }
    };

    /*||||||||||| SAVE DIALOG |||||||||||*/
    public void openSaveDialog() {
        // Method that will open the dialog for saving
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void applyName(String name) {
        // Method to get the wanted filename from the dialog into the activity
        dataStorage.writeCSV(name, mBluetoothConnected, command_fragment);
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void savingCancelled() {
        Toast.makeText(getApplicationContext(), "saving cancelled", Toast.LENGTH_SHORT).show();
    }


    /*||||||||||| SENSOR ACTIVITY |||||||||||*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            cDataProcess.setAcceleration(event.values[0], event.values[1], event.values[2]);

        }

        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            // internal sensor variables, can probs be turned into local variables for some
            float dT = (System.currentTimeMillis() - prev_timestamp) / (float) 1000; //units: seconds
            prev_timestamp = System.currentTimeMillis(); // for storing old value
            cDataProcess.setGyro((float) Math.toDegrees(event.values[0]), (float) Math.toDegrees(event.values[1]), (float) Math.toDegrees(event.values[2]), dT);
        }

        float EMWA_rot = cDataProcess.EMWA_filter(2, 0.5F);
        String s1 = "EMWA filter: " + Math.round(EMWA_rot);
        tempIntRotAcc.setText(s1);

        float comp_rot = cDataProcess.complimentaryFilter(2, 0.5F);
        String s2 ="Complimentary filter: " + Math.round(comp_rot);
        tempIntRotGyro.setText(s2);

        dataStorage.writeDataForCSV(prev_timestamp, EMWA_rot, comp_rot);

        if (dataStorage.getRunningTime() > 10000) stopData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // need to overwrite, when implement sensor stuff
        Toast.makeText(getApplicationContext(), "Sensor accuracy changed", Toast.LENGTH_SHORT).show();
        // Todo: check if we need to add something here if accuracy has changed
    }

    /*||||||||||| INITIALISATIONS |||||||||||*/
    private void getSettings() {
        // Method to deserialize input file
        try{
            FileInputStream fin = openFileInput("settings.ser");

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

        if (cSettings != null){
            cFrequencyInteger = cSettings.getFrequencyInteger();
            IMU_COMMAND = cSettings.getCommand();
            command_fragment = cSettings.getCommandFragment();
            deltaT = (float) 1/cFrequencyInteger;
        }else{
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

    }

    private void initInternalSensors() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Toast.makeText(getApplicationContext(), "No accelerometer found", Toast.LENGTH_SHORT).show();
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Toast.makeText(getApplicationContext(), "No gyroscope found", Toast.LENGTH_SHORT).show();
        }
    }

    private void initNavMenu(){
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }

}