package kth.jjve.moveable;
/*
This activity is the home screen of the app
The homescreen allows the user to connect to a bluetooth device
The homescreen will also show data when its there

Names: Jitse van Esch & Elisa Perini
Date: 12.12.21
 */

import static kth.jjve.moveable.utilities.VisibilityChanger.setViewVisibility;
import kth.jjve.moveable.utilities.TypeConverter;
import kth.jjve.moveable.dialogs.BluetoothActivity;
import kth.jjve.moveable.dialogs.SaveDialog;
import kth.jjve.moveable.datastorage.Settings;
import kth.jjve.moveable.utils.DataProcess;

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

import com.google.android.material.navigation.NavigationView;


import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

public class MainActivity<rotFromGyro> extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SaveDialog.SaveDialogListener, SensorEventListener {

    /*--------------------------- VIEW ----------------------*/
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView bluetoothSettings;
    private ImageView bluetoothDisabled;
    private ImageView bluetoothEnabled;

    private TextView tempTimeView, tempAccView, tempIntRotAcc, tempIntRotGyro;

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /*------------------------- PREFS ---------------------*/
    private Settings cSettings;
    private int cFrequencyInteger;
    private int frequencyInteger;
    private String IMU_COMMAND = "Meas/Acc/13"; //Todo: get the IMU command from the preferences

    /*---------------- INTERNAL SENSORS -------------------*/
    //TODO both variables were private final in android documentation, why?
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    // internal sensor variables, can probs be turned into local variables for some
    private float dT;
    private long timestamp = 0;
    private float[] pitchRollYaw = new float[3];
    private float[] rotFromGyro = {0, 0, 0};
    private float[] complimentary_filtered_values = {0, 0, 0};
    private float[] EWMA_filtered_values = {0,0,0};

    /*------------------------- BLUETOOTH ---------------------*/
    private BluetoothDevice mSelectedDevice = null;
    private boolean mBluetoothConnected;
    private final byte MOVESENSE_REQ = 1, MOVESENSE_RES = 2, REQUEST_ID = 99;
    private BluetoothGatt mBluetoothGatt = null;

//    public final UUID MOVESENSE_20_SERVICE = UUID.fromString(getResources().getString(R.string.uuidMS2_0Service));
//    public final UUID MOVESENSE_20_COMMAND_CHAR = UUID.fromString(getResources().getString(R.string.uuidMS2_0CommandChar));
//    public final UUID MOVESENSE_20_DATA_CHAR = UUID.fromString(getResources().getString(R.string.uuidMS2_0DataChar));
//    public final UUID CLIENT_CHAR_CONFIG = UUID.fromString(getResources().getString(R.string.uuidClientCharConfig));

    public static final UUID MOVESENSE_20_SERVICE =
            UUID.fromString("34802252-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_20_COMMAND_CHAR =
            UUID.fromString("34800001-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_20_DATA_CHAR =
            UUID.fromString("34800002-7185-4d5d-b431-630e7050e8f0");
    // UUID for the client characteristic, which is necessary for notifications
    public static final UUID CLIENT_CHAR_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /*----------------------- HANDLER -----------------------*/
    public Handler mHandler;

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
        tempIntRotAcc = findViewById(R.id.temp_int_rot_from_acc);
        tempIntRotGyro = findViewById(R.id.temp_int_rot_from_gyro);

        /*---------------------- Settings ----------------------*/
        deserialise();
        if (cSettings == null){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        frequencyInteger = cSettings.getFrequencyInteger();


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
        //dT = 1/ Double.valueOf(frequencyInteger);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.i(LOG_TAG, "accelerometer found");
        } else {
            Toast.makeText(getApplicationContext(), "No accelerometer found", Toast.LENGTH_SHORT).show();
        }

        // Gyroscope
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
        stopData();
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
            if (mSelectedDevice != null){
                mBluetoothGatt =
                        mSelectedDevice.connectGatt(this, false, mBtGattCallback);
            }
        } else{
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            //timestamp for 1st value of gyroscope rotation (init)
            timestamp = System.currentTimeMillis();
        }
    }

