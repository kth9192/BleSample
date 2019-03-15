package com.example.bluetoothsample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothServer {

    private static String TAG = BluetoothServer.class.getSimpleName();
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGattService service;
    private BluetoothLeAdvertiser advertiser;

    public BluetoothServer(Context context) {
        this.context = context;
    }

   public void initServer(){
       bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
       bluetoothAdapter = bluetoothManager.getAdapter();
       bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallback);
       service = new BluetoothGattService(BleProfile.UART_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

       //add a read characteristic.
       BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(BleProfile.TX_READ_CHAR , BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
       BluetoothGattCharacteristic characteristicWrite1 = new BluetoothGattCharacteristic(BleProfile.RX_WRITE_CHAR , BluetoothGattCharacteristic.PROPERTY_READ,  BluetoothGattCharacteristic.PERMISSION_READ);
       BluetoothGattCharacteristic characteristicWrite2 = new BluetoothGattCharacteristic(BleProfile.RX_WRITE_CHAR , BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);

       BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(BleProfile.TX_READ_CHAR_DESC, BleProfile.DESCRIPTOR_PERMISSION);
       characteristicWrite1.addDescriptor(descriptor);

       service.addCharacteristic(characteristicRead);
       service.addCharacteristic(characteristicWrite1);
       service.addCharacteristic(characteristicWrite2);

       bluetoothGattServer.addService(service);
   }

   public void deviceAdvertise(){
       advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

       AdvertiseSettings settings = new AdvertiseSettings.Builder()
               .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
               .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
               .setConnectable(true)
               .build();

//       ParcelUuid pUuid = new ParcelUuid(UUID.fromString(context.getString(R.string.ble_uuid)));

       AdvertiseData data = new AdvertiseData.Builder()
               .setIncludeDeviceName(false)
               .addServiceUuid(new ParcelUuid(BleProfile.UART_SERVICE))
               .build();

       advertiser.startAdvertising(settings, data, advertisingCallback);
   }

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "광고중");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };

    public void closeServer(){
        if (bluetoothGattServer == null) {
            return;
        }
        advertiser.stopAdvertising(advertisingCallback);
        bluetoothGattServer.close();
    }

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, BleProfile.getStateDescription(newState));
            bluetoothGattServer.connect(device, true);

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(TAG, "서비스 추가 " + status + ":" +  service.getCharacteristic(BleProfile.RX_WRITE_CHAR).getUuid());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (BleProfile.TX_READ_CHAR.equals(characteristic.getUuid())) {
                Log.d(TAG, "읽기요청 접근장비 " + device.getName());
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if ( BleProfile.RX_WRITE_CHAR.equals(characteristic.getUuid())) {

                if (responseNeeded) {
                    bluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                    Log.d(TAG, "Received  data on " + characteristic.getUuid().toString());
                    Log.d(TAG, "Received data" + new String(value, StandardCharsets.UTF_8));

                }
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            bluetoothGattServer.connect(device, true);
        }

        @Override
        public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(device, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyRead(device, txPhy, rxPhy, status);
        }
    };
}
