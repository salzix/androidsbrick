# androidsbrick
Android library to control [SBrick](https://www.sbrick.com/sbrick). Allows control of multiple SBricks. See [demo application](https://github.com/salzix/androidsbrickdemo) of library usage.

Current status: provides remote control. Doesn't support quickdrive, authentication, status reads.

### Prerequisites
* SBrick or SBrick Plus with [firmware version 17+](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol)
* Device with Android 6.0 (Marshmallow) or newer (API level 23)
* [Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth-le) support on Android device
* your favorite Android IDE

## Usage

### Add gradle dependency
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
	dependencies {
	        implementation 'com.github.salzix:androidsbrick:0.2.0'
	}
```

### Set required permissions
In *AndroidManifest.xml*:
```
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```
Notice *ACCESS_FINE_LOCATION*. It's required for Bluetooth device discovery. Yes, I was surprised too.

### (Option 1 - recommended) Create SBrick object from BluetoothDevice
It assumes that Bluetooth LE service discovery code is already present.
When you have BluetoothDevice instance pass it to SBrick, with ApplicationContext.
```
    // your BT LE discovery here
    SBrick sbrick = new SBrick(context, bluetoothDevice)
```

### (Option 2) Use ConnectionHelper
This library comes with optional ConnectionHelper to handle SBrick connection in simple scenarios. Example:
```
public class MainActivity extends AppCompatActivity
        implements ConnectionCallback {

    private ConnectionHelper connectionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // your code here
        connectionHelper = new ConnectionHelper(this, this);  // params are app context and callback
        connectionHelper.scanForSBricks();                    // find SBricks, 5 seconds discovery.
        // your code here
    }

    // callback after connectionHelper.scanForSBricks()
    public void handleSBrickCollection(Map<String, SBrick> sbricks) {
        // optimistic scenario - at least one SBrick is found
        SBrick sbrick = sbricks.entrySet().iterator().next().getValue()
    }
}
```

### Control SBrick
Send *rotate* command with full power to motors connected to channel A:
```
    RotateCommand rotateCommand = new RotateCommand();
    rotateCommand.channelA(RotateCommand.DIR_COUNTER_CLOCKWISE, (byte) 0xFF);
    sbrick.execute(rotateCommand);
```

## API reference
[SBrick protocol](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol) allows sending one command type to multiple (1-4) channels in one request. You can set different params (power, direction) for each channel.

### Helper constants
They are used as channel params for easier to read code. You can omit them and use Int values.

*Direction selectors:*
* **RotateCommand.DIR_CLOCKWISE**         = 0
* **RotateCommand.DIR_COUNTER_CLOCKWISE** = 1

### Rotate command
Drives motors on selected channels. You can chain up to four of them. This command can be also used to control lights.

Channel params are: *channel, direction, power (byte 0-255)*
```
    RotateCommand rotateCommand = new RotateCommand();
    rotateCommand
        .channelA(RotateCommand.DIR_CLOCKWISE, (byte) 0xFF)
        .channelB(RotateCommand.DIR_COUNTER_CLOCKWISE, (byte) 0xCC);
    sbrick.execute(rotateCommand);
```
Drives motors on all channels.
```
    RotateCommand rotateCommand = new RotateCommand();
    rotateCommand.allChannels(RotateCommand.DIR_CLOCKWISE, (byte) 0xFF);
    sbrick.execute(rotateCommand);
```

### Stop command
Stops rotation/power on selected channels. You can chain up to four of them.
```
    stopCommand = new StopCommand();
    stopCommand
        .channelA()
        .channelC();
    sbrick.execute(stopCommand);
```

Stops rotation/power on all channels.
```
    stopCommand = new StopCommand();
    stopCommand.allChannels();
    sbrick.execute(stopCommand);
```
### Keep in mind
* Sending command on one channel doesn't reset state of other channels. E.g. if you first send command to drive on all channels clockwise, and then send drive counter-clockwise on channel A, the rest will keep rotating clockwise. In second command you should also set channel params with 0 values to stop them.
* SBrick protocol limitation: you can send only *one command type* in a request (like "stop" command). You can send this command to multiple channels, though.
* This library comes with buffer preventing communication overload (losing commands) when you quickly send commands. Up to three commands are queued, other are ignored. Queue is consumed and commands are executed every 200ms.

## Versioning

I use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/salzix/androidsbrick/tags). 

## Authors

* **Tomasz Węgrowski** - [salzix](https://github.com/salzix)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

SBrick trademark is owned by [Vengit](https://www.sbrick.com/)
