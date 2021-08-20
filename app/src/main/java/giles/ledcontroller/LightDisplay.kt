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
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class LightDisplay(
    val numLights: Int
) : Serializable, BluetoothSerial.BluetoothSerialListener {

    var brightness: Float = 1f
    val bluetooth = BluetoothSerial(this)

    //Thread locking mechanism to ensure frames are sent over BT only after receiving byte over BT
    val frameSemaphore = Semaphore(1)

    fun displayPattern(context: Context, pattern: Pattern){
        if(bluetooth.connectionState != BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED){
            throw IllegalStateException("Must have a BT connection to display a pattern")
        }

        //Start service to send pattern data over serial
        val serviceIntent = Intent(context, DisplayService::class.java)
        serviceIntent.putExtra(context.getString(R.string.EXTRA_PATTERN), pattern)
        context.startService(serviceIntent)
    }

    override fun onConnected(device: BluetoothDevice) {
        //Send the number of lights
        val buffer: ByteBuffer = ByteBuffer.allocate(4)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(numLights)
        val result: ByteArray = buffer.array()
        bluetooth.write(result)
    }

    override fun onConnectionFailure() {
        // Start the service over to restart listening mode
        bluetooth.start()
    }

    override fun onConnectionLost() {
        // Start the service over to restart listening mode
        bluetooth.start()
    }

    override fun onMessageReceived(numBytes: Int, msg: ByteArray) {
        if(numBytes == 1 && msg[0] == 32.toByte()){
            frameSemaphore.release()
        }
    }

    override fun onMessageSent(buffer: ByteArray?) { /* Do nothing */ }
}