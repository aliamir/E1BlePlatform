package com.example.amir.e1bleplatform;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ConnectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.BLE_ADDRESS_MESSAGE);
        String name = intent.getStringExtra(MainActivity.BLE_NAME_MESSAGE);

        TextView rxAddress = findViewById(R.id.rx_address);
        TextView rxName = findViewById(R.id.rx_name);

        rxAddress.setText(address);
        rxName.setText(name);
    }

}
