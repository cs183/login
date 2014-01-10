package com.example.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 05.01.14
 * Time: 15:25
 * To change this template use File | Settings | File Templates.
 */
public class NetDeviceConnectionImpl implements DeviceConnection {

    private SocketAddress _deviceAddress = null;
    private InputStream _inputStream = null;
    private OutputStream _outputStream = null;
    private Socket _socket = null;
    private DeviceConnectionState _state = DeviceConnectionState.Unknown;

    public DeviceConnectionState getState() { return _state; }

    public ConnectionType getType() { return ConnectionType.Net; }

    public InputStream getInputStream() { return _inputStream; }

    public OutputStream getOutputStream() { return _outputStream; }

    public NetDeviceConnectionImpl(SocketAddress deviceAddress) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("Device address cannot be null");
        }
        _deviceAddress = deviceAddress;
    }

    public boolean connect() {
        try {
            if (_socket == null) {
                _socket = new Socket();
            }
            if (!_socket.isConnected()) {
                _socket.connect(_deviceAddress, 3000);
            }
            if (_socket.isConnected()) {
                _inputStream = _socket.getInputStream();
                _outputStream = _socket.getOutputStream();
                _state = DeviceConnectionState.Open;
                return true;
            }
            else _socket.close();
        } catch (IOException e) {
            int i = 1;
            i += 1;
        }

        _state = DeviceConnectionState.Closed;
        _inputStream = null;
        _outputStream = null;
        _socket = null;
        return false;
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
