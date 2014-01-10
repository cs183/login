package com.example.login;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 05.01.14
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public interface DeviceConnection {
    public enum DeviceConnectionState {
        Unknown,
        Open,
        Closed
    }
    ConnectionType getType();
    DeviceConnectionState getState();
    InputStream getInputStream();
    OutputStream getOutputStream();
    boolean connect();
    void close();
}
