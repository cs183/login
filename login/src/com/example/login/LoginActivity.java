package com.example.login;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.EnumSet;

public class LoginActivity extends Activity {
    public enum ConnectionType {
        None(0),
        Net(1),
        Bluetooth(2);

        private final long _connectionTypeValue;

        ConnectionType(long connectionTypeValue) {
            _connectionTypeValue = connectionTypeValue;
        }

        public long getConnectionTypeValue() {
            return _connectionTypeValue;
        }
    }

    static final int REQUEST_ENABLE_BT_ID = 1;

    private EditText netAddress;
    private EditText netPort;
    private Button netButton;
    private Button btButton;
    private Button connectButton;
    private Button btScanButton;
    private ViewGroup btView;
    private ViewGroup netView;

    private ConnectionType connectionType = ConnectionType.Bluetooth;
    private EnumSet<ConnectionType> _availableConnections = EnumSet.of(ConnectionType.Net);
    private BluetoothAdapter _btAdapter = null;
    private WifiManager _wifiManager = null;
    private BluetoothDeviceAdapter _btDevices = null;

    private final BroadcastReceiver _deviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
/*                for(int position = 0; position < _btDevices.getCount(); position++) {
                    if (_btDevices.getItem(position).getAddress() == device.getAddress()) {
                        // this device already in list
                        return;
                    }
                }
*/                _btDevices.add(device);
            }
        }
    };


    private View.OnClickListener scanDevicesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            _btAdapter.startDiscovery();
        }
    };


    private View.OnClickListener connectionTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btButton:
                    setConnectionType(ConnectionType.Bluetooth, true);
                    break;
                case R.id.netButton:
                    setConnectionType(ConnectionType.Net, true);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong connection type");
            }
        }
    };

    private View.OnClickListener connectButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private void checkBluetoothConnections() {
        btScanButton.setEnabled(false);
        _availableConnections.remove(ConnectionType.Bluetooth);
        _btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_btAdapter == null) {
            // device doesn't support bluetooth
            return;
        }
        if (!_btAdapter.isEnabled()) {
            // request to turn on bluetooth
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT_ID);
            return;
        }
        if (_btDevices == null) {
            _btDevices = new BluetoothDeviceAdapter(this, R.layout.bluetoothdevice);
            _btDevices.addAll(_btAdapter.getBondedDevices());
            ((ListView)btView.findViewById(R.id.btDevicesListView)).setAdapter(_btDevices);
        }
        _availableConnections.add(ConnectionType.Bluetooth);
        btScanButton.setEnabled(true);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        netButton = Button.class.cast(findViewById(R.id.netButton));
        btButton = Button.class.cast(findViewById(R.id.btButton));
        connectButton = Button.class.cast(findViewById(R.id.connectButton));
        btView = (ViewGroup)findViewById(R.id.btLayout);
        netView = (ViewGroup)findViewById(R.id.netLayout);
        netAddress = (EditText)findViewById(R.id.hostEditText);
        netPort = (EditText)findViewById(R.id.portEditText);
        btScanButton = (Button)findViewById(R.id.btScanButton);
        checkBluetoothConnections();
        if (_btDevices == null) {
            _btDevices = new BluetoothDeviceAdapter(this, R.layout.bluetoothdevice);
        }
        setListeners();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(_deviceFoundReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(_deviceFoundReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        LinearLayout bt = (LinearLayout)btView;
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        LinearLayout.LayoutParams params;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            bt.setOrientation(LinearLayout.VERTICAL);
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 0;
           // params.gravity = Gravity.BOTTOM;
            btScanButton.setLayoutParams(params);
        } else {
            bt.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 4;
          //  params.gravity = Gravity.RIGHT;
            btScanButton.setLayoutParams(params);
        }
        setConnectionType(connectionType, false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        int ct = savedState.getInt("connectionType", -1);
        if (ct >= 0) {
            connectionType = ConnectionType.values()[ct];
        }
        super.onRestoreInstanceState(savedState);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        private EnumSet<ConnectionType> _availableConnections = EnumSet.of(ConnectionType.Net);
        private BluetoothAdapter _btAdapter = null;
        private WifiManager _wifiManager = null;
        private BluetoothDeviceAdapter _btDevices = null;
        saveState.putE.putParcelable("_availableConnections", _availableConnections);
        saveState.putInt("connectionType", connectionType.ordinal());
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(((connectionType == ConnectionType.Net)? netView : btView).getWindowToken(), 0);
    }

    private void setConnectionType(ConnectionType connection, boolean animate) {
        connectionType = connection;
        netButton.setSelected(connection == ConnectionType.Net);
        netButton.setEnabled(connection != ConnectionType.Net);
        btButton.setSelected(connection == ConnectionType.Bluetooth);
        btButton.setEnabled(connection != ConnectionType.Bluetooth);
        View removeView = (connection == ConnectionType.Bluetooth)? netView : btView;
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(removeView.getWindowToken(), 0);
        View showView = (connection == ConnectionType.Bluetooth)? btView : netView;
        Animation removeAnimation = AnimationUtils.loadAnimation(this, R.anim.hiderotation);
        Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.showrotation);
        removeView.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
        if (animate) {
            removeView.startAnimation(removeAnimation);
            showView.startAnimation(showAnimation);
        }
    }

    private void setListeners() {
        netButton.setOnClickListener(connectionTypeListener);
        btButton.setOnClickListener(connectionTypeListener);
        connectButton.setOnClickListener(connectButtonListener);
        btScanButton.setOnClickListener(scanDevicesListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT_ID:
                if (resultCode == RESULT_OK) {
                    _availableConnections.add(ConnectionType.Bluetooth);
                }
                btScanButton.setEnabled(resultCode == RESULT_OK);
                // draw available connection types (if changed)
        }
    }
}
