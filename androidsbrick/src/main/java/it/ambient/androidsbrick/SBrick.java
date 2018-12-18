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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SBrick instance. Allows sending commands to SBrick GATT service / characteristic.
 *
 * @author Tomasz Wegrowski <tomasz.wegrowski+github@gmail.com>
 */
public class SBrick {
    private static final String TAG = "SBrick";

    public static final byte CHANNEL_A                  = 0x00;
    public static final byte CHANNEL_B                  = 0x01;
    public static final byte CHANNEL_C                  = 0x02;
    public static final byte CHANNEL_D                  = 0x03;
    public static final byte DIR_CLOCKWISE              = 0x00;
    public static final byte DIR_COUNTER_CLOCKWISE      = 0x01;

    private static final UUID SERVICE_REMOTE_CONTROL
            = UUID.fromString("4dc591b0-857c-41de-b5f1-15abda665b0c");
    private static final UUID CHARACTERISTIC_REMOTE_CONTROL_COMMANDS
            = UUID.fromString("2b8cbcc-0e25-4bda-8790-a15f53e6010f");
    private static final byte COMMAND_STOP              = 0x00;
    private static final byte COMMAND_DRIVE             = 0x01;

    private Context appContext;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt gatt;
    private BluetoothGattService serviceRemoteControl;
    private BluetoothGattCharacteristic characteristicRemoteControl;
    private boolean isConnected;
    private Handler timerHandler = new Handler();

    private byte preparedCommand;
    private Map<Byte, byte[]> channels;
    private byte[] commandStream;

    /**
     * Constructor
     * @param context application context
     * @param device connected BluetoothDevice
     */
    public SBrick(Context context, BluetoothDevice device) {
        appContext = context;
        bluetoothDevice = device;
        stop().channelAll();
        connectDevice();
    }

    /**
     * Start building DRIVE command. Attach channel() methods to which command applies to.
     * E.g.: drive().channel(...)...
     *
     * @return SBrick to chain channel methods
     */
    public SBrick drive() {
        Log.d(TAG, "drive");
        preparedCommand = COMMAND_DRIVE;
        channels = new HashMap<>();
        return this;
    }

    /**
     * Assigns DRIVE command to channel. Set direction and power.
     *
     * @param channel SBrick.CHANNEL_A..D
     * @param direction SBrick.DIR_CLOCKWISE or SBrick.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return SBrick to chain another channels or execute() method
     */
    public SBrick channel(byte channel, byte direction, byte power) {
        byte[] part = {channel, direction, power};
        channels.put(channel, part);
        return this;
    }

    /**
     * Assigns DRIVE command to all channels. Sets direction and power.
     *
     * @param direction SBrick.DIR_CLOCKWISE or SBrick.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return SBrick to chain execute() method
     */
    public SBrick channelAll(byte direction, byte power) {
        for (byte channel = 0; channel <= 3; channel++) {
            byte[] part = {channel, direction, power};
            channels.put(channel, part);
        }
        return this;
    }

    /**
     * Start building STOP command. Attach channel methods to which command applies to.
     * E.g. drive().channel(...)...
     *
     * @return SBrick to chain channel methods
     */
    public SBrick stop() {
        Log.d(TAG, "stop");
        preparedCommand = COMMAND_STOP;
        channels = new HashMap<>();
        return this;
    }


    /**
     * Assigns STOP command to channel.
     *
     * @param channel SBrick.CHANNEL_A..D
     * @return SBrick to chain another channels or execute() method
     */
    public SBrick channel(byte channel) {
        byte[] part = {channel};
        channels.put(channel, part);
        return this;
    }

    /**
     * Assigns STOP command to all channels.
     *
     * @return SBrick to chain execute() method
     */
    public SBrick channelAll() {
        for (byte channel = 0; channel <= 3; channel++) {
            byte[] part = {channel};
            channels.put(channel, part);
        }
        return this;
    }

    /**
     * Glues together command and channels values, then sends it to SBrick.
     * E.g. drive().channel(...).channel(...).execute();
     * @return execution status
     */
    public boolean execute() {
        Log.d(TAG, "execute");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (channels.isEmpty()) {
            Log.e(TAG, "Command has no set channels, use channel() or channelAll() methods.");
            return false;
        }
        try {
            outputStream.write(preparedCommand);
            for (byte[] part : channels.values()) {
                outputStream.write(part);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());       // TODO add callback to notify application
            return false;
        }
        Log.d(TAG, " - outputStream: " + outputStream.toString());
        commandStream = outputStream.toByteArray();
        characteristicRemoteControl.setValue(commandStream);
        return gatt.writeCharacteristic(characteristicRemoteControl);
    }

    private void connectDevice() {
        Log.d(TAG, "connectDevice");
        GattClientCallback gattClientCallback = new GattClientCallback();
        gatt = bluetoothDevice.connectGatt(appContext, false, gattClientCallback);
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }

    private void disconnectGattServer() {
        Log.d(TAG, "disconnectGattServer");
        timerHandler.removeCallbacks(timerCommandRepeater);
        setConnected(false);
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    /**
     * SBrick by default disconnects after few seconds if not receiving commands.
     * This Runnable repeats last command every 250ms to prevent disconnection.
     */
    private Runnable timerCommandRepeater = new Runnable() {
        @Override
        public void run() {
            if (isConnected) {
                execute();
            } else {
                Log.d(TAG, "TIMER skip - SBrick is disconnected");
            }
            timerHandler.postDelayed(this, 250);
        }
    };

    /**
     * Handles Bluetooth connection changes.
     */
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(TAG, "Connection Gatt failure status " + status);
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                Log.e(TAG,"Connection not GATT sucess status " + status);
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG,"Connected to device " + gatt.getDevice().getAddress());
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG,"Disconnected from device");
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
