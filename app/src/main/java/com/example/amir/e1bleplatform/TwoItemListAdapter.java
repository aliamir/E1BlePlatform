package com.example.amir.e1bleplatform;

import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.TwoLineListItem;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class TwoItemListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BleDevice> devices;

    public TwoItemListAdapter(Context context, ArrayList<BleDevice> devices) {
        this.context = context;
        this.devices = devices;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        TwoLineListItem twoLineListItem;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) convertView;
        }

        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();

        text1.setText(devices.get(position).getName());
        text2.setText("" + devices.get(position).getAddress());

        return twoLineListItem;
    }
}