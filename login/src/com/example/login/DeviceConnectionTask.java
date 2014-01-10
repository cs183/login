package com.example.login;

import android.app.Activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 06.01.14
 * Time: 18:55
 * To change this template use File | Settings | File Templates.
 */
public class DeviceConnectionTask extends AsyncTask<Object, Void, DeviceConnection> {
    private Context _context;
    private ProgressDialog _waitDialog;

    public DeviceConnectionTask(Context parent) {
        super();
        _context = parent;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        _waitDialog = ProgressDialog.show(_context, "", "Connecting", true, false);
    }

    @Override
    public DeviceConnection doInBackground(Object... devices) {
        Object device = (devices.length > 0)? devices[0] : null;
        if (device == null) {
            return null;
        }
        DeviceConnection connection = null;
        if(device instanceof BluetoothDevice) {
            connection = new BtDeviceConnectionImpl((BluetoothDevice)device);
        } else if (device instanceof SocketAddress) {
            connection = new NetDeviceConnectionImpl((SocketAddress)device);
        }
        try {
            connection.connect();
        } catch (Exception e) {
            return connection;
        }
        if (device instanceof SocketAddress) {
            ThreadLocalVariablesKeeper.setServerAddress((SocketAddress)device);
        }
        return connection;
    }

    @Override
    public void onCancelled(DeviceConnection connection) {
        super.onCancelled(connection);
        _waitDialog.dismiss();
    }

    @Override
    public void onPostExecute(DeviceConnection connection) {
        super.onPostExecute(connection);
        _waitDialog.dismiss();
        if (connection == null) {
            Toast.makeText(_context, "Укажите устройство, к которому необходимо подключиться!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (connection.getState() != DeviceConnection.DeviceConnectionState.Open) {
            try {
                connection.close();
            } catch (Exception e) {}

            Toast.makeText(_context, "Невозможно подключиться к устройству!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(_context, "Подключено!",
                Toast.LENGTH_LONG).show();
        SensorDevice device = new SensorDevice(connection);
        device.execute();
        ThreadLocalVariablesKeeper.setSensorDevice(device);
        Intent startIntent = new Intent((LoginActivity)_context, GraphicsActivity.class);
        ((LoginActivity)_context).startActivity(startIntent);
    }
}
