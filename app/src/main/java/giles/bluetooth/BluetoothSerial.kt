package giles.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothSerial(val btListener: BluetoothSerialListener) {
    // Member fields
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var transmissionThread: TransmissionThread? = null

    //Connection state
    enum class BluetoothConnectionState {
        STATE_NONE,
        STATE_LISTEN,
        STATE_CONNECTING,
        STATE_CONNECTED
    }
    @get:Synchronized
    var connectionState: BluetoothConnectionState = BluetoothConnectionState.STATE_NONE
        private set

    companion object {
        // Debugging
        private const val TAG = "BluetoothService"

        // Name for the SDP record when creating server socket
        private const val SDP_NAME = "BluetoothSerial"

        // UUID for SPP connection
        private val SERIAL_BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    interface BluetoothSerialListener {
        fun onConnected(device: BluetoothDevice)
        fun onConnectionFailure()
        fun onConnectionLost()
        fun onMessageReceived(numBytes: Int, msg: ByteArray)
        fun onMessageSent(buffer: ByteArray?)
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (transmissionThread != null) {
            transmissionThread!!.cancel()
            transmissionThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (connectionState == BluetoothConnectionState.STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread!!.cancel()
                connectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (transmissionThread != null) {
            transmissionThread!!.cancel()
            transmissionThread = null
        }

        // Start the thread to connect with the given device
        connectThread = ConnectThread(device)
        connectThread!!.start()
    }

    /**
     * Start the TransmissionThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (transmissionThread != null) {
            transmissionThread!!.cancel()
            transmissionThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        transmissionThread = TransmissionThread(socket)
        transmissionThread!!.start()

        btListener.onConnected(device)
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (transmissionThread != null) {
            transmissionThread!!.cancel()
            transmissionThread = null
        }
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }
        connectionState = BluetoothConnectionState.STATE_NONE
    }

    /**
     * Write to the TransmissionThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see TransmissionThread.write
     */
    fun write(out: ByteArray?) {
        // Create temporary object
        var r: TransmissionThread?
        // Synchronize a copy of the TransmissionThread
        synchronized(this) {
            if (connectionState != BluetoothConnectionState.STATE_CONNECTED) return
            r = transmissionThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Run on connection attempt failure
     */
    private fun connectionFailed() {
        connectionState = BluetoothConnectionState.STATE_NONE
        btListener.onConnectionFailure()
    }

    /**
     * Run when existing connection is lost
     */
    private fun connectionLost() {
        connectionState = BluetoothConnectionState.STATE_NONE
        btListener.onConnectionLost()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    //TODO: Get rid of this class (not needed?)
    private inner class AcceptThread : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, SERIAL_BT_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "listen() failed", e)
            }
            mmServerSocket = tmp
            connectionState = BluetoothConnectionState.STATE_LISTEN
        }

        override fun run() {
            name = "AcceptThread"
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (connectionState != BluetoothConnectionState.STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "AcceptThread failed", e)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothSerial) {
                        when (connectionState) {
                            BluetoothConnectionState.STATE_LISTEN, BluetoothConnectionState.STATE_CONNECTING ->
                                connected(socket, socket.remoteDevice)
                            BluetoothConnectionState.STATE_NONE, BluetoothConnectionState.STATE_CONNECTED -> {
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", e)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of server failed", e)
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        override fun run() {
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothSerial) { connectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "close() of connect socket failed", e
                )
            }
        }

        init {
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(SERIAL_BT_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket create() failed", e)
            }
            mmSocket = tmp
            connectionState = BluetoothConnectionState.STATE_CONNECTING
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     * (Previously called ConnectedThread)
     */
    private inner class TransmissionThread(private val socket: BluetoothSocket?) :
        Thread() {
        private val inStream: InputStream?
        private val outStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            inStream = tmpIn
            outStream = tmpOut
            connectionState = BluetoothConnectionState.STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN Transmission Thread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (connectionState == BluetoothConnectionState.STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = inStream!!.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    btListener.onMessageReceived(bytes, buffer)
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                outStream!!.write(buffer)
                btListener.onMessageSent(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }
}