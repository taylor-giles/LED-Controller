package giles.ledcontroller.activities

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.bluetooth.BluetoothConnection
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.activity_device_select.*
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*
import java.util.*
import kotlin.collections.ArrayList

class DeviceSelectActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_select)

        //Request to enable bluetooth if disabled
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, resources.getInteger(R.integer.ENABLE_BLUETOOTH_REQUEST))
        }

        btn_refresh.setOnClickListener{ refreshList() }
    }

    private fun refreshList(){
        val pairedDevices = bluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
        } else {
            for(device: BluetoothDevice in pairedDevices){
                list.add(device)
            }
        }

        recycler_device_select.layoutManager = LinearLayoutManager(this)
        recycler_device_select.adapter = BluetoothDeviceItemAdapter(this, list) { clickedView ->
            AppData.display.connection?.disconnect()
            AppData.display.connection = BluetoothConnection(
                UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"),
                clickedView.text_bluetooth_address.text.toString(), bluetoothAdapter)
            AppData.display.connection!!.connect(this)
            AppData.display.connection!!.sendInt(AppData.display.numLights)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == resources.getInteger(R.integer.ENABLE_BLUETOOTH_REQUEST)){
            if(resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"Bluetooth is disabled", Toast.LENGTH_SHORT).show()
                }
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this,"Enable Bluetooth to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class BluetoothDeviceItemAdapter(
    private val context : Context,
    private val items: List<BluetoothDevice>,
    private val itemOnClickListener: View.OnClickListener
): RecyclerView.Adapter<BluetoothDeviceItemAdapter.BluetoothViewHolder>() {
    class BluetoothViewHolder(val view: View): RecyclerView.ViewHolder(view){
        //Change title and MAC address display
        fun setDevice(device: BluetoothDevice){
            view.text_bluetooth_name.text = device.name
            view.text_bluetooth_address.text = device.address
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        return BluetoothViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false))
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int){
        val device = items[position]
        holder.setDevice(device)
        holder.view.setOnClickListener(itemOnClickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
