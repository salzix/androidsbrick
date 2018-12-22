# androidsbrick
Android library for making apps [communicating with SBrick](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol) little easier. Allows control of multiple SBricks.

Current status: working, unstable.

### Prerequisites
* SBrick or SBrick Plus with [firmware version 17+](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol)
* Device with Android 6.0 (Marshmallow) or newer (API level 23)
* [Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth-le) support on Android device
* your favorite Android IDE

### Installing

Clone or download project. It consists of two parts:
* **androidsbrick** library
* **demo** application

You can compile and run project demo to see it working. For your project you'll need only *androidsbrick* folder. Copy it to your project as a module and configure project to use it as dependency.

Maven and Gradle installation - in progress.

## Usage
Library comes with optional ConnectionHelper to handle SBrick connection easier. This example uses helper, but if you are familiar with Bluetooth LE service discovery you can set up SBrick instances on your own:
```
    // your BT LE disovery here
    SBrick sbrick = new SBrick(context, bluetoothDevice)
```

### Set required permissions
In *AndroidManifest.xml*:
```
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```
Note: *ACCESS_FINE_LOCATION* is needed for Bluetooth device discovery.

### Declare variables
```
    private ConnectionHelper connectionHelper;      // optional helper used in this example
    private Map<String, SBrick> sbricks;            // will contain discovered and connected SBricks
    private SBrick sbrick;                          // selected SBrick
```
### Implement ConnectionCallback
It will be called when device discovery finishes scanning for SBricks or app gets asked for permissions.
Fills variable *sbricks* with *SBrick* objects.
```
public class MainActivity extends AppCompatActivity
        implements ConnectionCallback {
        
    public void handleSBrickCollection(Map<String, SBrick> sbricks) {
        // optimistic scenario - at least one SBrick is found
        sbrick = sbricks.entrySet().iterator().next().getValue()
    }

    public boolean handlePermissionRequests() {
        // handle Bluetooth permission requests, see demo
    }
```

### Start SBrick discovery
After few seconds it calls callback above.
```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        connectionHelper = new ConnectionHelper(this, this);  // params are app context and callback
        connectionHelper.scanForSBricks();                    // find SBricks, 5 seconds discovery.
    }
```

### Control SBrick
Send *drive* command to two motors:
```
    sbrick.drive()
        .channel(SBrick.CHANNEL_A, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
        .execute();
```

## API reference
[SBrick protocol](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol) allows sending one command to multiple (1-4) channels in one request. You can set different params (power, direction) for each channel.

### Helper constants
They are used as channel params for easier to read code. You can omit them and use Int values.

*Channel selectors:*
* **SBrick.CHANNEL_A**             = 0
* **SBrick.CHANNEL_B**             = 1
* **SBrick.CHANNEL_C**             = 2
* **SBrick.CHANNEL_D**             = 3

*Direction selectors:*
* **SBrick.DIR_CLOCKWISE**         = 0
* **SBrick.DIR_COUNTER_CLOCKWISE** = 1

### drive() command
Drives motors on selected channels. You can chain up to four of them. This command can be also used to control lights.

Channel params are: *channel, direction, power (byte 0-255)*
```
   sbrick.drive()
       .channel(SBrick.CHANNEL_A, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
       .channel(SBrick.CHANNEL_B, SBrick.DIR_COUNTER_CLOCKWISE, (byte) 0xBB)
       .execute()
```

### stop() command
Stops rotation/power on selected channels. You can chain up to 4 of them.
```
   sbrick.stop()
       .channel(SBrick.CHANNEL_A)
       .channel(SBrick.CHANNEL_B)
       .channel(SBrick.CHANNEL_C)
       .channel(SBrick.CHANNEL_D)
       .execute()
```

### Keep in mind
Sending command on one channel doesn't reset state of other channels. E.g. if you first send command to drive on all channels clockwise, and then send drive counter-clockwise on channel A, the rest will keep rotating clockwise. In second command you should also set channel params with 0 values to stop them.

## Versioning

I use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/salzix/androidsbrick/tags). 

## Authors

* **Tomasz Węgrowski** - *Initial work* - [salzix](https://github.com/salzix)

See also the list of [contributors](https://github.com/salzix/androidsbrick/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

SBrick trademark is owned by [Vengit](https://www.sbrick.com/)

