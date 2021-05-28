package giles.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import java.io.IOException
import java.util.*

object BluetoothConnection{
    var isConnected = false
    lateinit var uuid: UUID
    lateinit var address: String
    lateinit var name: String
    private var bluetoothSocket: BluetoothSocket? = null


    fun sendCommand(input: String){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e : IOException){
                e.printStackTrace()
            }
        } else {
            throw NullPointerException("Must call connect() before calling sendCommand()")
        }

    }


    fun disconnect(){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch(e: IOException){
                e.printStackTrace()
            }
        } else {
            throw NullPointerException("Must call connect() before calling sendCommand()")
        }
    }


    fun connect(context: Context){
        ConnectToDevice(address, uuid)
        if(isConnected){
            Toast.makeText(context, "Successfully connected to device", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_SHORT).show()
        }
    }


    private class ConnectToDevice(private var address: String?, private var uuid: UUID?): AsyncTask<Void, Void, String>(){
        private var connectSuccess = true
        private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        override fun onPreExecute(){
            super.onPreExecute()
            //progress = ProgressDialog.show(context, "Connecting...", "Please Wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try{
                if(!isConnected){
                    val device = bluetoothAdapter.getRemoteDevice(address)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch(e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            isConnected = connectSuccess
            //progress.dismiss()
        }
    }
}