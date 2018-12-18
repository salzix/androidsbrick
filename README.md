# androidsbrick
Android library for making apps communicating with SBrick little easier. Current status: working, unstable.

### Prerequisites
* SBrick or SBrick Plus with [firmware version 17+](https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol)
* Device with Android 6.0 (Marshmallow) or newer (API level 23)
* [Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth-le) support on Android device
* your favorite Android IDE

### Installing

Clone or download project. It consists of two parts:
* **androidsbrick** library
* **demo** application

You can compile and run project demo to see it working. For your project you'll need only androidsbrick. Copy it to your project as a module and configure project to use it as dependency.

Maven and Gradle installation - in progress.

## Usage
Library comes with optional ConnectionHelper to handle SBrick connection easier. This example uses helper.

### Set required permissions
In *AndroidManifest.xml*:
```
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```
Note: *ACCESS_FINE_LOCATION* is needed for Bluetooth device discovery.

### Declare variables
```
    private static final int REQUEST_ENABLE_BT = 1; // Bluetooth permissions
    private ConnectionHelper connectionHelper;      // optional helper
    private Map<String, SBrick> sbricks;            // will contain connected SBricks
    private String selectedSBrickId;                // store selected SBrick ID from map above
```
### Implement ConnectionCallback
It will be called when device discovery finishes scanning for SBricks or app gets asked for permissions.
Fills variable *sbricks* with *SBrick* objects.
```
public class MainActivity extends AppCompatActivity
        implements ConnectionCallback {
        
    public void handleSBrickCollection(Map<String, SBrick> sBrickCollection) {
        if (sBrickCollection.isEmpty()) {
            // SBricks not found
            return;
        }
        sbricks = sBrickCollection;
    }

    public boolean handlePermissionRequests() {
        // handle permission requests, see demo
    }
```

### Start SBrick discovery
After finishing callback above will be called.
```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        connectionHelper = new ConnectionHelper(this, this);  // params are app context and callback
    }
```

### Control SBrick
Send commands. See API reference below for details.
```
    selectedSBrickId = sbricks.keySet().iterator().next();
    sbricks.get(selectedSBrickId)
        .drive()
        .channel(SBrick.CHANNEL_A, SBrick.DIR_CLOCKWISE, (byte) 0xFF)
        .execute();
```

## API reference
WIP

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

I use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/salzix/androidsbrick/tags). 

## Authors

* **Tomasz Węgrowski** - *Initial work* - [salzix](https://github.com/salzix)

See also the list of [contributors](https://github.com/salzix/androidsbrick/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
SBrick trademark is owned by [Vengit](https://www.sbrick.com/)

