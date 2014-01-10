package com.example.login;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.net.InetSocketAddress;
import java.util.EnumSet;

public class LoginActivity extends Activity {

    static final int REQUEST_ENABLE_BT_ID = 1;

    private EditText _netAddress;
    private EditText _netPort;
    private Button netButton;
    private Button btButton;
    private Button connectButton;
    private Button btScanButton;
    private ViewGroup btView;
    private ViewGroup netView;
    private ListView btDeviceListView;

    private ConnectionType _connectionType = ConnectionType.Bluetooth;
    private EnumSet<ConnectionType> _availableConnections = EnumSet.of(ConnectionType.Net);
    private BluetoothAdapter _btAdapter = null;
//    private WifiManager _wifiManager = null;
    private BluetoothDeviceAdapter _btDevices = null;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        netButton = Button.class.cast(findViewById(R.id.netButton));
        btButton = Button.class.cast(findViewById(R.id.btButton));
        connectButton = Button.class.cast(findViewById(R.id.connectButton));
        btView = (ViewGroup)findViewById(R.id.btLayout);
        netView = (ViewGroup)findViewById(R.id.netLayout);
        _netAddress = (EditText)findViewById(R.id.hostEditText);
        _netPort = (EditText)findViewById(R.id.portEditText);
        btScanButton = (Button)findViewById(R.id.btScanButton);
        btDeviceListView = (ListView)findViewById(R.id.btDevicesListView);
        btDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        checkBluetoothConnections();
        if (_btDevices == null) {
            _btDevices = new BluetoothDeviceAdapter(this, R.layout.bluetoothdevice);
        }
        setListeners();
        registerReceiver(_deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    public void onDestroy() {
        if (_btAdapter != null) {
            _btAdapter.cancelDiscovery();
        }
        unregisterReceiver(_deviceFoundReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        setConnectionType(_connectionType, false);
        onConfigurationChanged(getResources().getConfiguration());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);

        LinearLayout.LayoutParams params;
        int checkedDevice = btDeviceListView.getCheckedItemPosition();
        if (newConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((LinearLayout)btView).setOrientation(LinearLayout.VERTICAL);
            params = (LinearLayout.LayoutParams)btScanButton.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.weight = 0;
            btScanButton.setLayoutParams(params);
            params = (LinearLayout.LayoutParams)btDeviceListView.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.weight = 1;
            btDeviceListView.setLayoutParams(params);
        } else {
            ((LinearLayout)btView).setOrientation(LinearLayout.HORIZONTAL);
            params = (LinearLayout.LayoutParams)btScanButton.getLayoutParams();
            params.width = 0;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.weight = 2;
            btScanButton.setLayoutParams(params);
            params = (LinearLayout.LayoutParams)btDeviceListView.getLayoutParams();
            params.width = 0;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.weight = 6;
            btDeviceListView.setLayoutParams(params);
        }
        if (checkedDevice >= 0 && checkedDevice < btDeviceListView.getCount()) {
//            btDeviceListView.setItemChecked(checkedDevice, true);
            btDeviceListView.setSelection(btDeviceListView.getCheckedItemPosition());
        }
        btDeviceListView.invalidateViews();
    }

    private void setConnectionType(ConnectionType connection, boolean animate) {
        _connectionType = connection;
        boolean netConnection = (connection == ConnectionType.Net);
        netButton.setSelected(netConnection);
        netButton.setEnabled(!netConnection);
        btButton.setSelected(!netConnection);
        btButton.setEnabled(netConnection);
        View removeView = (netConnection)? btView : netView;
        View showView = (netConnection)? netView : btView;
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(removeView.getWindowToken(), 0);
        Animation removeAnimation = AnimationUtils.loadAnimation(this, R.anim.hiderotation);
        Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.showrotation);
        removeView.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
        if (animate) {
            removeView.startAnimation(removeAnimation);
            showView.startAnimation(showAnimation);
        }
    }

    // Listeners
    private void setListeners() {
        netButton.setOnClickListener(connectionTypeListener);
        btButton.setOnClickListener(connectionTypeListener);
        connectButton.setOnClickListener(connectButtonListener);
        btScanButton.setOnClickListener(scanDevicesListener);
        btDeviceListView.setOnItemClickListener(btDeviceSelectListener);
    }

    private final BroadcastReceiver _deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                for(int position = 0; position < _btDevices.getCount(); position++) {
                    if (_btDevices.getItem(position).getAddress().equals(device.getAddress())) {
                        // this device already in list
                        // return;
                    }
                }
                _btDevices.add(device);
            }
        }
    };

    private AdapterView.OnItemClickListener btDeviceSelectListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            btDeviceListView.setItemChecked(position, true);
//            view.setSelected(true);
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
            if (_btAdapter != null) {
                _btAdapter.cancelDiscovery();
            }
            switch (_connectionType) {
                case Net:
                    InetSocketAddress deviceAddress;
                    try {
                        String hostName = _netAddress.getText().toString();
                        Integer port = Integer.valueOf(_netPort.getText().toString());
                        deviceAddress = new InetSocketAddress(hostName, port);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Ошибка при разборе адреса", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    (new DeviceConnectionTask(LoginActivity.this)).execute(deviceAddress);
                    break;
                case Bluetooth:
                    if (btDeviceListView.getCount() <= 0) {
                        Toast.makeText(getApplicationContext(), "Нет доступных устройств для подключения", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    int itemPosition = btDeviceListView.getCheckedItemPosition();
                    if (itemPosition < 0) {
                        Toast.makeText(getApplicationContext(), "Не выбрано устройство для подключения", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    BluetoothDevice device = (BluetoothDevice)btDeviceListView.getItemAtPosition(itemPosition);
                    (new DeviceConnectionTask(LoginActivity.this)).execute(device);
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Хмммм... Тип соединения неопределен", Toast.LENGTH_LONG).show();
                    return;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT_ID:
                if (resultCode == RESULT_OK) {
                    if (_btDevices == null) {
                        _btDevices = new BluetoothDeviceAdapter(this, R.layout.bluetoothdevice);
                        _btDevices.addAll(_btAdapter.getBondedDevices());
                        ((ListView)btView.findViewById(R.id.btDevicesListView)).setAdapter(_btDevices);
                    }
                    _availableConnections.add(ConnectionType.Bluetooth);
                }
                btScanButton.setEnabled(resultCode == RESULT_OK);
                // draw available connection types (if changed)
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 0, "Настройки").setIntent(new Intent(this, Preferences.class));
        return super.onCreateOptionsMenu(menu);
    }

}
