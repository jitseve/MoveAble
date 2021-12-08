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

import android.annotation.SuppressLint;
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

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.navigation.NavigationView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import kth.jjve.moveable.datastorage.DataStorage;
import kth.jjve.moveable.datastorage.Settings;
import kth.jjve.moveable.dialogs.BluetoothActivity;
import kth.jjve.moveable.dialogs.SaveDialog;
import kth.jjve.moveable.utilities.TypeConverter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SaveDialog.SaveDialogListener, SensorEventListener {

    /*--------------------------- VIEW ----------------------*/
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView bluetoothSettings;
    private ImageView bluetoothDisabled;
    private ImageView bluetoothEnabled;
    public LineChart lineChart;

    private TextView tempTimeView, tempAccView;

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /*------------------------- PREFS ---------------------*/
    private Settings cSettings;
    private int cFrequencyInteger;
    private final String IMU_COMMAND = "Meas/Acc/13"; //Todo: get the IMU command from the preferences

    /*---------------- INTERNAL SENSORS -------------------*/
    //TODO both variables were private final in android documentation, why?
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private double dT;
    private double ax, ay, az;
    private double roll_x, pitch_y, yaw_z;
    private double gx, gy, gz;
    private double rot_x = 0;
    private double rot_y = 0;
    private double rot_z = 0;

    /*------------------------- BLUETOOTH ---------------------*/
    private BluetoothDevice mSelectedDevice = null;
    private boolean mBluetoothConnected;
    private final byte MOVESENSE_REQ = 1, MOVESENSE_RES = 2, REQUEST_ID = 99;
    private BluetoothGatt mBluetoothGatt = null;

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
        tempTimeView = findViewById(R.id.temp_tv_main_Time); //Todo: when graph is done, these can disappear
        tempAccView = findViewById(R.id.temp_tv_main_Acc);
        lineChart = findViewById(R.id.main_linechart);

        /*---------------------- Settings ----------------------*/
        deserialize();
        if (cSettings == null){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }


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
        dT = 1/ (double) cFrequencyInteger;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Toast.makeText(getApplicationContext(), "No accelerometer found", Toast.LENGTH_SHORT).show();
        }

        // Accelerometer
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Toast.makeText(getApplicationContext(), "No gyroscope found", Toast.LENGTH_SHORT).show();
        }

        /*-------------- On Click Listener ------------------*/
        bluetoothSettings.setOnClickListener(this::onClick);
        bluetoothDisabled.setOnClickListener(this::onClick);
        bluetoothEnabled.setOnClickListener(this::onClick);
        buttonRecord.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Recording has started", Toast.LENGTH_SHORT).show();
            acquireData();
            Log.i(LOG_TAG, "Recording has started");          
        });
        buttonSave.setOnClickListener(v -> {
            stopData();
            tempAccView.setVisibility(View.INVISIBLE);
            tempTimeView.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Recording has stopped", Toast.LENGTH_SHORT).show();
            openSaveDialog();
            Log.i(LOG_TAG, "Recording has stopped and is being saved");
        });

        mHandler = new Handler();
    }

    @Override
    protected void onResume(){
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        stopData();
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
                            mBluetoothConnected = mSelectedDevice != null;
                            setViewVisibility(bluetoothEnabled, bluetoothDisabled, bluetoothSettings);
                        }
                    } else {
                        mBluetoothConnected = false;
                    }
                }
            });

    private void acquireData(){
        if (mBluetoothConnected){
            dataStorage = new DataStorage();
            lineChart.setVisibility(View.VISIBLE);
            if (mSelectedDevice != null){
                mBluetoothGatt =
                        mSelectedDevice.connectGatt(this, false, mBtGattCallback);
            }
        } else{
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void stopData(){
      if (mBluetoothConnected){
        if(mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
            lineChart.setVisibility(View.INVISIBLE);
            try{
                mBluetoothGatt.close();
                Log.i(LOG_TAG, "bluetooth gatt closed");
            }catch (Exception e){
                Log.i(LOG_TAG, "Exception is " + e);
                e.printStackTrace();
            }
        }
      }else{     
          mSensorManager.unregisterListener(this);
      }
    }

    /*||||||||||| BLUETOOTH STUFF |||||||||||*/
    private final BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED){
                mBluetoothGatt = gatt;
                mHandler.post(() -> setViewVisibility(bluetoothEnabled, bluetoothDisabled, bluetoothSettings));
                gatt.discoverServices();
                Log.i(LOG_TAG, "state connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                mBluetoothGatt = null;
                mHandler.post(() -> setViewVisibility(bluetoothDisabled, bluetoothEnabled, bluetoothSettings));
                Log.i(LOG_TAG, "state disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
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
                    byte[] command =
                            TypeConverter.stringToAsciiArray(REQUEST_ID, IMU_COMMAND);
                    commandChar.setValue(command);
                    boolean wasSuccessful = mBluetoothGatt.writeCharacteristic(commandChar);
                    Log.i("writeCharacteristic", "was successful = " + wasSuccessful);
                } else {
                    Log.i(LOG_TAG, "service not found");
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
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
            if (MOVESENSE_20_DATA_CHAR.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                if (data[0] == MOVESENSE_RES && data[1] == REQUEST_ID) {
                    // parse and interpret the data, ...
                    int time = TypeConverter.fourBytesToInt(data, 2);
                    float accX = TypeConverter.fourBytesToFloat(data, 6);
                    float accY = TypeConverter.fourBytesToFloat(data, 10);
                    float accZ = TypeConverter.fourBytesToFloat(data, 14);

                    dataStorage.writeData(time, accX);
                    displayTheGraph(dataStorage.getXGraphdata(), dataStorage.getYGraphdata(), lineChart);

                    // Todo: filter the data (one filter function)
                    // Todo: stop the data after 10 seconds
                    // Todo: add filtered data to a list
                    // Todo: save the list (expanding)

                    String accStr = "" + accX + " " + accY + " " + accZ;
                    Log.i("acc data", "" + time + " " + accStr);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(LOG_TAG, "onDescriptorWrite, status " + status);

            if (CLIENT_CHAR_CONFIG.equals(descriptor.getUuid()))
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // if success, we should receive data in onCharacteristicChanged
                    mHandler.post(() -> {
                        //Todo: here we could update something to the ui, but we could also not
                    });
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
        // Todo: save the data here using the name somehow
    }

    @Override
    public void savingCancelled() {
        Toast.makeText(getApplicationContext(), "saving cancelled", Toast.LENGTH_SHORT).show();
    }

        /*||||||||||| SENSOR ACTIVITY |||||||||||*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];

            // rotation from acceleration data
            roll_x = Math.toDegrees(Math.atan(ay / (Math.sqrt(Math.pow(ax, 2) + Math.pow(ax, 2)))));
            pitch_y = Math.toDegrees(Math.atan(ax / (Math.sqrt(Math.pow(ay, 2) + Math.pow(ax, 2)))));
            yaw_z = Math.toDegrees(Math.atan( (Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2)) / az )));
        }
        //Log.i(LOG_TAG, "Acceleration    x: " + ax + ", y: " + ay + ", z: " + az);
        //Log.i(LOG_TAG, "roll_x: " + roll_x + ", pitch_y: " + pitch_y + ", yaw_z: " + yaw_z);

        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            gx=event.values[0];
            gy=event.values[1];
            gz=event.values[2];

            //rotation from gyroscope
            rot_x = rotFromGyroscope(gx, rot_x);
            rot_y = rotFromGyroscope(gx, rot_y);
            rot_z = rotFromGyroscope(gx, rot_z);
        }
        //Log.i(LOG_TAG, "Gyroscope    x: " + gx + ", y: " + gy + ", z: " + gz);
        Log.i(LOG_TAG, "Rotation:      rot_x: " + rot_x + ", rot_y: " + rot_y + ", rot_z: " + rot_z);
        //Todo: figure out how to save values: list, array, ... ?
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // need to overwrite, when implement sensor stuff
        // Todo: check if we need to add something here if accuracy has changed
    }

    //TODO: add to filtering class or make it take all three rots at once
    private double rotFromGyroscope(double gyro_value, double previous_rot_value) {
        return previous_rot_value + (dT * gyro_value);
    }


    /*||||||||||| SERIALISATION |||||||||||*/
    private void deserialize() {
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
        }
    }


}