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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import android.widget.Adapter;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int  SCAN_PERIOD = 5000;
    private static final String TAG = "MainActivity";
    private ListView mListOfDevicesView;
    List<BluetoothDevice> deviceList = new ArrayList<>();
    private Button scanButton;
    private ProgressBar scanProgressBar;
    private boolean scanClicked = true;
    private BluetoothGatt mGatt;

    // Bluetooth Objects
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mLEScanner;
    private ScanRecord mScanRecord;


    // Thread for scanning
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        // List of BLE devices found
        mListOfDevicesView = findViewById(R.id.list_bt_devices);

        //HandleBtleScan();

        /******* TESTING ********/
        // Populate ListArray with dummy values
        ArrayList<BleDevice> list = new ArrayList<BleDevice>();
        BleDevice device = new BleDevice("Amir", "50515 Woodford Dr.");
        list.add(device);
        // Show in ListView
        TwoItemListAdapter adaptlist = new TwoItemListAdapter(this, R.layout.list_devices, list);
        mListOfDevicesView.setAdapter(adaptlist);
        /******* TESTING ********/
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

    private void HandleBtleScan() {
        // Setup BT manager and adapter to use BT
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        // initialize mLeScanner
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Create thread to handle scanning
        mHandler = new Handler();

        scanProgressBar = (ProgressBar)findViewById(R.id.scan_progress_bar);

        // Scan Button Pressed
        scanButton = (Button)findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

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

                    if (scanClicked) {

                        // Start Progress Bar Spinning
                        scanProgressBar.setVisibility(View.VISIBLE);
                        scanButton.setText(R.string.stop_scan_button_text);

                        // Clear old list of devices
                        deviceList.clear();

                        // Start a scan
                        //scanForDevices(true);

                    } else {
                        // Stop Progress Bar Spinning
                        scanProgressBar.setVisibility(View.GONE);
                        scanButton.setText(R.string.start_scan_button_text);

                        // Stop a scan
                        //scanForDevices(false);
                    }
                    scanClicked ^= true;
                }
            }
        });
    }

    public void addDevice(final BluetoothDevice device){

    }
}
