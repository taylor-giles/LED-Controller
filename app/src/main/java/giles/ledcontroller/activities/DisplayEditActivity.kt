package giles.ledcontroller.activities

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
import giles.ledcontroller.AppData
import giles.ledcontroller.LightDisplay
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.activity_display_edit.*
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*
import kotlin.collections.ArrayList

class DisplayEditActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var adapter: BluetoothDeviceItemAdapter
    private var givenDisplay: LightDisplay? = null
    private var selectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_edit)

        //Request to enable bluetooth if disabled
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, resources.getInteger(R.integer.ENABLE_BLUETOOTH_REQUEST))
        }

        //Get the display to edit
        givenDisplay = intent.getSerializableExtra(getString(R.string.EXTRA_DISPLAY)) as LightDisplay?
        if(givenDisplay != null){
            edit_display_name.setText(givenDisplay!!.name)
            edit_display_num_lights.setText(givenDisplay!!.name)
            text_display_edit_device_name.text = givenDisplay!!.device.name
            selectedDevice = givenDisplay!!.device
        }

        //Set up save button behavior
        btn_save_display.setOnClickListener {
            //Make sure a device has been selected
            if(selectedDevice != null){
                //Make default name if none is given
                var name = edit_display_name.text.toString()
                if(name.isBlank() || name.isEmpty()){
                    name = "Untitled Display"
                }

                //Keep incrementing the suffix number until the name is valid
                if(!checkName(name)){
                    var suffix = 1
                    name += (suffix++).toString()
                    while(!checkName(name)){
                        name = (name.substring(0, name.length - (suffix-1).toString().length)) + (suffix++).toString()
                    }
                }

                //Remove edited display
                if(givenDisplay != null){
                    AppData.savedDisplays.remove(givenDisplay)
                }

                //Make and save display
                val display = LightDisplay(name, edit_display_num_lights.text.toString().toInt())
                AppData.savedDisplays.add(display)
                display.device = selectedDevice!!
                finish()
            } else {
                Toast.makeText(this, "Choose a Bluetooth device for this LED display", Toast.LENGTH_SHORT).show()
            }
        }

        //Set up cancel button behavior
        btn_cancel_display_edit.setOnClickListener { finish() }

        btn_refresh_device_list.setOnClickListener{ refreshList() }
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

        //Set up adapter with device item click behavior
        recycler_device_select.layoutManager = LinearLayoutManager(this)
        adapter = BluetoothDeviceItemAdapter(this, list) { clickedView ->
            adapter.selectView(clickedView as BluetoothDeviceItemAdapter.BluetoothDeviceView)
            text_display_edit_device_name.text = adapter.selectedDevice!!.name
            selectedDevice = adapter.selectedDevice
        }
        recycler_device_select.adapter = adapter
    }

    /**
     * Checks whether or not the given String can be used as a name for a [LightDisplay].
     * A name is valid iff there are no [LightDisplay]s already saved with that name.
     *
     * @return true if the name is valid, false otherwise.
     */
    private fun checkName(name: String) : Boolean{
        for(display: LightDisplay in AppData.savedDisplays){
            if(display.name == name){
                return false
            }
        }
        return true
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
