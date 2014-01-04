package com.example.login;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 24.11.13
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private int _itemView = -1;
    private final Activity _context;
    public BluetoothDeviceAdapter(Activity context, int resource) {
        super(context, resource);
        _context = context;
        _itemView = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = _context.getLayoutInflater();
            view = inflater.inflate(R.layout.bluetoothdevice, null);
        }

        BluetoothDevice item = getItem(position);
        if (item!= null) {
            ((TextView)view.findViewById(R.id.bluetoothDevice)).setText(item.getName() + " " + item.getAddress());
        }

        return view;
    }
}
