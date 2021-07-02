package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.SaturationBar
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

    private var colorPicker: ColorPicker
    private var colorDisplay: TextView
    private var saturationLayout: LinearLayout
    private var saturationBar: SaturationBar
    private var saveButton: ImageView
    private var savedColorsLayout: LinearLayout
    private var savedColorsList: RecyclerView
    private var savedColorsAdapter: ColorViewAdapter? = null

    init {
        isClickable = false
        inflate(context, R.layout.layout_color_picker, this)

        //Get components
        colorPicker = findViewById(R.id.color_picker)
        colorDisplay = findViewById(R.id.text_color_display)
        saturationLayout = findViewById(R.id.layout_saturation)
        saveButton = findViewById(R.id.image_save_color)
        savedColorsLayout = findViewById(R.id.layout_picker_saved_colors)
        savedColorsList = findViewById(R.id.list_picker_saved_colors)

        //Set up saturation bar
        /*NOTE: The saturation bar is created here programmatically because it would not appear in the inflated view
            when it was placed directly into the XML. This is a workaround.*/
        saturationBar = SaturationBar(context, attrs, defStyle)
        saturationBar.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        saturationLayout.addView(saturationBar)

        //Set up color picker
        colorPicker.addSaturationBar(saturationBar)
        //saturationBar.setOnSaturationChangedListener { updateDisplay(color_picker.color) }
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
        colorDisplay.text = String.format("#%06X", 0xFFFFFF and color)
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

            Toast.makeText(context, "Saved color " + String.format("#%06X", 0xFFFFFF and color), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Color " + String.format("#%06X", 0xFFFFFF and color) + " is already saved", Toast.LENGTH_SHORT).show()
        }
    }
}