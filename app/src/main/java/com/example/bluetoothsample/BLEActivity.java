package com.example.bluetoothsample;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.ederdoski.simpleble.interfaces.BleCallback;
import com.ederdoski.simpleble.models.BluetoothLE;
import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.example.bluetoothsample.databinding.ActivityBleBinding;
import com.example.bluetoothsample.repository.ble.BleRoom;
import com.example.bluetoothsample.viewmodel.BleViewModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class BLEActivity extends AppCompatActivity {

    private static String TAG = BLEActivity.class.getSimpleName();
    private ActivityBleBinding activityBleBinding;
    private BleViewModel bleViewModel;
    private BluetoothLEHelper ble;
    private BleAdapter bleAdapter;
    private LocationManager locationManager;
    private ProgressDialog progressDialog;

    private BluetoothServer bluetoothServer;

    private static final int SCAN_PERIOD = 5000; //스캔주기 10초
    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private static final String CUSTOMSERVICE = "47a474ab-a474-4774-4774-1a0a12345678";
    private static final String CUSTOMCHARACTERISTIC = "a1234b12-4774-1a2b-ab47-1234a123ab10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBleBinding = DataBindingUtil.setContentView(this, R.layout.activity_ble);
        bleViewModel = ViewModelProviders.of(this).get(BleViewModel.class);

        //BluetoothLEHelper init
        ble = new BluetoothLEHelper(this);
        ble.setScanPeriod(SCAN_PERIOD);

        //스캔을 위한 위치확인서비스 설정
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        permissionCheck();

        activityBleBinding.searchStart.setOnClickListener(v -> {
            if (ble.isReadyForScan()) {
                scanningBle();
            }
        });

        activityBleBinding.searchStop.setOnClickListener(v -> {
            ble.disconnect();
        });

        activityBleBinding.bleRead.setOnClickListener(v -> {
            if (ble.isConnected()) {
                ble.read(CUSTOMSERVICE, CUSTOMCHARACTERISTIC);
            }
        });

        activityBleBinding.bleWrite.setOnClickListener(v -> {
            if (ble.isConnected()) {
                try {
                    ble.write(CUSTOMSERVICE, CUSTOMCHARACTERISTIC, URLEncoder.encode("hihi", "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });

        activityBleBinding.advertise.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bluetoothServer = new BluetoothServer(BLEActivity.this);
                    bluetoothServer.initServer();
                    bluetoothServer.deviceAdvertise();
                } else {
                    bluetoothServer.closeServer();
                }
            }
        });

        bleAdapter = new BleAdapter();
        activityBleBinding.recycler.setAdapter(bleAdapter);
        activityBleBinding.recycler.setLayoutManager(new LinearLayoutManager(this));

        bleViewModel.getListLiveData().observe(this, new Observer<List<BleRoom>>() {
            @Override
            public void onChanged(List<BleRoom> bleRooms) {
                bleAdapter.submitList(bleRooms);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleViewModel.deleteAll();
    }

    private void permissionCheck() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            new AlertDialog.Builder(this)
                    .setMessage("gps 권한이 필요합니다. 설정하시겠습니까?")
                    .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 설정 창을 띄운다
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", null).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (ble.isScanning()) {
            ble.disconnect();
            bleViewModel.deleteAll();
        }
        super.onBackPressed();
    }

    private void scanningBle() {

        Handler handler = new Handler();
        ble.scanLeDevice(true);
        progressDialog = new ProgressDialog(this);

        handler.postDelayed(() -> {

            for (BluetoothLE bluetoothLE : ble.getListDevices()) {
                bleViewModel.insert(new BleRoom(bluetoothLE.getMacAddress(), bluetoothLE.getDevice().getName()));
                if (!TextUtils.isEmpty(bluetoothLE.getDevice().getName())
                        && bluetoothLE.getDevice().getAddress().equals("FF:FF:FF:FF:FF:FF")) { //dummy macAddress

                    ble.connect(bluetoothLE.getDevice(), bleCallback);
                }

            }

        }, SCAN_PERIOD);
    }

    private BleCallback bleCallback = new BleCallback() {

        @Override
        public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onBleConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                progressDialog.dismiss();
                runOnUiThread(() -> {
                    Toast.makeText(BLEActivity.this, "Connected to GATT server.", Toast.LENGTH_SHORT).show();
                });
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> Toast.makeText(BLEActivity.this, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onBleServiceDiscovered(BluetoothGatt gatt, int status) {
            super.onBleServiceDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered received: " + status);
                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {

                    Log.d(TAG, "서비스 확인 : " + bluetoothGattService.getCharacteristics());
                }
            }
        }

        @Override
        public void onBleCharacteristicChange(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onBleCharacteristicChange(gatt, characteristic);
            Log.i(TAG, "onCharacteristicChanged Value: " + Arrays.toString(characteristic.getValue()));
        }

        @Override
        public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onBleWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, Arrays.toString(characteristic.getValue()));

                runOnUiThread(() -> Toast.makeText(BLEActivity.this, "onCharacteristicRead : " + Arrays.toString(characteristic.getValue()), Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onBleRead(gatt, characteristic, status);
            Log.i(TAG, "읽는 중 " + Arrays.toString(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "읽는 중 " + Arrays.toString(characteristic.getValue()));
                runOnUiThread(() -> Toast.makeText(BLEActivity.this, "onCharacteristicWrite Status : " + status, Toast.LENGTH_SHORT).show());
            }
        }
    };
}