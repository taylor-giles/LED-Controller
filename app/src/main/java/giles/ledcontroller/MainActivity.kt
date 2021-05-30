package giles.ledcontroller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)


        //Set up color picker
        val colorPickerView = ColorPickerView(this)
//        colorPickerView.setOnColorSelectedListener(ColorPicker.OnColorSelectedListener{
//            colorChange(colorPickerView.getColor())
//        })

        //Add manual color selection menu item
        val manualPickerItem = MenuItem(this, "Manual Color Selection", colorPickerView, true)

        //Add saved colors menu item
        val savedColorsMenuItem = MenuItem(this, "Saved Colors")
        var savedColorsIntent = Intent(this, SavedColorsActivity::class.java)
        savedColorsMenuItem.view.setOnClickListener{startActivity(savedColorsIntent)}

        //Add patterns menu item
        val patternsMenuItem = MenuItem(this, "Patterns")

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

    private fun colorChange(color: Int){
        //Update current display message
        text_current_display.text = String.format("#%06X", 0xFFFFFF and color)

        //Change brightness bar color
        //bar_brightness.thumb.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}
