package giles.ledcontroller

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import giles.bluetooth.BluetoothSerial
import giles.ledcontroller.services.DisplayService
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Semaphore

class LightDisplay(
    val numLights: Int
) : Serializable, BluetoothSerial.BluetoothSerialListener {

    var brightness: Float = 1f
    val bluetooth = BluetoothSerial(this)
    lateinit var device: BluetoothDevice
    lateinit var currentPattern: Pattern

    //Service-related variables
    private lateinit var serviceIntent: Intent

    //Thread locking mechanism to ensure frames are sent over BT only after receiving byte over BT
    val frameSemaphore = Semaphore(0)

    @Throws(IllegalStateException::class)
    fun displayPattern(context: Context, pattern: Pattern){
        if(bluetooth.connectionState != BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED){
            throw IllegalStateException("Must have a BT connection to display a pattern")
        }

        //Stop currently running service (if it exists)
        if(this::serviceIntent.isInitialized){
            context.stopService(serviceIntent)
        }

        //Start service to send pattern data over serial
        serviceIntent = Intent(context, DisplayService::class.java)
        currentPattern = pattern
        context.startForegroundService(serviceIntent)
    }

    fun connectBluetoothDevice(device: BluetoothDevice){
        this.device = device
        bluetooth.connect(device)
    }

    override fun onConnected(device: BluetoothDevice) {
        //Send the number of lights
        val buffer1: ByteBuffer = ByteBuffer.allocate(4)
        buffer1.order(ByteOrder.BIG_ENDIAN)
        buffer1.putInt(numLights)
        bluetooth.write(buffer1.array())

        //Send the refresh rate
        val buffer2: ByteBuffer = ByteBuffer.allocate(4)
        buffer2.order(ByteOrder.BIG_ENDIAN)
        buffer2.putInt(MILLIS_BETWEEN_FRAMES)
        bluetooth.write(buffer2.array())
    }

    //Runs in the ConnectThread
    override fun onConnectionFailure() {
        // Wait 5 minutes then try again
        try{
            Thread.sleep(30000)
        } catch(ex: InterruptedException){
            return
        }

        if(bluetooth.connectionState != BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED){
            bluetooth.connect(device)
        }
    }

    //Runs in the TransmissionThread
    override fun onConnectionLost() {
        // Attempt to re-establish connection
        bluetooth.connect(device)
    }

    override fun onMessageReceived(numBytes: Int, msg: ByteArray) {
        if(numBytes == 1 && msg[0] == 32.toByte()){
            frameSemaphore.release()
        }
    }

    override fun onMessageSent(buffer: ByteArray?) { /* Do nothing */ }

    override fun onFailure(){
        //TODO: Stop currently running service (if it exists)
//        if(this::serviceIntent.isInitialized && this::currentContext.isInitialized){
//            currentContext.stopService(serviceIntent)
//        }
    }
}