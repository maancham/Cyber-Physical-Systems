package com.example.airhockey.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toast;

import com.example.airhockey.R;
import com.example.airhockey.services.BluetoothService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConnectionActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 0;
    private final int REQUEST_SET_DISCOVERABLE = 1;

    ProgressDialog progressDialogServer;
    ProgressDialog progressDialogClient;
    Dialog dialogClient;

    ImageView clientBtn;
    ImageView serverBtn;
    List<String> foundDevicesNames = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> foundedDevices = new HashSet<>();
//    private Set<BluetoothDevice> pairedDevices = new HashSet<>();
    private BluetoothService bluetoothService = BluetoothService.getInstance();
    ListView lv;
    boolean searchForConnection = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {
                    foundedDevices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
//                    TODO: finish checking and send message if needed
                    break;
                }
            }
            Log.e("FOUND", action + " " + foundedDevices.size());
        }
    };

    public void startScan() {
        if (!bluetoothAdapter.isDiscovering())
//            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
    }

    List<String> getDeviceNames() {
        List<String> nameList = new ArrayList<>();
        for (BluetoothDevice device : foundedDevices) {
            nameList.add(device.getName());
        }
        String[] array = new String[nameList.size()];
        return nameList;
    }

    Animation getAnimation(){
        Animation animation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }



    public void setView() {
        setContentView(R.layout.activity_connection);
        clientBtn = findViewById(R.id.connection_client_btn);
        serverBtn = findViewById(R.id.connection_server_btn);
        progressDialogServer = new ProgressDialog(this);
        progressDialogClient = new ProgressDialog(this);
        Animation animation = getAnimation();
        serverBtn.startAnimation(animation);
        clientBtn.startAnimation(animation);
        dialogClient = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.devices_list, null);
        ListView listView = view.findViewById(R.id.bluetooth_devices);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if (foundDevicesNames != null){
                progressDialogClient.setTitle("connecting to opponent");
                progressDialogClient.setMessage("Waiting for opponent to connect...");
                progressDialogClient.setCancelable(true); // disable dismiss by tapping outside of the dialog
                searchForConnection = true;
                progressDialogClient.setOnCancelListener(dialog -> {
                    searchForConnection = false;
                });
                progressDialogClient.show();
                BluetoothDevice device = null;
                String name = foundDevicesNames.get(position);
                for (BluetoothDevice bluetoothDevice : foundedDevices){
                    if (bluetoothDevice.getName().equals(name)){
                        device = bluetoothDevice;
                        break;
                    }
                }
                BluetoothDevice finalDevice = device;
                Thread connectionThread = new Thread(() -> {
                    boolean connected = false;
                    bluetoothService.connect(finalDevice);
                    while (!connected && searchForConnection) {
                        connected = bluetoothService.isConnected();
                    }
                    if (connected) {
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    }
                    else {
                        bluetoothService.stopConnection();
                    }
                    progressDialogClient.dismiss();
                });
                connectionThread.start();
            }
        });
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.bluetooth_device,R.id.bluetooth_name,getDeviceNames());
        Button scanButton = view.findViewById(R.id.scan_btn);
        listView.setEmptyView(findViewById(R.id.empty_list));
        scanButton.setOnClickListener(v -> {
            startScan();
            arrayAdapter.clear();
            foundDevicesNames = getDeviceNames();
            arrayAdapter.addAll(foundDevicesNames);
            arrayAdapter.notifyDataSetChanged();
        });
        dialogClient.setContentView(view);
        clientBtn.setOnClickListener(v -> {
            arrayAdapter.clear();
            foundDevicesNames = getDeviceNames();
            arrayAdapter.addAll(foundDevicesNames);
            startScan();
            listView.setAdapter(arrayAdapter);
            dialogClient.show();
        });

        serverBtn.setOnClickListener(v -> {
            progressDialogServer.setTitle("Waiting for opponent");
            progressDialogServer.setMessage("Waiting for clients to connect...");
            progressDialogServer.setCancelable(true); // disable dismiss by tapping outside of the dialog
            searchForConnection = true;
            progressDialogServer.setOnCancelListener(dialog -> {
                searchForConnection = false;
            });
            makeDeviceDiscoverable();
            Thread connectionThread = new Thread(() -> {
                boolean connected = false;
                bluetoothService.startConnection();
                while (!connected && searchForConnection) {
                    connected = bluetoothService.isConnected();
                }
                if (connected) {
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
                else {
                    bluetoothService.stopConnection();
                }
                progressDialogServer.dismiss();
            });
            connectionThread.start();
            progressDialogServer.show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        if (!bluetoothAdapter.isEnabled())
            turnOnBluetooth();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if (resultCode != RESULT_OK){
                turnOnBluetooth();
            }
        }
    }

    private void turnOnBluetooth(){
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, REQUEST_ENABLE_BT);  
    }

    private void makeDeviceDiscoverable() {
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_SET_DISCOVERABLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }
}