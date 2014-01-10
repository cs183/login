package com.example.login;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 05.01.14
 * Time: 15:06
 * Supported connection types
 */
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
