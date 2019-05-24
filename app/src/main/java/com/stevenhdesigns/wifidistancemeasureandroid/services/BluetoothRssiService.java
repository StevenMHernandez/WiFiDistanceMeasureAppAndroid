package com.stevenhdesigns.wifidistancemeasureandroid.services;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.nio.charset.Charset;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


public class BluetoothRssiService {
    RxBleClient rxBleClient;
    String SERVICE_UUID_STRING = "D4627123-5555-9999-73B7-FEE516F96870";
    //    var centralManager: CentralManager! = CentralManager(queue: .main)
//    var peripheralManager: PeripheralManager! = PeripheralManager(queue: .main)
//    var centralManagerObserver: Observable<ScannedPeripheral>!
//    var bleService:CBMutableService!
//    var disposablePeripheralScanner: Disposable!
//    var disposablePeripheralManagerAdvertise: Disposable!
//    var peripheralDatasetIndex = [UUID: Int]()
//    var peripheralConnected = [UUID: Int]()
    public BluetoothRssiDelegate delegate;
    Disposable scanSubscription;

    public void setupBLE(Activity activity, Context context) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // pass
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 9);
            }
        } else {
            begin(context);
        }
    }

    public void permissionGranted(Context context) {
        begin(context);
    }

    void begin(Context context) {
        /*
         * Begin Advertising
         */

        // @SEE: https://code.tutsplus.com/tutorials/how-to-advertise-android-as-a-bluetooth-le-peripheral--cms-25426
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(SERVICE_UUID_STRING));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, "Data".getBytes(Charset.forName("UTF-8")))
                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, data, advertisingCallback);

        /*
         * Begin Scanning
         */
        rxBleClient = RxBleClient.create(context);
        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
        )
                .subscribe(
                        scanResult -> {
                            if (scanResult.getBleDevice().getName() != null && scanResult.getBleDevice().getName().equals("BLELOC")) {
                                this.delegate.bluetoothRssi((double) scanResult.getRssi());
                            }
                        },
                        throwable -> {
                            Log.e("BLE", String.valueOf(throwable));
                        }
                );
    }
}
