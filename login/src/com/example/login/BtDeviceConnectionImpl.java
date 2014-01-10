package com.example.login;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 05.01.14
 * Time: 15:25
 * To change this template use File | Settings | File Templates.
 */
public class BtDeviceConnectionImpl implements DeviceConnection {

    static final UUID SERVICE_UUID = UUID.fromString("DA6051A8-A56C-4304-8271-6F43379F4844");

    private BluetoothDevice _device = null;
    private InputStream _inputStream = null;
    private OutputStream _outputStream = null;
    private BluetoothSocket _socket = null;
    private DeviceConnectionState _state = DeviceConnectionState.Unknown;

    public DeviceConnectionState getState() { return _state; }

    public ConnectionType getType() { return ConnectionType.Bluetooth; }

    public InputStream getInputStream() { return _inputStream; }

    public OutputStream getOutputStream() { return _outputStream; }

    public BtDeviceConnectionImpl(BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        _device = device;
    }

    public boolean connect() {
        try {
            if (_socket == null) {
                _socket = _device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            }
            if (!_socket.isConnected()) {
                _socket.connect();
            }
            _inputStream = _socket.getInputStream();
            _outputStream = _socket.getOutputStream();
            _state = DeviceConnectionState.Open;
        } catch (IOException e) {
            _state = DeviceConnectionState.Closed;
            _inputStream = null;
            _outputStream = null;
            return false;
            //throw new IllegalStateException("Bluetooth socket unable to connect or get input/output streams.");
        }
        return true;
    }

    public void close() {
        if (_socket != null && _socket.isConnected()) {
            try {
                _socket.close();
            } catch(IOException e) {}
        }
        _socket = null;
        _inputStream = null;
        _outputStream = null;
        _state = DeviceConnectionState.Closed;
    }
}
