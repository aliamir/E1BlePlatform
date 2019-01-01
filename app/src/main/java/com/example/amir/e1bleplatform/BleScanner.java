package com.example.amir.e1bleplatform;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Description:
 * The type Ble scanner.
 */
public class BleScanner {

    private MainActivity ma;
    //private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private long scanPeriod;
    private BluetoothLeScanner mBleScanner;

    /**
     * Description:
     * Instantiates a new Ble scanner.
     *
     * @param mainActivity the main activity
     * @param scanPeriod   the scan period
     */
    BleScanner(MainActivity mainActivity, long scanPeriod){
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }

    /**
     * Description:
     * Returns the current state of scanning.
     *
     * @return boolean boolean
     */
    boolean isScanning() {
        return mScanning;
    }

    /**
     * Description:
     * Public method to start scanning for BLE devices.
     */
    public void start() {
        scanLeDevice(true);
    }

    /**
     * Description:
     * Public method to stop scanning for BLE devices.
     */
    void stop() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning){
            // Toast.makeText(ma.getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBleScanner.stopScan(mScanCallback);
                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBleScanner.startScan(mScanCallback);
        }
        else {
            mScanning = false;
            mBleScanner.stopScan(mScanCallback);
        }

    }


    /**
     * Description:
     * When a new device is found, this callback is called which allows the main activity to save
     * the BLE device.
     */
    private ScanCallback mScanCallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result)  {
                    Log.i("callbackType", String.valueOf(callbackType));
                    Log.i("result", result.toString());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ma.addDevice( result.getDevice() );
                        }
                    });
                }
            };
}
