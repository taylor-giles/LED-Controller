package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.SVBar
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import giles.util.ColorUtils
import kotlinx.android.synthetic.main.layout_color_picker.view.*


/**
 * A custom view which uses the functionality of ColorPicker and SaturationBar to update
 * a hex display. Includes a button for saving colors to the associated AppData variable.
 */
class ColorPickerView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    showSaveButton: Boolean = true,
    showSavedColors: Boolean = true
    ) : FrameLayout(context, attrs, defStyle){

    //Components
    private val colorPicker: ColorPicker
    private val colorDisplay: TextView
    private val svBar: SVBar
    private val saveButton: Button
    private val savedColorsLayout: LinearLayout
    private val savedColorsList: RecyclerView

    //RecyclerView adapter
    private var savedColorsAdapter: ColorViewAdapter? = null

    init {
        isClickable = false
        inflate(context, R.layout.layout_color_picker, this)

        //Get components
        colorPicker = findViewById(R.id.color_picker)
        colorDisplay = findViewById(R.id.text_color_display)
        saveButton = findViewById(R.id.btn_save_color)
        svBar = findViewById(R.id.sv_bar)
        savedColorsLayout = findViewById(R.id.layout_picker_saved_colors)
        savedColorsList = findViewById(R.id.list_picker_saved_colors)

        //Set up color picker
        colorPicker.addSVBar(svBar)
        colorPicker.setOnColorChangedListener { updateDisplay(color_picker.color) }
        colorPicker.setTouchAnywhereOnColorWheelEnabled(true)
        colorPicker.showOldCenterColor = false

        //Set up save button
        if(showSaveButton){
            saveButton.setOnClickListener{ saveColor(colorPicker.color) }
        } else {
            saveButton.visibility = GONE
        }

        //Set up saved colors layout
        if(showSavedColors){
            savedColorsList.layoutManager = GridLayoutManager(context, 3)
            val sortedSavedColors = AppData.savedColors.sortedBy{ color -> ColorUtils.getHue(color) }.toTypedArray()
            savedColorsAdapter = ColorViewAdapter(sortedSavedColors)
                { view ->
                    //Select the clicked view
                    savedColorsAdapter!!.selectView(view as ColorView)

                    //Set the picker's color to be the selected saved color
                    if(savedColorsAdapter!!.selectedColor != null){
                        colorPicker.color = savedColorsAdapter!!.selectedColor!!
                    }
                }
            savedColorsList.adapter = savedColorsAdapter
        } else {
            savedColorsLayout.visibility = GONE
        }

        updateDisplay(color_picker.color)
    }

    private fun updateDisplay(color: Int){
        //Update hex color label
        colorDisplay.text = ColorUtils.getHexString(color)
    }

    fun setOnColorSelectedListener(listener: ColorPicker.OnColorSelectedListener){
        colorPicker.onColorSelectedListener = listener
    }

    fun getColor() = colorPicker.color

    private fun saveColor(color: Int){
        if(AppData.savedColors.add(color)){
            //Update the saved colors list
            savedColorsAdapter?.dataSet = AppData.savedColors.sortedBy{ savedColor -> ColorUtils.getHue(savedColor) }.toTypedArray()
            savedColorsAdapter?.notifyDataSetChanged()
            AppData.saveColors(this.context)

            Toast.makeText(context, "Saved color " + ColorUtils.getHexString(color), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Color " + ColorUtils.getHexString(color) + " is already saved", Toast.LENGTH_SHORT).show()
        }
    }
}