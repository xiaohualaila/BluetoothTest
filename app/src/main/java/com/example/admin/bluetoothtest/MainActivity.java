package com.example.admin.bluetoothtest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.admin.bluetoothtest.bluetooth.IBeacon;
import com.example.admin.bluetoothtest.bluetooth.ScanResultAnalysis;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "sss";

    private IBeacon iBeacon;
    private BluetoothAdapter mBluetoothAdapter;
    private long mStartTime;
    private IntentFilter mIntentFilter;
    private boolean mBluetoothState = true;//record bluetooth state when launch app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter = new IntentFilter( BluetoothAdapter.ACTION_STATE_CHANGED );
//        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
//        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
//        mIntentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
//        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
        registerReceiver( mBroadcastReceiver, mIntentFilter );

    }

    @Override
    protected void onResume() {
        if (checkVersion() && checkBluetooth())
            scanDevice();
        mBluetoothAdapter.startDiscovery();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopScan();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mBluetoothAdapter != null && mBluetoothState == false)
            mBluetoothAdapter.disable();
        unregisterReceiver( mBroadcastReceiver );
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i( TAG, "Receive broadcast" + intent.getAction() + ":" + mBluetoothAdapter.isEnabled() );
            if (intent.getAction().equals( BluetoothAdapter.ACTION_STATE_CHANGED )) {
                if (mBluetoothAdapter.isEnabled()) {
                    Log.i( TAG, "time:" + String.valueOf( System.currentTimeMillis() - mStartTime ) );
                    //TODO:Thread
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            scanDevice();
                        }
                    } ).start();
                    mStartTime = 0;
                }
            }
        }
    };


    @SuppressLint("NewApi")
    private void scanDevice() {
        boolean startLeScan = mBluetoothAdapter.startLeScan( mLeScanCallback );
        Log.i( TAG, "startSacn " + startLeScan );
    }

    @SuppressLint("NewApi")
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    final IBeacon ibeacon = ScanResultAnalysis.formatScanData( device, rssi, scanRecord );
                    Log.i( TAG,  "蓝牙 " + "++++++++" );
                    if (ibeacon == null)
                        return;

                       Log.i( TAG,  "蓝牙 " + ibeacon );
                }
            };

    @SuppressLint("NewApi")
    private void stopScan() {
        mBluetoothAdapter.stopLeScan( mLeScanCallback );
    }

    private boolean checkVersion() {
        if (Build.VERSION.SDK_INT < 18) {
            Toast.makeText( this, "Android版本过低", Toast.LENGTH_LONG ).show();
            return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    private boolean checkBluetooth() {
        if (!getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE )) {
            Toast.makeText( this, "蓝牙设备不支持", Toast.LENGTH_LONG ).show();
            return false;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText( this, "蓝牙设备不支持", Toast.LENGTH_LONG ).show();
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i( TAG, "BT not enabled yet" );
            mStartTime = System.currentTimeMillis();
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            mBluetoothState = false;
            mBluetoothAdapter.enable();
            return false;
        }
        return true;
    }
}
