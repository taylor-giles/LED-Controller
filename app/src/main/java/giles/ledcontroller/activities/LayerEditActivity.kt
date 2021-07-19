package giles.ledcontroller.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import giles.ledcontroller.Gradient
import giles.ledcontroller.GradientWaveEffect
import giles.ledcontroller.Layer
import giles.ledcontroller.R
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.GradientRectView
import java.lang.Integer.getInteger


class LayerEditActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var effectTypeSpinner: Spinner
    private lateinit var lightSelectionGroup: RadioGroup
    private lateinit var alternatingLightsLayout: ConstraintLayout
    private lateinit var advancedLightsLayout: ConstraintLayout
    private lateinit var effectOptionsLayout: LinearLayout
    private lateinit var solidColorOptionsLayout: View
    private lateinit var gradientOptionsLayout: View
    private lateinit var directionalOptionsLayout: View
    private lateinit var colorPreview: View
    private lateinit var colorPreviewText: TextView
    private lateinit var chooseColorButton: Button
    private lateinit var colorOpacityBar: SeekBar
    private lateinit var colorOpacityText: TextView
    private lateinit var gradientViewLayout: FrameLayout
    private lateinit var chooseGradientButton: Button
    private lateinit var gradientText: TextView
    private lateinit var gradientView: GradientRectView

    private var color: Int = 0
    private lateinit var gradient: Gradient
    private lateinit var direction: GradientWaveEffect.GradientWaveDirection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer_edit)

        //Get views
        saveButton = this.findViewById(R.id.btn_save_layer)
        cancelButton = this.findViewById(R.id.btn_cancel_layer)
        effectTypeSpinner = this.findViewById(R.id.spinner_effect_type)
        lightSelectionGroup = this.findViewById(R.id.group_radio_lights_selection)
        alternatingLightsLayout = this.findViewById(R.id.layout_alternating_lights_selection)
        advancedLightsLayout = this.findViewById(R.id.layout_advanced_lights_selection)
        effectOptionsLayout = this.findViewById(R.id.layout_effect_options)
        solidColorOptionsLayout = layoutInflater.inflate(R.layout.layout_solid_color_effect_options, ConstraintLayout(this), false)
        gradientOptionsLayout = layoutInflater.inflate(R.layout.layout_gradient_effect_options, ConstraintLayout(this), false)
        directionalOptionsLayout = layoutInflater.inflate(R.layout.layout_directional_effect_options, ConstraintLayout(this), false)

        //Views for Solid Color options
        colorPreview = solidColorOptionsLayout.findViewById(R.id.preview_solid_color_option_color)
        colorPreviewText = solidColorOptionsLayout.findViewById(R.id.text_solid_color_option_color)
        colorOpacityBar = solidColorOptionsLayout.findViewById(R.id.slider_solid_color_opacity)
        colorOpacityText = solidColorOptionsLayout.findViewById(R.id.text_solid_color_opacity)
        chooseColorButton = solidColorOptionsLayout.findViewById(R.id.btn_solid_color_effect_change_color)

        //Default solid color selection to black
        setColor(getColor(android.R.color.black))
        colorPreview.background = ContextCompat.getDrawable(this, R.drawable.transparent_background_bitmap)

        //Give behavior to the opacity bar and color choose button
        colorOpacityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setColor(Color.argb(progress, Color.red(color), Color.green(color), Color.blue(color)))
            }
        })
        chooseColorButton.setOnClickListener {
            //Create dialog to display color picker
            val builder = AlertDialog.Builder(this)
            val dialogPicker = ColorPickerView(this, showSaveButton = true, showSavedColors = true)
            builder.setTitle("Solid Color Effect")
                .setView(dialogPicker)
                .setPositiveButton(R.string.add_color) { _, _ -> setColor(dialogPicker.getColor()) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        //Views for Gradient effect options
        gradientViewLayout = gradientOptionsLayout.findViewById(R.id.layout_effect_gradient_preview)
        chooseGradientButton = gradientOptionsLayout.findViewById(R.id.btn_effect_choose_gradient)
        gradientText = gradientOptionsLayout.findViewById(R.id.text_effect_gradient_name)

        //Put the gradient view into the preview frame
        gradientView = GradientRectView(this)
        gradientView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        gradientViewLayout.addView(gradientView)

        //Give behavior to the gradient choose button
        chooseGradientButton.setOnClickListener {
            val intent = Intent(this, GradientSelectionActivity::class.java)
            startActivityForResult(intent, R.integer.SELECT_GRADIENT_REQUEST)
        }

        //Get the layer to be edited (if there is one)
        val givenLayer = intent.getSerializableExtra(getString(R.string.EXTRA_LAYER)) as Layer?

        //Set behavior on effect type change
        effectTypeSpinner.onItemSelectedListener = this

        //Set behavior on lights selection type change
        lightSelectionGroup.setOnCheckedChangeListener { _, checkedID ->
            //Remove both light selection layouts
            alternatingLightsLayout.visibility = View.GONE
            advancedLightsLayout.visibility = View.GONE
            when (checkedID) {
                R.id.radio_alternating_lights -> alternatingLightsLayout.visibility = View.VISIBLE
                R.id.radio_advanced_lights_selection -> advancedLightsLayout.visibility = View.VISIBLE
            }
        }

        //Set behavior for cancel button
        cancelButton.setOnClickListener { finish() }

        //Set behavior for save button
        saveButton.setOnClickListener {

        }
    }

    /**
     * Sets the views in the solidColorOptionsLayout to reflect the given color
     */
    private fun setColor(color: Int){
        this.color = color
        colorPreview.foreground = ColorDrawable(color)
        colorPreviewText.text = String.format("#%06X", 0xFFFFFF and color)
        colorOpacityText.text = Color.alpha(color).toString()
        colorOpacityBar.progress = Color.alpha(color)
    }

    /**
     * Sets the views in the gradientOptionsLayout to reflect the given gradient
     */
    private fun setGradient(gradient: Gradient){
        this.gradient = gradient
        gradientView.displayGradient(gradient)
        gradientText.text = gradient.name
    }

    /**
     * Methods for spinner selection
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        //Determine which item was selected
        val chosenItem = parent.getItemAtPosition(pos)

        effectOptionsLayout.removeAllViews()

        if (chosenItem == getString(R.string.solid_color)){
            effectOptionsLayout.addView(solidColorOptionsLayout)
        } else {
            effectOptionsLayout.addView(gradientOptionsLayout)
        }

        if(chosenItem == getString(R.string.gradient_wave)){
            effectOptionsLayout.addView(directionalOptionsLayout)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK -> when(requestCode){
                R.integer.SELECT_GRADIENT_REQUEST -> {
                    //Get gradient from extras
                    setGradient(data?.getSerializableExtra(getString(R.string.EXTRA_GRADIENT)) as Gradient)
                }
            }
        }
    }
}