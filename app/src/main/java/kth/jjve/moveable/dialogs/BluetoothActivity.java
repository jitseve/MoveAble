package kth.jjve.moveable.dialogs;
/*
Gotten from github (Movesense 2.0) todo:do a proper source thingy
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kth.jjve.moveable.R;

public class BluetoothActivity extends AppCompatActivity {

    public static final String MOVESENSE = "Movesense";

    public static final int REQUEST_ENABLE_BT = 1000;
    public static final int REQUEST_ACCESS_LOCATION = 1001;

    public static String SELECTED_DEVICE = "Selected device";

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBTadapter;
    private boolean mScanning;
    private Handler bHandler;

    private TextView ScanInfo;

    private ArrayList<BluetoothDevice> mDeviceList;
    private BtDeviceAdapter mBtDeviceAdapter;

    private static final String LOG_TAG = "BluetoothActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bluetooth);

        mDeviceList = new ArrayList<>();
        bHandler = new Handler();

        RecyclerView rv = findViewById(R.id.rv_bluetooth_devices);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        mBtDeviceAdapter = new BtDeviceAdapter(mDeviceList,
                this::onDeviceSelected);
        rv.setAdapter(mBtDeviceAdapter);

        ScanInfo = findViewById(R.id.scanning_info);

        initBLE();

        mDeviceList.clear();
        scanForDevices(true);


    }

    @Override
    protected void onStart() {
        super.onStart();
        initBLE();
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanForDevices(false);
        mDeviceList.clear();
        mBtDeviceAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
    private void initBLE(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getApplicationContext(), "BLE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            int hasAccessLocation = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasAccessLocation != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_LOCATION);
            }
        }

        mBTadapter = BluetoothAdapter.getDefaultAdapter();

        if (mBTadapter == null || !mBTadapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void onDeviceSelected(int position) {
        BluetoothDevice selectedDevice = mDeviceList.get(position);
        Toast.makeText(getApplicationContext(), "Device is selected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra(SELECTED_DEVICE, selectedDevice);
        setResult(69, intent);
        finish();
    }

    private void scanForDevices(final boolean enable){
        final BluetoothLeScanner scanner = mBTadapter.getBluetoothLeScanner();
        if (enable) {
            if (!mScanning){
                bHandler.postDelayed(() -> {
                    if (mScanning){
                        mScanning = false;
                        scanner.stopScan(mScanCallback);
                        ScanInfo.setText(R.string.scanning_stopped);
                    }
                }, SCAN_PERIOD);
                mScanning = true;
                scanner.startScan(mScanCallback);
                ScanInfo.setText(R.string.scanning_started);
            }
        } else {
            if (mScanning){
                mScanning = false;
                scanner.stopScan(mScanCallback);
                ScanInfo.setText(R.string.scanning_started);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            final String name = device.getName();

            bHandler.post(() -> {
                if (name != null
                && name.contains(MOVESENSE)
                && !mDeviceList.contains(device)){
                    mDeviceList.add(device);
                    mBtDeviceAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, device.toString());
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i(LOG_TAG, "onBatchScanResult");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(LOG_TAG, "onScanFailed");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_LOCATION){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                this.finish();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}