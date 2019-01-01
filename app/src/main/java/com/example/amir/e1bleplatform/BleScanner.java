package com.example.amir.e1bleplatform;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class BleScanner {

    private MainActivity ma;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private long scanPeriod;
    private BluetoothLeScanner mLEScanner;

    BleScanner(MainActivity mainActivity, long scanPeriod){
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }

    boolean isScanning() {
        return mScanning;
    }

    public void start() {
        scanLeDevice(true);
    }

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
                    mLEScanner.stopScan(mScanCallback);
                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mLEScanner.startScan(mScanCallback);
        }
        else {
            mScanning = false;
            mLEScanner.stopScan(mScanCallback);
        }

    }


    // Device scan callback.
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
