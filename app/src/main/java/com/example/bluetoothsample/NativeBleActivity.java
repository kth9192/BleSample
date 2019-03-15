package com.example.bluetoothsample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothsample.databinding.ActivityTestBinding;
import com.example.bluetoothsample.repository.test.TestRoom;
import com.example.bluetoothsample.viewmodel.TestViewModel;

import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class NativeBleActivity extends AppCompatActivity {

    private static String TAG = NativeBleActivity.class.getSimpleName();
    private ActivityTestBinding activityTestBinding;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt gatt;
    private TestViewModel testViewModel;
    private int SCAN_PERIOD = 5000;

    private BluetoothLeScanner leScanner;
    private static final String CUSTOMSERVICE = "47a474ab-a474-4774-4774-1a0a12345678";
    private static final String CUSTOMCHARACTERISTIC = "a1234b12-4774-1a2b-ab47-1234a123ab10";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTestBinding = DataBindingUtil.setContentView(this, R.layout.activity_test);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        testViewModel = ViewModelProviders.of(this).get(TestViewModel.class);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        bleCheck(bluetoothAdapter);

        UUID[] uuid = new UUID[1];
        uuid[0] = UUID.fromString("47A474AB-A474-4774-4774-1A0A12345678");

        activityTestBinding.scanStart.setOnClickListener(v -> {
            leScanner.startScan(leScanCallback);

            new Handler().postDelayed(()->{
                leScanner.stopScan(leScanCallback);
                testViewModel.deleteAll();
            }, SCAN_PERIOD);
        });

        activityTestBinding.scanStop.setOnClickListener(v -> {
            leScanner.stopScan(leScanCallback);
            testViewModel.deleteAll();
        });

        NativeAdapter adapter = new NativeAdapter();
        activityTestBinding.recycler.setAdapter(adapter);
        activityTestBinding.recycler.setLayoutManager(new LinearLayoutManager(this));

        testViewModel.getListLiveData().observe(this, new Observer<List<TestRoom>>() {
            @Override
            public void onChanged(List<TestRoom> testRooms) {
                adapter.submitList(testRooms);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testViewModel.deleteAll();
    }

    private void bleCheck(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            //블루투스를 지원하지 않으면 장치를 끈다
            Toast.makeText(this, "블루투스를 지원하지 않는 장치입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            //연결 안되었을 때
            if (!bluetoothAdapter.isEnabled()) {
                //블루투스 연결
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(i);
            }
        }
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!TextUtils.isEmpty(result.getDevice().getName()) && result.getDevice().getName().equals("I hate Bluetooth")) {
                gatt = result.getDevice().connectGatt(getApplicationContext(), true, gattCallback);
                testViewModel.insert(new TestRoom(result.getDevice().getAddress(), result.getDevice().getName()));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "scan failed " + errorCode);
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == STATE_CONNECTED) {
                gatt.discoverServices();
                Log.d(TAG, "서비스발견" + gatt.discoverServices());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                List<BluetoothGattService> gattServices = gatt.getServices();
                Log.d("onServicesDiscovered", "Services count: " + gattServices.size());

                for (BluetoothGattService gattService : gattServices) {
                    String serviceUUID = gattService.getUuid().toString();
                    Log.d("onServicesDiscovered", "서비스 uuid " + serviceUUID);

                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()){
                        Log.d(TAG, "캐릭터 uuid : " + characteristic.getUuid());
                    }
                }

//                BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(CUSTOMSERVICE))
//                        .getCharacteristic(UUID.fromString(CUSTOMCHARACTERISTIC));
//
//                gatt.setCharacteristicNotification(characteristic, true);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.readCharacteristic(characteristic);

                Log.d(TAG, "캐릭터 읽는 중" + characteristic.getUuid());

                List<BluetoothGattService> gattServices = gatt.getServices();

                for (BluetoothGattService gattService : gattServices) {
                    String UUID = gattService.getCharacteristics().toString();
                    Log.d(TAG , "Service uuid " + UUID);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "캐릭터 쓰는 중");

                if(characteristic.getUuid().toString().equals(CUSTOMCHARACTERISTIC)){
                    Log.d(TAG, "SEND TEXT TO SERVER");
                    String sendValue = "hihi";
                    byte[] b = hexStringToByteArray(sendValue);
//                            characteristic.setValue( 11,  BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, 0);
                    characteristic.setValue(b);
                    gatt.writeCharacteristic(characteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
