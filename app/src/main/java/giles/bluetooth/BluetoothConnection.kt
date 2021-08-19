package giles.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.Toast
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BluetoothConnection (val uuid: UUID, val address: String, val adapter: BluetoothAdapter){
    private var bluetoothSocket: BluetoothSocket? = null
    private val device = adapter.getRemoteDevice(address)

    fun sendInt(message: Int){
        val b: ByteBuffer = ByteBuffer.allocate(4)
        b.order(ByteOrder.BIG_ENDIAN)
        b.putInt(message)
        val result: ByteArray = b.array()
        sendByteArray(result)
    }

    fun sendByteArray(message: ByteArray){
        if(isConnected()){
            try{
                bluetoothSocket!!.outputStream.write(message)
            } catch(e : IOException){
                e.printStackTrace()
            }
        } else {
            throw NullPointerException("Cannot send message while disconnected")
        }
    }

    fun sendOneByte(message: Int){
        if(isConnected()){
            try{
                bluetoothSocket!!.outputStream.write(message)
            } catch(e : IOException){
                e.printStackTrace()
            }
        } else {
            throw NullPointerException("Cannot send message while disconnected")
        }
    }

    fun sendOneByte(message: Byte){
        sendByteArray(byteArrayOf(message))
    }

    fun disconnect(){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket!!.close()
                bluetoothSocket = null
            } catch(e: IOException){
                e.printStackTrace()
            }
        } else {
            throw NullPointerException("Cannot disconnect a null socket")
        }
    }

    fun connect(context: Context){
        //TODO: This does not get run because I am creating a new BluetoothConnection object rather than changing this one.
        if(isConnected()){
            disconnect()
        }
        try{
            if(!isConnected()){
                adapter.cancelDiscovery()
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                bluetoothSocket!!.connect()
            }
        } catch(e: IOException){
            e.printStackTrace()
        }
        if(isConnected()){
            Toast.makeText(context, "Successfully connected to device", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_SHORT).show()
        }
    }

    fun isConnected(): Boolean {
        return bluetoothSocket != null
    }
}