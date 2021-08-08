package com.example.airhockey.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.example.airhockey.models.ConnectionStates;
import com.example.airhockey.models.MessageConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    private static BluetoothService _instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket transferSocket;
    private Handler handler;
    private Handler connectionDropNotifier;
    private int currentState;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private final String APP_NAME = "AirHockey";
    private static final UUID SERVICE_UUID = UUID.fromString("0e628292-c018-11eb-8529-0242ac130003");
    private final int BUFFER_LENGTH = 1024;

    private BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        currentState = ConnectionStates.STATE_IDLE;
    }

    public static BluetoothService getInstance() {
        if (_instance == null)
            _instance = new BluetoothService();
        return _instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setConnectionDropNotifier(Handler handler){
        this.connectionDropNotifier = handler;
    }

    public void write(byte[] data) {
        ConnectedThread copy;
        synchronized (this) {
            if (currentState != ConnectionStates.STATE_CONNECTED) return;
            copy = this.connectedThread;
        }
        copy.write(data);
    }

    public boolean isConnected() {
        return currentState == ConnectionStates.STATE_CONNECTED;
    }

    public synchronized int getCurrentState() {
        return currentState;
    }

    private synchronized void cancelThreads(boolean cancelAccept, boolean cancelConnect, boolean cancelConnected) {
        if (cancelConnect && connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (cancelConnect && connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (cancelAccept && acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    public synchronized void startConnection() {
        cancelThreads(false, true, true);
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        cancelThreads(false, currentState == ConnectionStates.STATE_CONNECTING, true);
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public synchronized void runConnectedSocket() {
        cancelThreads(true, true, true);
        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    public synchronized void stopConnection() {
        cancelThreads(true, true, true);
        currentState = ConnectionStates.STATE_IDLE;
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, SERVICE_UUID);
                currentState = ConnectionStates.STATE_LISTENING;
            } catch (IOException e) {
//                TODO: snd message
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
//                    TODO: Send error message
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (currentState) {
                            case ConnectionStates.STATE_LISTENING:
                            case ConnectionStates.STATE_CONNECTING: {
                                    transferSocket = socket;
                                    runConnectedSocket();
                                }
                                break;
                            case ConnectionStates.STATE_IDLE:
                            case ConnectionStates.STATE_CONNECTED: {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
//                                        send error
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
//                        TODO: send error message
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket temp = null;
            try {
                temp = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            } catch (IOException e) {
//                TODO: send message to user
            }
            bluetoothSocket = temp;
            currentState = ConnectionStates.STATE_CONNECTING;
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                cancel();
                return;
            }
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
            transferSocket = bluetoothSocket;
            runConnectedSocket();
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
//                TODO: send proper message to user
            }
        }
    }

    private class ConnectedThread extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;
        private byte[] buffer;

        public ConnectedThread() {
            try {
                inputStream = transferSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
//                TODO: message user
            }
            try {
                outputStream = transferSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
//                TODO: message user
            }
            currentState = ConnectionStates.STATE_CONNECTED;
        }

        @Override
        public void run() {
            buffer = new byte[BUFFER_LENGTH];
            int numberOfBytes;
            int dropCount = 0;
            while (currentState == ConnectionStates.STATE_CONNECTED) {
                try {
                    numberOfBytes = inputStream.read(buffer);
                    Message readMessage = handler.obtainMessage(MessageConstants.MESSAGE_READ, numberOfBytes, -1, buffer);
                    readMessage.sendToTarget();
                } catch (IOException e) {
                    if (e.getMessage().equals("bt socket closed, read return: -1")){
                        Log.e("BLUETOOTH", "connection dropped");
                        dropCount += 1;
                        if (dropCount > 1){
                            connectionDropNotifier.sendEmptyMessage(0);
                            break;
                        }
                    }
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
//                TODO: catch and send error to the handler
            }
        }

        public void cancel() {
            try {
                transferSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
