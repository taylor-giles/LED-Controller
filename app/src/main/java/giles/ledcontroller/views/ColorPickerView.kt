package giles.ledcontroller.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.SaturationBar
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.layout_color_picker.view.*


class ColorPickerView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
    ) : ConstraintLayout(context, attrs, defStyle){

    private var colorPicker: ColorPicker
    private var colorDisplay: TextView
    private var saturationLayout: LinearLayout
    private var saturationBar: SaturationBar

    init {
        inflate(getContext(), R.layout.layout_color_picker, this)
        colorPicker = findViewById(R.id.color_picker)
        colorDisplay = findViewById(R.id.text_color_display)
        saturationLayout = findViewById(R.id.layout_saturation)

        //Set up saturation bar
        /*NOTE: The saturation bar is created here programmatically because it would not appear in the inflated view
            when it was placed directly into the XML. This is a workaround.*/
        saturationBar = SaturationBar(context, attrs, defStyle)
        saturationBar.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        saturationLayout.addView(saturationBar)
        Log.d("Log", "Saturation bar: $saturationBar")

        colorPicker.addSaturationBar(saturationBar)
        //saturationBar.setOnSaturationChangedListener { updateDisplay(color_picker.color) }
        colorPicker.setOnColorChangedListener { updateDisplay(color_picker.color) }
        colorPicker.setTouchAnywhereOnColorWheelEnabled(true)
        colorPicker.showOldCenterColor = false

        updateDisplay(color_picker.color)
    }

    private fun updateDisplay(color: Int){
        //Update hex color label
        colorDisplay.text = String.format("#%06X", 0xFFFFFF and color)
    }

    fun setOnColorSelectedListener(listener: ColorPicker.OnColorSelectedListener){
        colorPicker.onColorSelectedListener = listener
    }

    fun getColor(): Int{
        return colorPicker.color
    }
}