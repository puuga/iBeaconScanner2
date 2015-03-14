package com.puuga.ibeaconscanner2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    public static final String TAG = "iBeaconScanner";

    private BeaconManager beaconManager;
    Region myRegion = new Region("myRegion", Identifier.parse("e2c56db5-dffb-48d2-b060-d0f5a71096e0"),null,null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyBluetooth();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    public void onBeaconServiceConnect() {
        iBeaconMonitor();
        iBeaconRange();
    }

    RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            // Log.i(TAG, "region: " + region.toString());
            if (beacons.size() > 0) {
                Log.i(TAG, "InRegion:");
                int i=1;
                for (Beacon beacon: beacons) {
                    logToDisplay("----------------" + i++);
                    logToDisplay("The beacon: " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");
                    logToDisplay("name:" + beacon.getBluetoothName());
                    logToDisplay("address:" + beacon.getBluetoothAddress());
                    logToDisplay("rssi:" + beacon.getRssi());
                    logToDisplay("tx power:" + beacon.getTxPower());
                    logToDisplay("distance:" + beacon.getDistance());
                    logToDisplay("----------------");
                    Log.i(TAG, "The beacon: " + beacon.toString());
                    Log.i(TAG, "name: " + beacon.getBluetoothName());
                }
            }
        }
    };

    private void iBeaconRange() {
        beaconManager.setRangeNotifier(rangeNotifier);

//        try {
//            beaconManager.startRangingBeaconsInRegion(myRegion);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    private void iBeaconMonitor() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                logToDisplay2("I just saw an beacon for the first time!");
                Log.i(TAG, "I just saw an beacon for the first time!");

                try {
                    beaconManager.startRangingBeaconsInRegion(myRegion);
                    logToDisplay2("startRangingBeaconsInRegion");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                logToDisplay2("I no longer see an beacon");
                Log.i(TAG, "I no longer see an beacon");

                try {
                    beaconManager.stopRangingBeaconsInRegion(myRegion);
                    logToDisplay2("stopRangingBeaconsInRegion");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                logToDisplay2("I have just switched from seeing/not seeing beacons: "+state);
                logToDisplay2("region: "+region.toString());
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(myRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) findViewById(R.id.editText);
                editText.append(line+"\n");
            }
        });
    }

    private void logToDisplay2(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) findViewById(R.id.editText2);
                editText.append(line+"\n");
            }
        });
    }
}