    private void stopData(){
      if (mBluetoothConnected){
        if(mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
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
                    boolean wasSuccessfull = mBluetoothGatt.writeCharacteristic(commandChar);
                    Log.i("writeCharacteristic", "was succesfull = " + wasSuccessfull);
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
                Log.i(LOG_TAG, "setCharactNotification success");
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
                    // NB! use length of the array to determine the number of values in this
                    // "packet", the number of values in the packet depends on the frequency set(!)
                    int len = data.length;

                    // parse and interpret the data, ...
                    int time = TypeConverter.fourBytesToInt(data, 2);
                    float accX = TypeConverter.fourBytesToFloat(data, 6);
                    float accY = TypeConverter.fourBytesToFloat(data, 10);
                    float accZ = TypeConverter.fourBytesToFloat(data, 14);

                    // Todo: filter the data (one filter function)
                    // Todo: add filtered data to a list
                    // Todo: save the list (expanding)
                    // Todo: save the data to a list with a fixed length to display in graph

                    String accStr = "" + accX + " " + accY + " " + accZ;
                    Log.i("acc data", "" + time + " " + accStr);

                    @SuppressLint("DefaultLocale") final String viewDataStr = String.format("%.2f, %.2f, %.2f", accX, accY, accZ);
                    mHandler.post(() -> {
                        String timeString = time + " ms";
                        tempTimeView.setText(timeString);
                        tempTimeView.setVisibility(View.VISIBLE);
                        tempAccView.setText(viewDataStr);
                        tempAccView.setVisibility(View.VISIBLE);
                    });
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

        /*||||||||||| SENSOR ACITIVY |||||||||||*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            float ax=event.values[0];
            float ay=event.values[1];
            float az=event.values[2];

            // rotation from acceleration data
            pitchRollYaw = DataProcess.rotFromAcc(ax, ay, az);
        }
        tempIntRotAcc.setText("Rotation from accelerometer: " + Math.round(pitchRollYaw[0]) + ", " + Math.round(pitchRollYaw[1]) + ", " + Math.round(pitchRollYaw[2]));

        EWMA_filtered_values[0] = DataProcess.EMWA_filter(pitchRollYaw[0], (float) 0.5, EWMA_filtered_values[0]);
        EWMA_filtered_values[1] = DataProcess.EMWA_filter(pitchRollYaw[1], (float) 0.5, EWMA_filtered_values[1]);
        EWMA_filtered_values[2] = DataProcess.EMWA_filter(pitchRollYaw[2], (float) 0.5, EWMA_filtered_values[2]);
        Log.i(LOG_TAG, "EMWA filter: " + EWMA_filtered_values[0] + ", " + EWMA_filtered_values[1] + ", " + EWMA_filtered_values[2]);

        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            float gx = (float) Math.toDegrees(event.values[0]);
            float gy= (float) Math.toDegrees(event.values[1]);
            float gz= (float) Math.toDegrees(event.values[2]);

            dT = (System.currentTimeMillis() - timestamp) / (float) 1000; //units: seconds
            timestamp = System.currentTimeMillis(); // for storing old value

            //rotation from gyroscope
            rotFromGyro = DataProcess.rotFromGyroscope(gx, gy, gz, rotFromGyro[0], rotFromGyro[1], rotFromGyro[2], dT);
        }

        tempIntRotGyro.setText("Rotation from gyroscope: " + Math.round(rotFromGyro[0]) + ", " + Math.round(rotFromGyro[1]) + ", " + Math.round(rotFromGyro[2]));

        //Complimentary filter
        complimentary_filtered_values[0] = DataProcess.complimentaryFilter(rotFromGyro[0], pitchRollYaw[0], (float) 0.5, complimentary_filtered_values[0], dT);
        complimentary_filtered_values[1] = DataProcess.complimentaryFilter(rotFromGyro[1], pitchRollYaw[1], (float) 0.5, complimentary_filtered_values[1], dT);
        complimentary_filtered_values[2] = DataProcess.complimentaryFilter(rotFromGyro[2], pitchRollYaw[2], (float) 0.5, complimentary_filtered_values[2], dT);
        Log.i(LOG_TAG, "Complimentary filter: x = " + complimentary_filtered_values[0] + ", " + complimentary_filtered_values[1] + ", " + complimentary_filtered_values[2]);
        //Todo: figure out how to save values: list, array, ... ?
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // need to overwrite, when implement sensor stuff
        Toast.makeText(getApplicationContext(), "Sensor accuracy changed", Toast.LENGTH_SHORT).show();
        // Todo: check if we need to add something here if accuracy has changed
    }


    /*||||||||||| SERIALISATION |||||||||||*/
    private void deserialise() {
        // Method to deserialise input file
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