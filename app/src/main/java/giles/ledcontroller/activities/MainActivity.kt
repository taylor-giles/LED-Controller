package giles.ledcontroller.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.larswerkman.holocolorpicker.ColorPicker
import giles.ledcontroller.*
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.MenuItem
import giles.util.ColorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppData.load(this)

        text_device_connected.setOnClickListener{
            val deviceIntent = Intent(this, DeviceSelectActivity::class.java)
            startActivity(deviceIntent)
        }

        //Brightness bar
        bar_brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                AppData.display.brightness = progress.toFloat() / seekBar.max.toFloat()
            }
        })

        //Set up color picker
        val colorPickerView = ColorPickerView(this, showSavedColors = false)
        val colorPickerListener = ColorPicker.OnColorSelectedListener { color: Int -> colorChange(color)}
        colorPickerView.setOnColorSelectedListener(colorPickerListener)

        //Add manual color selection menu item
        val manualPickerItem = MenuItem(this, "Manual Color Selection", colorPickerView, true)

        //Add saved colors menu item
        val savedColorsMenuItem = MenuItem(this, "Saved Colors & Gradients")
        val savedColorsIntent = Intent(this, SavedColorsActivity::class.java)
        savedColorsMenuItem.view.setOnClickListener{
            startActivityForResult(savedColorsIntent, resources.getInteger(R.integer.SAVED_COLORS_REQUEST))
        }

        //Add patterns menu item
        val patternsMenuItem = MenuItem(this, "Patterns")
        val patternsIntent = Intent(this, PatternsActivity::class.java)
        patternsMenuItem.view.setOnClickListener{
            startActivityForResult(patternsIntent, resources.getInteger(R.integer.PATTERNS_REQUEST))
        }

        //Add notifications menu item
        val notificationsMenuItem = MenuItem(this, "Notification Lighting")

        val menuItems = arrayOf(manualPickerItem, savedColorsMenuItem, patternsMenuItem, notificationsMenuItem)

        for(item: MenuItem in menuItems){
            //Create divider
            val divider = View(this)
            divider.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    (1.5f * resources.displayMetrics.density).toInt())
            val ta = obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
            divider.background = ta.getDrawable(0)
            ta.recycle()

            //Add the menu view and the divider to the layout
            layout_menu_items.addView(item.view)
            layout_menu_items.addView(divider)
        }

        //address = intent.getStringExtra(resources.getString(R.string.EXTRA_ADDRESS))
        //BluetoothConnection.connect(address, uuid, this)
    }

    //Display a solid color
    private fun colorChange(color: Int){
        //Create a temp SolidColorEffect pattern to display this color on all lights
        val lights = ArrayList<Int>()
        for(i in 0 until AppData.display.numLights){
            lights.add(i)
        }
        val layers = ArrayList<Layer>()
        layers.add(Layer(SolidColorEffect(color), lights))

        //Display the color
        display(Pattern(ColorUtils.getHexString(color), layers))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK ->
                when(requestCode){
                    //Saved Colors activity - a color was selected
                    resources.getInteger(R.integer.SAVED_COLORS_REQUEST) ->
                        colorChange(data!!.getIntExtra(getString(R.string.EXTRA_COLOR), 0))

                    //Patterns activity - a pattern was selected
                    resources.getInteger(R.integer.PATTERNS_REQUEST) ->
                        display(data!!.getSerializableExtra(getString(R.string.EXTRA_PATTERN)) as Pattern)
                }
        }
    }

    fun display(pattern: Pattern){
        try{
            //Display the pattern
            AppData.display.displayPattern(this, pattern)
            text_current_display.text = pattern.name
        } catch(ex: IllegalStateException) {
            //The display is not connected
            Toast.makeText(this, "Connect a display device before selecting a connection", Toast.LENGTH_SHORT).show()
            text_current_display.text = getString(R.string.none)
        }
    }
}
