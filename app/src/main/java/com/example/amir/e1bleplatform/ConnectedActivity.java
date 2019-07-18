package com.example.amir.e1bleplatform;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

import static java.nio.charset.Charset.defaultCharset;


public class ConnectedActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();                   //Activity name for logging messages on the ADB
    public static final String BLE_ADDRESS_MESSAGE_SERVICE = "com.example.amir.e1bleplatform.BLE_ADDRESS_MESSAGE_SERVICE";
    public static final String BLE_NAME_MESSAGE_SERVICE = "com.example.amir.e1bleplatform.BLE_NAME_MESSAGE_SERVICE";

    // Message Packet Data
    private final byte STX = 0x55;
    private final byte ETX = 0x04;
    private final byte DLE = 0x05;
    private final byte MAX_CAN_PACKET_SIZE = 14;
    private final byte CS_SIZE = 1;
    // Screen Text
    TextView rxAddressView;
    TextView rxNameView;

    // Buttons
    Button CheckConnection;
    Button SendButton;
    Button HwRev;
    Button SwRev;
    Button Disconn;
    FloatingActionButton ClearTxButton;
    FloatingActionButton ClearRxButton;

    // Progress Bar
    ProgressBar ConnStatusProgressBar;
    // BLE Device Class
    BleDevice mBleDevice;
    // Textboxes for Rx/Tx
    TextView RxTextBox;
    EditText TxTextBox;

    TextView SocTextBox, PackVTextBox, PackITextBox;

    // Intent from MainActivity
    Intent serviceIntent;

    // Service info
    private static BleConnectionService mBleConnectionService;
    private boolean isServiceBound;
    private ServiceConnection mServiceConnection;

    // BLE States
    private enum BleState {DISCONNECTED, CONNECTED}
    BleState mBleState;
    private static boolean serviceStarted = false;

    // Rx Data
    Queue<Byte> rxBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Intent from MainActivity
        Intent intent = getIntent();

        // Store messages in Queue
        rxBytes = new LinkedList<>();

        mBleDevice = new BleDevice(intent.getStringExtra(MainActivity.BLE_NAME_MESSAGE),
                intent.getStringExtra(MainActivity.BLE_ADDRESS_MESSAGE));

        // set TextView
        rxAddressView = findViewById(R.id.rx_address);
        rxNameView = findViewById(R.id.rx_name);
        // Set TextBox for Rx/Tx data
        RxTextBox = findViewById(R.id.rx_data_text_box);
        TxTextBox = findViewById(R.id.tx_data_text_box);
        // Set TextBox for SOC, Pack voltage, Pack current
        SocTextBox = findViewById(R.id.SocValueText);
        PackVTextBox = findViewById(R.id.PackVValueText);
        PackITextBox = findViewById(R.id.PackIValueText);

        // Set Button
        CheckConnection = findViewById(R.id.conn_status_button);
        CheckConnection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isServiceBound) {
                    boolean connectionStatus = mBleConnectionService.getConnectionStatus();
                    if (connectionStatus) {
                        mBleConnectionService.disconnect();
                        ConnStatusProgressBar.setVisibility(View.VISIBLE);
                        // Change text to disconnecting...
                    }
                    else {
                        mBleConnectionService.connect(mBleDevice.getAddress());
                        ConnStatusProgressBar.setVisibility(View.VISIBLE);
                        // Change text to connecting...
                    }
                }
                else {
                    // Service is not bound, so can't disconnect
                }
            }
        });

        TxTextBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String composeMsg = TxTextBox.getText().toString()+"\n";
                    TxTextBox.setText("");
                    sendMldpData(composeMsg);
                    return true;
                }
                return false;
            }
        });

        ClearTxButton = findViewById(R.id.tx_clear_float_button);
        ClearTxButton.setOnClickListener(new FloatingButtons());

        ClearRxButton = findViewById(R.id.rx_clear_float_button);
        ClearRxButton.setOnClickListener(new FloatingButtons());

        // Send Button
        SendButton = findViewById(R.id.send_button);
        SendButton.setOnClickListener(new FloatingButtons());
        SendButton.setVisibility(View.GONE);

        // Set ProgressBar
        ConnStatusProgressBar = findViewById(R.id.conn_status_progressbar);
        ConnStatusProgressBar.setVisibility(View.VISIBLE);

        rxAddressView.setText(mBleDevice.getAddress());
        rxNameView.setText(mBleDevice.getName());

        // Send HW Revision Command Button
        HwRev = findViewById(R.id.hwRevButton);
        HwRev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (BleState.CONNECTED == mBleState) {
                    byte value[] = {(byte) 0x02};
                    byte checksumbyte[] = createPacket(value);
                    String HexString = byteArrayToHex(checksumbyte);
                    HexString = HexString.replaceAll("..", "$0 ").trim();
                    TxTextBox.setText(HexString);
                    mBleConnectionService.writeMLDP(checksumbyte);
                }
            }
        });

        // Send SW Version Command Button
        SwRev = findViewById(R.id.swRevButton);
        SwRev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (BleState.CONNECTED == mBleState) {
                    byte value[] = {(byte) 0x03};
                    byte checksumbyte[] = createPacket(value);
                    String HexString = byteArrayToHex(checksumbyte);
                    HexString = HexString.replaceAll("..", "$0 ").trim();
                    TxTextBox.setText(HexString);
                    mBleConnectionService.writeMLDP(checksumbyte);
                }
            }
        });

        Disconn = findViewById(R.id.disconnButton);
        Disconn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (BleState.CONNECTED == mBleState) {
                    byte value[] = {(byte) 0x20, 0x03};
                    byte checksumbyte[] = createPacket(value);
                    String HexString = byteArrayToHex(checksumbyte);
                    HexString = HexString.replaceAll("..", "$0 ").trim();
                    TxTextBox.setText(HexString);
                    mBleConnectionService.writeMLDP(checksumbyte);
                }
            }
        });

        if (!serviceStarted) {
            StartBleConnectionService();
            serviceStarted = true;
        }
        else {
            if (isServiceBound) {
                mBleConnectionService.connect(mBleDevice.getAddress());
            }
            else {
                bindService();

                //mBleConnectionService.connect(mBleDevice.getAddress());
            }
        }

    }

    class FloatingButtons implements View.OnClickListener {
        @Override
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.rx_clear_float_button:
                    RxTextBox.setText("");
                    break;
                case R.id.tx_clear_float_button:
                    TxTextBox.setText("");
                    break;
                case R.id.send_button:
                    String composeMsg = TxTextBox.getText().toString()+"\n";
                    TxTextBox.setText("");
                    sendMldpData(composeMsg);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendMldpData(String msg) {
        if (BleState.CONNECTED == mBleState) {
            mBleConnectionService.writeMLDP(msg);
        }
    }

    void UnbindBleService() {
        unbindService(mServiceConnection);
        isServiceBound = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(bleServiceReceiver);
        //stopService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bleServiceReceiver, bleServiceIntentFilter());
        bindService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBleConnectionService.disconnect();
        if (isServiceBound) {
            UnbindBleService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            UnbindBleService();                                                   //Unbind from the service handling Bluetooth
        }
        mBleConnectionService = null;
    }

    /**
     * Description:
     * Start service to maintain BLE connection
     */
    void StartBleConnectionService() {
        // Create Intent to send data
        serviceIntent = new Intent(getApplicationContext(), BleConnectionService.class);
        serviceIntent.putExtra(BLE_NAME_MESSAGE_SERVICE, mBleDevice.getName());
        serviceIntent.putExtra(BLE_ADDRESS_MESSAGE_SERVICE, mBleDevice.getAddress());
        startService(serviceIntent);

        bindService();
    }

    private void bindService() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BleConnectionService.BleConnectionBinder myServiceBinder = (BleConnectionService.BleConnectionBinder)service;
                    mBleConnectionService = myServiceBinder.getService();

                    //Once we're bound again, we will try to connect:
                    if (serviceStarted) {
                        mBleConnectionService.connect(mBleDevice.getAddress());
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
        }

        isServiceBound = true;
        serviceIntent = new Intent(getApplicationContext(), BleConnectionService.class);
        serviceIntent.putExtra(BLE_NAME_MESSAGE_SERVICE, mBleDevice.getName());
        serviceIntent.putExtra(BLE_ADDRESS_MESSAGE_SERVICE, mBleDevice.getAddress());
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);


    }

    // ----------------------------------------------------------------------------------------------------------------
    // Intent filter to add Intent values that will be broadcast by the MldpBluetoothService to the bleServiceReceiver BroadcastReceiver
    private static IntentFilter bleServiceIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConnectionService.ACTION_BLE_REQ_ENABLE_BT);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(BleConnectionService.ACTION_BLE_DATA_RECEIVED);
        return intentFilter;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver handles various events fired by the MldpBluetoothService service.
    private final BroadcastReceiver bleServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
            if (BleConnectionService.ACTION_BLE_CONNECTED.equals(intent.getAction())) {			                //Service has connected to BLE device
            //connectTimeoutHandler.removeCallbacks(abortConnection);                             //Stop the connection timeout handler from calling the runnable to stop the connection attempt
            Log.d(TAG, "Received intent  ACTION_BLE_CONNECTED");
            UpdateConnectionState(BleState.CONNECTED);
            //state = State.CONNECTED;
            //updateConnectionState();                                                            //Update the screen and menus
//            if (attemptingAutoConnect == true) {
//                showAlert.dismiss();
//            }
        }
            else if (BleConnectionService.ACTION_BLE_DISCONNECTED.equals(action)) {		            //Service has disconnected from BLE device
            Log.d(TAG, "Received intent ACTION_BLE_DISCONNECTED");
            UpdateConnectionState(BleState.DISCONNECTED);                                                            //Update the screen and menus
        }
            else if (BleConnectionService.ACTION_BLE_DATA_RECEIVED.equals(action)) {
                Log.d(TAG, "Received intent ACTION_BLE_DATA_RECEIVED");
                byte data[] = intent.getByteArrayExtra(BleConnectionService.INTENT_EXTRA_SERVICE_DATA);
                //String toHex = String.format("%x", new BigInteger(1, data)); //Show all data in textbox


                // Put all the bytes into a queue
                for (int i = 0; i < data.length; i++) {
                    rxBytes.add(data[i]);
                }
                // Copy the queue to an array to parse
                Byte[] parseMsg = new Byte[rxBytes.size()];
                rxBytes.toArray(parseMsg);
                for (int i = 0; i < parseMsg.length; i++) {
                    // Since we want to check for 0x04, 0x04 we want to make sure we're not at the
                    // end of the array
                    if (i < (parseMsg.length - 1)) {
                        // If we find 0x04, 0x04 that means a message is ready
                        if ((parseMsg[i] == 0x04) && (parseMsg[i+1] == 0x04)) {
                            // Msg is ready, load into byte[] array and parse
                            byte msgReady[] = new byte[i+2];
                            int j = 0;
                            while (j < i) {
                                msgReady[j++] = rxBytes.remove();
                            }
                            msgReady[i] = rxBytes.remove();
                            msgReady[i+1] = rxBytes.remove();
                            byte[] parsedRxMsg = ParseBleMsg(msgReady);
                            if ((parsedRxMsg != null) && (parsedRxMsg[0] == 0x01)) {
                                ParseCanMessage(parsedRxMsg);
                            }
                            else{
                                String toHex = byteArrayToHex(msgReady);
                                toHex = toHex.replaceAll("..", "$0 ").trim();
                                RxTextBox.append(toHex);
                            }

                            break;
                        }
                    }
                }

            }
