package giles.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*

class BluetoothDeviceItemAdapter(private val context : Context, private val items: ArrayList<BluetoothDevice>):
    RecyclerView.Adapter<BluetoothDeviceItemAdapter.BluetoothViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        return BluetoothViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_bluetooth_device, parent, false)
        )
    }

    /**
     * Set the title and MAC address for this view
     */
    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int){
        val device = items[position]
        holder.nameView.text = device.name
        holder.addressView.text = device.address
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class BluetoothViewHolder(view: View): RecyclerView.ViewHolder(view){
        val nameView: TextView = view.text_bluetooth_name
        val addressView: TextView = view.text_bluetooth_address
    }
}