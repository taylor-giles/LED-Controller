package giles.ledcontroller

import android.app.*
import android.content.Context
import android.content.Intent
import giles.bluetooth.BluetoothConnection
import giles.ledcontroller.services.DisplayService
import java.io.Serializable

class LightDisplay(
    val numLights: Int,
    var connection: BluetoothConnection? = null
) : Serializable {

    var brightness: Float = 1f

    fun displayPattern(context: Context, pattern: Pattern){
        if(connection == null){
            throw IllegalStateException("Must have a BT connection to display a pattern")
        }
        val serviceIntent = Intent(context, DisplayService::class.java)
        serviceIntent.putExtra(context.getString(R.string.EXTRA_PATTERN), pattern)
        context.startService(serviceIntent)
    }
}