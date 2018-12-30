package com.example.amir.e1bleplatform;

import android.widget.BaseAdapter;
import android.content.Context;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class TwoItemListAdapter extends BaseAdapter {

    private Context context;
    private int layoutResourceID;
    private ArrayList<BleDevice> devices;

    public TwoItemListAdapter(Context context, int resource, ArrayList<BleDevice> devices) {
        this.context = context;
        this.devices = devices;
        layoutResourceID = resource;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertedView, ViewGroup parent) {
        // Make sure we have a view to use
        View itemView = convertedView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(layoutResourceID, parent, false);
        }

        // Find the device
        BleDevice currentDevice = devices.get(position);

        // Fill the View (show the data from the BtleDevice class
        TextView dNameText = itemView.findViewById(R.id.name);
        dNameText.setText(currentDevice.getName());

        TextView dAddressText = itemView.findViewById(R.id.address);
        dAddressText.setText(currentDevice.getAddress());

        // Return the View
        return itemView;
    }
}
