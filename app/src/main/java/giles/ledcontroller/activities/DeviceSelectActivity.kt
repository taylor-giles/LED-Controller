package giles.ledcontroller.activities

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harrysoft.androidbluetoothserial.BluetoothManager
import giles.bluetooth.BluetoothConnection
import giles.ledcontroller.AppData
import giles.ledcontroller.LightDisplay
import giles.ledcontroller.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_device_select.*
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*
import java.util.*
import kotlin.collections.ArrayList

class DeviceSelectActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var adapter: BluetoothDeviceItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_select)

        //Request to enable bluetooth if disabled
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, resources.getInteger(R.integer.ENABLE_BLUETOOTH_REQUEST))
        }

        btn_refresh.setOnClickListener{ refreshList() }
        refreshList()
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
        adapter = BluetoothDeviceItemAdapter(this, list) { clickedView ->
            adapter.selectView(clickedView as BluetoothDeviceItemAdapter.BluetoothDeviceView)
            AppData.display.bluetooth.connect(adapter.selectedDevice!!)
            finish()
        }
        recycler_device_select.adapter = adapter
    }
}

class BluetoothDeviceItemAdapter(
    private val context : Context,
    private val items: List<BluetoothDevice>,
    private val itemOnClickListener: View.OnClickListener
): RecyclerView.Adapter<BluetoothDeviceItemAdapter.BluetoothViewHolder>() {
    var selectedDevice: BluetoothDevice? = null

    class BluetoothViewHolder(val view: BluetoothDeviceView): RecyclerView.ViewHolder(view){
        //Change title and MAC address display
        fun setDevice(device: BluetoothDevice){
            view.device = device
            view.text_bluetooth_name.text = device.name
            view.text_bluetooth_address.text = device.address
        }
    }

    class BluetoothDeviceView @JvmOverloads constructor(
        context : Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        var device: BluetoothDevice? = null
    ): FrameLayout(context, attrs, defStyle)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false)
        val deviceView = BluetoothDeviceView(context)
        deviceView.addView(view)
        return BluetoothViewHolder(deviceView)
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int){
        val device = items[position]
        holder.setDevice(device)
        holder.view.setOnClickListener(itemOnClickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun selectView(view: BluetoothDeviceView){
        selectedDevice = view.device
    }
}
