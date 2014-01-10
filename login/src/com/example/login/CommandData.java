package com.example.login;

import android.util.Log;

public class CommandData {
    private byte _marker = (byte)0xAA;
    private byte _command = 0;
    private int _dataLength = 0;
    private int _crc = 0;
    private int[] _data = null;

    private final static int[] _dCrc16Table = new int[] {
            0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
            0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
            0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
            0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
            0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
            0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
            0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
            0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
            0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
            0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
            0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
            0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
            0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
            0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
            0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
            0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
            0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
            0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
            0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
            0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
            0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
            0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
            0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
            0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
            0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
            0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
            0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
            0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
            0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
            0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
            0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
            0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
    };

    public byte get_marker() {
        return _marker;
    }

    public byte get_command() {
        return _command;
    }

    public int get_dataLength() {
        return _dataLength;
    }

    public int get_crc() {
        return _crc;
    }

    public int[] get_data(){
        return _data;
    }

    private CommandData(){
    }

    public static int isCommand(byte[] data) {
        int commandLength = 0;

        if (data.length < 6)
            return 0;
        if (data[0] != ((byte) 0xAA) || data[1] < 1 || data[1] > 3)
            return -1;

        // check CRC
        int crc = ((((int) data[5]) << 8) & 0x0000FF00) | (data[4] & 0x000000FF);
        if (CommandData.calculateCrc(data, 4) != crc) {
            return -1;
        }
        commandLength = (((data[3]) << 8) & 0x0000FF00) | (data[2] & 0x000000FF);
        return commandLength;
    }

    public static CommandData decodeCommand(byte[] data) {
        CommandData cmd = new CommandData();
        if(data == null || data.length < 6){
            // command should contains at least 6 bytes
            return null;
        }

        if (data[0] != (byte)0xAA){
            // it isn't command
            return null;
        }
        cmd._command = data[1];

        cmd._crc = ((data[5] << 8) & 0x0000FF00) | (data[4] & 0x000000FF);
        int calculatedCrc = calculateCrc(data, 4);
        if (cmd._crc != calculatedCrc) {
            // wrong command crc
            return null;
        }

        int dataCount = ((data[3] << 8) & 0x0000FF00) | (data[2] & 0x000000FF);
        if ((dataCount & 0x0001) != 0) {
            // we wait array of words (double bytes) therefore there should be even number of bytes
            return null;
        }
        if (data.length < dataCount + 6) {
            // wring command length?
            return null;
        }

        cmd._dataLength = dataCount / 2;
        cmd._data = new int[cmd._dataLength];
        for(int i = 0; i < cmd._dataLength; i++) {
            cmd._data[i] = ((data[(i * 2) + 1 + 6] << 8)) | (data[i * 2 + 6] & 0x000000FF);
        }
        return cmd;
    }

    public byte[] encodeCommand() {
        _dataLength = ((_data == null)? 0 : _data.length * 2);
        byte[] cmd = new byte[_dataLength + 6];
        cmd[0] = _marker;
        cmd[1] = _command;
        cmd[2] = (byte)(_dataLength & 0x000000FF);
        cmd[3] = (byte)((_dataLength & 0x0000FF00) >>> 8);
        _crc = calculateCrc(cmd, 4);
        cmd[4] = (byte)(_crc & 0x000000FF);
        cmd[5] = (byte)((_crc & 0x0000FF00)>>>8);
        if (_data != null) {
            for(int i = 0; i < _data.length; i++) {
                cmd[(i * 2) + 6] = (byte)(_data[i] & 0x000000FF);
                cmd[(i * 2) + 6 + 1] = (byte)((_data[i] & 0x0000FF00) >>> 8);
            }
        }
        return cmd;
    }

    public static byte[] setParametersToDevice(int[] params) {
        CommandData cmd = new CommandData();
        cmd._command = 0x01;
        cmd._data = params;
        return cmd.encodeCommand();
    }
    public static byte[] getParametersFromDevice() {
        CommandData cmd = new CommandData();
        cmd._command = 0x2;
        cmd._dataLength = 0;
        cmd._data = null;
        return cmd.encodeCommand();
    }

    public static byte[] getDataToDevice() {
        CommandData cmd = new CommandData();
        cmd._command = 0x3;
        cmd._dataLength = 0;
        cmd._data = null;
        return cmd.encodeCommand();
    }

    public static int calculateCrc(byte[] pData, int iLen) {
        int iCrc = 0xFFFF;
        int i = 0;
        while (iLen > 0)
        {
            iLen -= 1;
            int f1 = (iCrc << 8) & 0x0000FFFF;
            int f2 = _dCrc16Table[(((iCrc >>> 8) & 0x000000FF) ^ ((pData[i++] & 0x000000FF)))];
            iCrc = (f1 ^ f2) & 0x0000FFFF;
        }
        return iCrc;
    }

}
