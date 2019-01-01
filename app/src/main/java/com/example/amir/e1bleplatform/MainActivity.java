package com.example.amir.e1bleplatform;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int  SCAN_PERIOD = 5000;
    private static final String TAG = "MainActivity";
    private Button scanButton;
    private ProgressBar scanProgressBar;
    private boolean scanClicked = true;
    private BluetoothGatt mGatt;

    // Bluetooth Objects
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mLEScanner;
    private ScanRecord mScanRecord;
    private BleScanner mBleScanner;

    // Thread for scanning
    private Handler mHandler;

    // Device info and lists
    private HashMap<String, BluetoothDevice> mBleDevicesHashMap;
    private ListView mListOfDevicesView;
    private ArrayList<BleDevice> mBleDeviceList;
    private TwoItemListAdapter BleDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        // initialize mLeScanner
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Setup progress bar
        scanProgressBar = findViewById(R.id.scan_progress_bar);

        // Setup scan button
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( !mBleScanner.isScanning() )
                {
                    // If BT is off, request to turn it on
                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        scanProgressBar.setVisibility(View.INVISIBLE);
                    } else {
                        // Ask for location permission before scanning for BT devices. Google is weird.
                        int MY_PERMISSIONS_REQUEST_BTLE = 0;
                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                ACCESS_FINE_LOCATION);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            //Request access
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_BTLE);
                        }
                    }
                    startScan();
                }
                else{
                    stopScan();
                }
            }
        });

        // List of BLE devices found
        mListOfDevicesView = findViewById(R.id.list_bt_devices);

        // Populate ListArray with dummy values
        mBleDeviceList = new ArrayList<>();
        mBleDevicesHashMap = new HashMap<>();

        // Show in ListView
        BleDeviceListAdapter = new TwoItemListAdapter(this, R.layout.list_devices, mBleDeviceList);
        mListOfDevicesView.setAdapter(BleDeviceListAdapter);

        mBleScanner = new BleScanner(this, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addDevice(final BluetoothDevice device){

        String address = device.getAddress();

        if (!mBleDevicesHashMap.containsKey(address)) {
            // Add to hashmap
            mBleDevicesHashMap.put(address, device);

            // add to BleDevice list
            BleDevice deviceToList = new BleDevice(device.getName(), address);
            mBleDeviceList.add(deviceToList);
        }
        // Update the list
        BleDeviceListAdapter.notifyDataSetChanged();
    }

    public void startScan() {
        // Set the text of the button
        scanButton.setText(R.string.stop_scan_button_text);

        // Clear the hashmap
        mBleDevicesHashMap.clear();

        // Clear the list
        mBleDeviceList.clear();

        // Update the list
        BleDeviceListAdapter.notifyDataSetChanged();

        // Start scanning
        mBleScanner.start();
    }

    public void stopScan() {
        // Set the text of the button
        scanButton.setText(R.string.start_scan_button_text);

        mBleScanner.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop scanning
        stopScan();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop scanning
        stopScan();
    }
}
