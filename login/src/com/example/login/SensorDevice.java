package com.example.login;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SensorDevice extends AsyncTask<Void, Boolean, Void>{

    public interface ChangeHandler {
        void onDataChanged(int[] data);
    }

    private static final String _lTag = "SensorDevice";
    public ChangeHandler onDataChangedHandler = null;
    public ChangeHandler onParametersChangedHandler = null;
    private DeviceConnection _connection;
    private int[] _data = new int[10];
    private int[] _parameters = new int[4096];
    private boolean _deviceRunning = false;
    private int _cmdCount = 0;
    private byte[] _cmdBuffer = new byte[65536];
    private final Lock _dataLock = new ReentrantLock();
    private final Lock _parametersLock = new ReentrantLock();
    private final Object _dataSync = new Object();
    private final Object _parametersSync = new Object();
    private int _pollInterval = 40;
    private Thread _pollThread = null;

    public SensorDevice(DeviceConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Device connection required parameter");
        }
        _connection = connection;
    }

    public int[] getData() {
        return _data;
    }

    public int[] getParameters() {
        return _parameters;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        // check connection and another conditions to run device
        if (_connection.getState() != DeviceConnection.DeviceConnectionState.Open) {
            throw new IllegalStateException("Device connection should be established before run device.");
        }
        if (_connection.getInputStream() == null || _connection.getOutputStream() == null) {
            throw new IllegalStateException("Device streams are not ready.");
        }
        _deviceRunning = true;
    }

    @Override
    public Void doInBackground(Void... args) {
        byte[] readBuffer = new byte[65536];

        while (!isCancelled() && _deviceRunning) {
            int readCount = -1;
            try {
                readCount = _connection.getInputStream().read(readBuffer);
            } catch (IOException e) {
                Log.w(_lTag, "Ошибка при чтении данных с устройства", e);
                break;
            }

            if (!isCancelled() && _deviceRunning && readCount > 0) {
                byte[] data = Arrays.copyOf(readBuffer, readCount);
                Log.d(_lTag, "Данные получены. Начата обработка. " + readCount);
                parseDeviceData(data);
                Log.d(_lTag, "Полученные данные обработаны");
            }
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Boolean... args) {
        Boolean isParameters = (args != null && args.length > 0)? args[0] : false;

        if (isParameters) {
            synchronized (_parametersSync) {
                if (onParametersChangedHandler != null) onParametersChangedHandler.onDataChanged(_parameters);
            }
        } else {
            synchronized (_dataSync) {
                if (onDataChangedHandler != null) onDataChangedHandler.onDataChanged(_data);
            }
        }
    }

    @Override
    public void onCancelled(Void arg) {
        _deviceRunning = false;
        super.onCancelled();
    }

    @Override
    public void onPostExecute(Void arg) {
        _deviceRunning = false;
        super.onPostExecute(arg);
    }

    public void startPolling(int pollInterval) {
        if (!_deviceRunning) {
            throw new IllegalStateException("Device should be in running state before poll commands");
        }
        _pollInterval = pollInterval;
        if (_pollThread != null && _pollThread.isAlive())
            return;
        _pollThread = new Thread(new Runnable() {
            public void run() {
                poll();
            }
        });
        _pollThread.start();
    }

    public void stopPolling() {
        if (_pollThread == null)
            return;
        _pollThread.interrupt();
        _pollThread = null;
    }

    private void poll() {
        byte[] cmd = CommandData.getDataToDevice();
        try {
            OutputStream out = _connection.getOutputStream();
            while (!Thread.interrupted() && _deviceRunning) {
                synchronized (_dataSync) {
                    out.write(cmd);
                    try {
                        _dataSync.wait(_pollInterval);
                        Thread.sleep(_pollInterval);
                    } catch (InterruptedException e) {
                        Log.w(_lTag, "Polling thread was interrupted", e);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(_lTag, "Ошибка при отправке запроса данных на устройство", e);
        }
    }

    public void sendParameters(int[] _parameters) {
        final byte[] cmd = CommandData.setParametersToDevice(_parameters);
        Thread w = new Thread(new Runnable() {
            public void run() {
                try {
                    OutputStream out = _connection.getOutputStream();
                    out.write(cmd);
                } catch (IOException e) {
                    Log.e(_lTag, "Ошибка при отправке данных.", e);
                }
                return;
            }
        });
        w.start();
    }

    private void parseDeviceData(byte[] data) {
        if (data == null || data.length <= 0)
            return;

        int cmdLen = CommandData.isCommand(data);
        if (cmdLen >= 0) {
            _cmdCount = 0; // clear buffer if there was previous command it
            // wouldn't be continued
            if (cmdLen + 6 <= data.length) {
                byte[] cmd = new byte[cmdLen + 6];
                for (int i = 0; i < cmdLen + 6; i++)
                    cmd[i] = data[i];
                Log.d(_lTag, "Получена команда: " + data[1]);
                storeDeviceData(cmd);
                if (cmdLen + 6 < data.length) {
                    // We got command and something else it could be next
                    // command....
                    byte[] rest = new byte[data.length - (cmdLen + 6)];
                    for (int i = 0; i < data.length - (cmdLen + 6); i++)
                        rest[i] = data[i + cmdLen + 6];
                    parseDeviceData(rest);
                }
                return;
            } else {
                // Got only first part of command
                for (_cmdCount = 0; _cmdCount < data.length; _cmdCount++)
                    _cmdBuffer[_cmdCount] = data[_cmdCount];
                return;
            }
        }
        if (cmdLen < 0 && _cmdCount > 0) {
            // Got some data and have part of a command in waiting buffer
            int completeCmdLen = CommandData.isCommand(_cmdBuffer);
            if (completeCmdLen + 6 > _cmdCount + data.length) {
                // it is again only part of a command, continue waiting
                for (int i = 0; i < data.length; i++)
                    _cmdBuffer[_cmdCount++] = data[i];
                return;
            } else {
                byte[] cmd = new byte[completeCmdLen + 6];
                for (int i = 0; i < _cmdCount; i++)
                    cmd[i] = _cmdBuffer[i];
                for (int i = 0; i < completeCmdLen + 6 - _cmdCount; i++)
                    cmd[_cmdCount + i] = data[i];
                _cmdCount = 0;
                Log.d(_lTag, "Получена команда (буфер):" + cmd[1]);
                storeDeviceData(cmd);
                if (completeCmdLen + 6 < _cmdCount + data.length) {
                    // We got command and something else it could be next
                    // command....
                    int restSize = data.length + _cmdCount - (completeCmdLen + 6);
                    byte[] rest = new byte[restSize];
                    for (int i = 0; i < restSize; i++)
                        rest[i] = data[i + (data.length - restSize)];
                    parseDeviceData(rest);
                }
                return;
            }
        }
        Log.d(_lTag, "Обработка полученных данныхю Получили не понятно что..." + data.length);
    }

    private void storeDeviceData(byte[] data) {
        Log.d(_lTag, "Сохранение полученной команды.");
        CommandData cmd = CommandData.decodeCommand(data);
        if (cmd == null) {
            Log.w(_lTag, "Сохранение команды - не возможно разобрать коменду");
            return;
        }

        if (cmd.get_command() == 0x01) {
            synchronized (_parametersSync) {
                _parametersSync.notifyAll();
                // Call handler for changed parameters
            }
            Log.d(_lTag, "Сохранение команды. Ответ на SET PARAMS.");
        } else if (cmd.get_command() == 0x02) {
            // response on get parameters command
            synchronized (_parametersSync) {
                if (cmd.get_data() != null && cmd.get_data().length == 10) {
                    boolean paramsChanged = false;
                    if (_parameters != null) {
                        for (int i = 0; i < _parameters.length; i++) {
                            if (cmd.get_data()[i] != _parameters[i]) {
                                paramsChanged = true;
                                break;
                            }
                        }
                    } else {
                        paramsChanged = true;
                    }

                    if (paramsChanged) {
                        try {
                            _parametersLock.lock();
                            _parameters = cmd.get_data();
                        } finally {
                            _parametersLock.unlock();
                        }
                    }
                }
                _parametersSync.notifyAll();
                publishProgress(true);
                // Call handler for changed parameters
            }
            Log.d(_lTag, "Сохранение команды. Ответ на GET PARAMS.");
        } else if (cmd.get_command() == 0x03) {
            synchronized (_dataSync) {
                // response on get data command
                synchronized (_dataLock) {
                    try {
                        _dataLock.lock();
                        _data = (cmd.get_data() == null) ? new int[0] : cmd.get_data();
                    } finally {
                        _dataLock.unlock();
                    }
                }
                _dataSync.notifyAll();
                // Call handler for changed data
            }
            publishProgress(false);
            Log.d(_lTag, "Сохранение команды. Ответ на GET DATA.");
        }
        Log.d(_lTag, "Команда сохранена.");
    }
}