/*            else if (BleConnectionService.ACTION_BLE_DATA_RECEIVED.equals(action)) {		        //Service has found new data available on BLE device
            Log.d(TAG, "Received intent ACTION_BLE_DATA_RECEIVED");
            String data = intent.getStringExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_DATA); //Get data as a string to display
//                String data = null;
//                try {
//                    data = new String(intent.getByteArrayExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_DATA), "UTF-8"); // Example for bytes instead of string
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
            if (data != null) {
                textIncoming.append(data);
            }
        }*/
    }
    };

    private byte[] ParseBleMsg(byte[] msg){
        byte[] parsedCanMsg = new byte[msg.length];
        byte checksum = 0;
        boolean saveByte = false;
        boolean wasDle = false;
        int dataCount = 0;
        int i = 0;

        while(i < msg.length) {
            if (!wasDle) {
                switch (msg[i]) {
                    case STX: // Start of packet
                        checksum = 0;
                        dataCount = 0;
                        saveByte = false;
                        break;
                    case ETX: // End of packet
                        saveByte = false;
                        break;
                    case DLE: // Byte stuffing
                        wasDle = true;
                        saveByte = false;
                        break;
                    default:
                        saveByte = true;
                        break;
                }
            }
            else {
                saveByte = true;
            }

            /* Save all the bytes that aren't control bytes */
            if (saveByte) {
                checksum += msg[i];
                parsedCanMsg[dataCount] = msg[i];
                dataCount++;
                wasDle = false;
            }
            i++;
        }

        // Check if the message is valid
        // checksum: the board will send 2's compliment of the checksum. So when we add the checksum
        // it should equal 0
        if (checksum != 0) {
            parsedCanMsg = null;
        }

        return parsedCanMsg;
    }

    void ParseCanMessage(byte[] msg) {
        byte cmd = 0;
        int messageId = 0;
        byte isExtended = 0;
        byte dlc = 0;
        byte[] data = new byte[8];

        cmd = msg[0];
        messageId = msg[1];
        messageId |= msg[2] << 8;
        messageId |= msg[3] << 16;
        messageId |= msg[4] << 24;

        isExtended = msg[5];

        dlc = msg[6];

        for (int i = 0; i < data.length; i++) {
            data[i] = msg[i+7];
        }

        switch(messageId) {
            case 0x200:
                // Pack Voltage
                int packVoltageInt = 0;
                packVoltageInt = data[0] & 0xFF;
                packVoltageInt |= ((data[1] & 0xFF) << 8);
                double packVoltage = (double)packVoltageInt * 0.001;
                DecimalFormat packV = new DecimalFormat("##.##");
                PackVTextBox.setText(packV.format(packVoltage)+" V");

                // Pack Current
                int packIInt = data[2] & 0xFF;
                packIInt |= data[3] << 8;
                double packCurrent = (double)packIInt * 0.01;
                DecimalFormat packI = new DecimalFormat("###.##");
                PackITextBox.setText(packI.format(packCurrent) +" A");

                // SOC
                int socInt = data[6] & 0xFF;
                socInt |= (data[7] & 0xFF) << 8;
                double Soc = (double)socInt * 0.1;
                DecimalFormat packSoc = new DecimalFormat("###.#");
                SocTextBox.setText(packSoc.format(Soc) +" %");

                break;
            default:
                break;

        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b : a)

            sb.append(String.format("%02X", b & 0xff));

        return sb.toString();
    }

    private byte[] createPacket(byte[] data){
        try {
            ByteArrayOutputStream bytesToSend = new ByteArrayOutputStream();
            int checkSum = 0;
            bytesToSend.write(STX);
            bytesToSend.write(STX);
            //look at each byte for control characters and add checksum
            for (int offset = 0; offset < data.length; offset++) {
                if (true == checkForControlChars(data[offset])) {
                    bytesToSend.write(DLE);
                }
                bytesToSend.write(data[offset]);
                checkSum += data[offset];
            }
            checkSum = ~checkSum + 1;
            //check if checksum needs a delimiter
            if (true == checkForControlChars((byte) checkSum)) {
                bytesToSend.write(DLE);
            }
            bytesToSend.write((byte) checkSum);
            bytesToSend.write(ETX);
            bytesToSend.write(ETX);
            return bytesToSend.toByteArray();
        }catch (Exception e){
            e.getMessage();
            return new ByteArrayOutputStream().toByteArray();
        }
    }

    public boolean checkForControlChars(byte abc){
        try {

            if(STX==abc||ETX==abc|DLE==abc){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void UpdateConnectionState(BleState state) {
        switch (state) {
            case DISCONNECTED:
                CheckConnection.setText(R.string.connect_text);
                ConnStatusProgressBar.setVisibility(View.GONE);
                SendButton.setVisibility(View.GONE);
                mBleState = BleState.DISCONNECTED;
                break;
            case CONNECTED:
                CheckConnection.setText(R.string.disconnect_text);
                ConnStatusProgressBar.setVisibility(View.GONE);
                SendButton.setVisibility(View.VISIBLE);
                mBleState = BleState.CONNECTED;
                break;
            default:
                break;
        }
    }
}
