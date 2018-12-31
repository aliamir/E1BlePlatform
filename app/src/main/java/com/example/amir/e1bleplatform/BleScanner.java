package com.arsenalmod.arsenalmod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import com.example.amir.e1bleplatform.MainActivity;

public class BleScanner {

    private MainActivity ma;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private long scanPeriod;

    public BleScanner(MainActivity mainActivity, long scanPeriod){
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;

        final BluetoothManager bluetoothManager = (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    public boolean isScanning() {
        return mScanning;
    }

    public void start() {

    }

    public void stop() {

    }

    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning){
            Toast.makeText(ma.getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ma.getApplicationContext(), "Stop Scanning...", Toast.LENGTH_SHORT).show();

                    mScanning = false;
                    //mLEScanner.stopScan(mScanCallback);
                    //ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            //mLEScanner.startScan(mScanCallback);
        }
    }

    // Device scan callback.
    ScanCallback mScanCallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result)  {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ma.addDevice( result.getDevice() );
                        }
                    });
                }
            };
}
