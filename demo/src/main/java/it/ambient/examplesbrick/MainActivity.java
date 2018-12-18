package it.ambient.examplesbrick;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import it.ambient.androidsbrick.ConnectionHelper;
import it.ambient.androidsbrick.SBrick;
import it.ambient.androidsbrick.ConnectionCallback;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, ConnectionCallback {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_FINE_LOCATION = 2;
    private ConnectionHelper connectionHelper;
    private Map<String, SBrick> sbricks;
    private String selectedSBrickId;

    private TextView textStatus;
    private Button buttonStartScan;
    private Button buttonStopScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (!hasPermissions()) {
            Log.e(TAG, "onCreate, missing permissions");
            finish();
        }
        setContentView(R.layout.activity_main);
        buttonStartScan = findViewById(R.id.buttonStartScan);
        buttonStartScan.setOnClickListener(this);
        buttonStopScan = findViewById(R.id.buttonStopScan);
        buttonStopScan.setOnClickListener(this);
        buttonStopScan.setEnabled(false);
        findViewById(R.id.buttonDrive_A).setOnClickListener(this);
        findViewById(R.id.buttonDriveBack_A).setOnClickListener(this);
        findViewById(R.id.buttonStop_A).setOnClickListener(this);

        findViewById(R.id.buttonDrive_B).setOnClickListener(this);
        findViewById(R.id.buttonDriveBack_B).setOnClickListener(this);
        findViewById(R.id.buttonStop_B).setOnClickListener(this);

        findViewById(R.id.buttonDrive_C).setOnClickListener(this);
        findViewById(R.id.buttonDriveBack_C).setOnClickListener(this);
        findViewById(R.id.buttonStop_C).setOnClickListener(this);

        findViewById(R.id.buttonDrive_D).setOnClickListener(this);
        findViewById(R.id.buttonDriveBack_D).setOnClickListener(this);
        findViewById(R.id.buttonStop_D).setOnClickListener(this);

        textStatus = findViewById(R.id.textStatus);
        // step one - find SBrick devices and receive them in callback
        connectionHelper = new ConnectionHelper(this, this);
    }

    @Override
    protected void onResume() {
    super.onResume();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "Missing required Bluetooth LE support.");
            Toast.makeText(this, "Device is missing required Bluetooth LE support.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Step three - send commands to SBrick
     * @param v View
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartScan:
                Log.d(TAG, "onClick - buttonStartScan");
                buttonStartScan.setEnabled(false);
                buttonStopScan.setEnabled(true);
                textStatus.setText("Discovering SBricks...");
                connectionHelper.scanForSBricks();
                break;
            case R.id.buttonStopScan:
                Log.d(TAG, "onClick - buttonStopScan");
                buttonStartScan.setEnabled(true);
                buttonStopScan.setEnabled(false);
                textStatus.setText("Discovering stopped.");
                connectionHelper.stopScan();
                break;
            case R.id.buttonDrive_A:
                Log.d(TAG, "onClick - buttonDrive_A");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_A, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonDriveBack_A:
                Log.d(TAG, "onClick - buttonDriveBack_A");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_A, SBrick.DIR_COUNTER_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonStop_A:
                Log.d(TAG, "onClick - buttonStop_A");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .stop()
                        .channel(SBrick.CHANNEL_A)
                        .execute();
                break;
            case R.id.buttonDrive_B:
                Log.d(TAG, "onClick - buttonDrive_B");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_B, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;

            case R.id.buttonDriveBack_B:
                Log.d(TAG, "onClick - buttonDriveBack_B");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_B, SBrick.DIR_COUNTER_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonStop_B:
                Log.d(TAG, "onClick - buttonStop_B");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .stop()
                        .channel(SBrick.CHANNEL_B)
                        .execute();
                break;

            case R.id.buttonDrive_C:
                Log.d(TAG, "onClick - buttonDrive_C");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_C, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonDriveBack_C:
                Log.d(TAG, "onClick - buttonDriveBack_C");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_C, SBrick.DIR_COUNTER_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonStop_C:
                Log.d(TAG, "onClick - buttonStop_C");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .stop()
                        .channel(SBrick.CHANNEL_C)
                        .execute();
                break;

            case R.id.buttonDrive_D:
                Log.d(TAG, "onClick - buttonDrive_D");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_D, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonDriveBack_D:
                Log.d(TAG, "onClick - buttonDriveBack_D");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .drive()
                        .channel(SBrick.CHANNEL_D, SBrick.DIR_COUNTER_CLOCKWISE, (byte) 0xFF)
                        .execute();
                break;
            case R.id.buttonStop_D:
                Log.d(TAG, "onClick - buttonStop_D");
                if (sbricks.isEmpty()) break;
                sbricks.get(selectedSBrickId)
                        .stop()
                        .channel(SBrick.CHANNEL_D)
                        .execute();
                break;
        }
    }

    /**
     * Step two - add SBricks to collection
     * @param sBrickCollection from ConnectionHelper
     */
    @Override
    public void handleSBrickCollection(Map<String, SBrick> sBrickCollection) {
        Log.d(TAG, "handleSBrickCollection");
        buttonStartScan.setEnabled(true);
        buttonStopScan.setEnabled(false);
        if (sBrickCollection.isEmpty()) {
            textStatus.setText("SBrick(s) not found.");
            Log.w(TAG, "SBrick(s) not found.");
            return;
        }
        sbricks = sBrickCollection;
        selectedSBrickId = sbricks.keySet().iterator().next();
        textStatus.setText("Found " + sbricks.size() + " SBrick(s). Using first found: " + selectedSBrickId);
    }

    private boolean hasPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "permission request ACCESS_FINE_LOCATION");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, PERMISSION_FINE_LOCATION);
            return false;
        } else {
            Log.d(TAG, "permission ACCESS_FINE_LOCATION GRANTED");
        }
        return true;
    }

    @Override
    public boolean handlePermissionRequests() {
        Log.d(TAG, "requestBluetoothEnable");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult() PERMISSION_GRANTED");
                } else {
                    Log.d(TAG, "onRequestPermissionsResult() PERMISSION_DENIED");
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
