package giles.ledcontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import giles.ledcontroller.views.ColorPickerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        //Set up color picker
        val colorPickerView = ColorPickerView(this)
//        colorPickerView.setOnColorSelectedListener(ColorPicker.OnColorSelectedListener{
//            colorChange(colorPickerView.getColor())
//        })

        //Add menu item
        //val manualPickerItem = MenuItem(this, "Manual Color Selection", colorPickerView)
        layout_menu_items.addView(colorPickerView)

        //address = intent.getStringExtra(resources.getString(R.string.EXTRA_ADDRESS))
        //BluetoothConnection.connect(address, uuid, this)
    }

    private fun colorChange(color: Int){
        //Update current display message
        text_current_display.text = String.format("#%06X", 0xFFFFFF and color)

        //Change brightness bar color
        //bar_brightness.thumb.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}
