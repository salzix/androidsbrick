package it.ambient.androidsbrick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optional wrapper for simplified bluetooth / SBrick discovery.
 * It's final product is HashMap of connected SBricks.
 *
 * @author Tomasz Wegrowski <tomasz.wegrowski+github@gmail.com>
 */
public class ConnectionHelper {
    private static final String TAG = "ConnectionHelper";
    private long scanPeriod         = 5000; // default scan time 5 seconds

    private Context applicationContext;
    private Map<String, BluetoothDevice> scanResults;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private ConnectionCallback sBrickCallback;
    private Handler scanHandler;
    private boolean isScanning = false;

    /**
     * Constructor
     *
     * @param context Application Context
     * @param callback ConnectionCallback
     */
    public ConnectionHelper(Context context, ConnectionCallback callback) {
        applicationContext = context;
        sBrickCallback = callback;
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager
                = (BluetoothManager) applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (!isBtAdapterAvailable()) {
            Log.e(TAG, "onCreate, missing bluetooth connection");
        }
    }

    /**
     * Set how long should search for bluetooth devices.
     * Default is 5 seconds (5000ms).
     *
     * @param period scan period in miliseconds (1s = 1000ms)
     */
    public void setScanPeriod(long period) {
        scanPeriod = period;
    }

    /**
     * Starts bluetooth device discovery. Executes scanComplete() method when finished.
     *
     * @return true is discovery started without problems
     */
    public boolean scanForSBricks() {
        Log.d(TAG, "scanForSBricks()");
        if (!isBtAdapterAvailable()) {
            Log.e(TAG, "scanForSBricks - missing permissions");
            return false;
        }
        if (isScanning) {
            Log.e(TAG, "scanForSBricks - already scanning");
            return false;
        }
        scanResults = new HashMap<>();
        scanCallback = new ConnectionHelper.BtLeScanCallback(scanResults);

        ScanFilter scanFilter = new ScanFilter.Builder()
                .build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);
        // scan for Bluetooth LE devices only
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        scanHandler = new Handler();
        scanHandler.postDelayed(this::stopScan, scanPeriod);
        isScanning = true;
        return true;
    }

    /**
     * Stops bluetooth device discovery and runs startComplete() method
     */
    public void stopScan() {
        Log.d(TAG, "stopScan");
        if (isScanning
                && bluetoothAdapter != null
                && bluetoothAdapter.isEnabled()
                && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
            scanComplete();
        }
        scanCallback = null;
        isScanning = false;
        scanHandler = null;
    }

    /**
     * Executed by scanForSBricks() after finished discovery.
     * Iterates through scanResults in search of SBrick devices.
     * Executes provided callback with SBrick HashMap
     */
    private void scanComplete() {
        Log.d(TAG, "scanComplete");
        Map<String, SBrick> sBricks = new HashMap<>();
        if (!scanResults.isEmpty()) {
            for (String deviceAddress : scanResults.keySet()) {
                BluetoothDevice device = scanResults.get(deviceAddress);
                Log.d(TAG, "device found - name: " + device.getName()
                        + ", address: " + device.getAddress());
                if (isSBrickDevice(device)) {
                    Log.d(TAG, "device is SBrick");
                    sBricks.put(device.getAddress(), new SBrick(applicationContext, device));
                }
            }
        } else {
            Log.w(TAG, "device scan results empty");
        }
        sBrickCallback.handleSBrickCollection(sBricks);
    }

    /**
     * Crude check if bluetooth device is SBrick.
     * This method should be refactored if better solution is found.
     *
     * @param device
     * @return true if device is SBrick
     */
    private boolean isSBrickDevice(BluetoothDevice device) {
        if (device.getName() == null) return false;
        return device.getName().equals("SBrick");
    }

    /**
     * Check if bluetooth is available for use. If not, provided callback is executed.
     * Callback should handle bluetooth inavailability.
     *
     * @return true if bluetooth adapter is available
     */
    private boolean isBtAdapterAvailable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "isBtAdapterAvailable");
            return sBrickCallback.handlePermissionRequests();
        }
        return true;
    }

    /**
     * Default discover bluetooth device callback.
     * Adds discovered devices to scanResults HashMap.
     */
    private class BtLeScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> scanResults;

        BtLeScanCallback(Map<String, BluetoothDevice> scanResultsParam) {
            scanResults = scanResultsParam;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed errorCode: " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            scanResults.put(deviceAddress, device);
        }
    }
}
