package it.ambient.androidsbrick;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.UUID;

import it.ambient.androidsbrick.command.SBrickCommand;
import it.ambient.androidsbrick.command.StopCommand;

/**
 * SBrick instance. Allows sending commands to SBrick GATT service and characteristic.
 *
 * @author Tomasz Wegrowski <tomasz.wegrowski+github@gmail.com>
 */
public class SBrick {
    private static final String TAG = "SBrick";
    private static final UUID SERVICE_REMOTE_CONTROL
            = UUID.fromString("4dc591b0-857c-41de-b5f1-15abda665b0c");
    private static final UUID CHARACTERISTIC_REMOTE_CONTROL_COMMANDS
            = UUID.fromString("2b8cbcc-0e25-4bda-8790-a15f53e6010f");
    private static final int COMMAND_BUFFER_SIZE = 3;

    private Context appContext;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt gatt;
    private BluetoothGattService serviceRemoteControl;
    private BluetoothGattCharacteristic characteristicRemoteControl;
    private boolean isConnected;
    private Handler timerHandler = new Handler();

    private ArrayDeque<byte[]> preparedStreams = new ArrayDeque<>();
    private byte[] lastStream;

    /**
     * Constructor
     * @param context application context
     * @param device connected BluetoothDevice
     */
    public SBrick(Context context, BluetoothDevice device) {
        appContext = context;
        bluetoothDevice = device;
        StopCommand stopCommand = new StopCommand();
        stopCommand.allChannels();
        execute(stopCommand);
        connectDevice();
    }

    /**
     * Returns SBrick device id
     * @return String SBrick id
     */
    public String getId() {
        if (bluetoothDevice == null) return null;
        return bluetoothDevice.getAddress();
    }

    /**
     * Sets command for execution by SBrick
     * @param sbrickCommand command to execute
     * @return execution status
     */
    public boolean execute(SBrickCommand sbrickCommand) {
//        Log.d(TAG, "execute");
        try {
            if (preparedStreams.size() < COMMAND_BUFFER_SIZE) {
                preparedStreams.addFirst(sbrickCommand.getPreparedStream());
            } else {
                Log.w(TAG, "SBrick buffer of 3 consecutive commands exceeded, skipping command.");
            }
        } catch (IOException e) {
            // Should never happen, see: https://stackoverflow.com/questions/6271934/java-ioexception-when-writing-to-a-bytearrayoutputstream
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    /**
     * Sends prepared command to SBrick, used by timerCommandRepeater
     * @return execution status
     */
    private boolean timedExecute() {
//        Log.d(TAG, " - timedExecute: " + preparedStream.toString());
        if (!preparedStreams.isEmpty()) {
            lastStream = preparedStreams.pollLast();
        }
        characteristicRemoteControl.setValue(lastStream);
        return gatt.writeCharacteristic(characteristicRemoteControl);
    }

    private void connectDevice() {
//        Log.d(TAG, "connectDevice");
        GattClientCallback gattClientCallback = new GattClientCallback();
        gatt = bluetoothDevice.connectGatt(appContext, false, gattClientCallback);
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }

    private void disconnectGattServer() {
//        Log.d(TAG, "disconnectGattServer");
        timerHandler.removeCallbacks(timerCommandRepeater);
        setConnected(false);
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    /**
     * SBrick by default disconnects after few seconds if not receiving commands.
     * This Runnable repeats last command every 200ms to prevent disconnection.
     */
    private Runnable timerCommandRepeater = new Runnable() {
        @Override
        public void run() {
            if (isConnected) {
                timedExecute();
            } else {
                Log.d(TAG, "TIMER skip - SBrick is disconnected");
            }
            timerHandler.postDelayed(this, 200);
        }
    };

    /**
     * Handles Bluetooth connection changes.
     */
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(TAG, "Connection GATT_FAILURE status " + status);
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                Log.e(TAG,"Connection not GATT_SUCCESS status " + status);
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"Connected to device " + gatt.getDevice().getAddress());
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG,"Disconnected from device");
                disconnectGattServer();
            }
        }

        /**
         * Services discovered - connect to RC service and create characteristic
         * @param gatt BluetoothGatt callback
         * @param status BluetoothGatt.<status>
         */
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            Log.d(TAG, "onServicesDiscovered - services discovered");
            serviceRemoteControl = gatt.getService(SERVICE_REMOTE_CONTROL);
            characteristicRemoteControl = serviceRemoteControl.getCharacteristic(CHARACTERISTIC_REMOTE_CONTROL_COMMANDS);
            timerHandler.postDelayed(timerCommandRepeater, 0);
        }
    }
}
