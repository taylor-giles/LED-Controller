package giles.ledcontroller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import giles.bluetooth.BluetoothDeviceItemAdapter
import kotlinx.android.synthetic.main.activity_device_select.*

class DeviceSelectActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_select)

        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
        }

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

        recycler_device_select.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycler_device_select.adapter = BluetoothDeviceItemAdapter(this, list)

        //TODO: Figure out how to do OnItemTouchListener
//        recycler_device_select.addOnItemTouchListener( RecyclerView.OnItemTouchListener{
//            val device: BluetoothDevice = list[pos]
//            val address: String = device.address
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra(resources.getString(R.string.EXTRA_ADDRESS), address)
//            startActivity(intent)
//        })
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
                Toast.makeText(this,"Enabling bluetooth was cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
