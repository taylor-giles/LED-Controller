package giles.ledcontroller.activities

import android.app.Activity
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
import com.google.android.material.textfield.TextInputEditText
import giles.ledcontroller.*
import giles.ledcontroller.views.ColorPickerView
import giles.ledcontroller.views.GradientRectView
import kotlinx.android.synthetic.main.activity_layer_edit.*
import kotlin.math.pow
import kotlin.math.roundToInt
import android.widget.Toast

import android.widget.SeekBar

import android.widget.SeekBar.OnSeekBarChangeListener
import giles.util.ColorUtils
import kotlinx.android.synthetic.main.layout_directional_effect_options.*
import kotlin.math.log2

class LayerEditActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var effectTypeSpinner: Spinner
    private lateinit var lightSelectionGroup: RadioGroup
    private lateinit var directionGroup: RadioGroup
    private lateinit var alternatingLightsLayout: ConstraintLayout
    private lateinit var advancedLightsLayout: ConstraintLayout
    private lateinit var effectOptionsLayout: LinearLayout
    private lateinit var solidColorOptionsLayout: View
    private lateinit var gradientOptionsLayout: View
    private lateinit var directionalOptionsLayout: View
    private lateinit var speedOptionsLayout: View
    private lateinit var advancedSelectionText: TextInputEditText
    private lateinit var colorPreview: View
    private lateinit var colorPreviewText: TextView
    private lateinit var chooseColorButton: Button
    private lateinit var colorOpacityBar: SeekBar
    private lateinit var colorOpacityText: TextView
    private lateinit var gradientViewLayout: FrameLayout
    private lateinit var chooseGradientButton: Button
    private lateinit var gradientText: TextView
    private lateinit var gradientView: GradientRectView
    private lateinit var speedSlider: SeekBar

    private var color: Int = 0
    private lateinit var gradient: Gradient
    private var direction: EffectDirection = EffectDirection.START_TO_END
    private var numLights: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer_edit)

        //Get views
        saveButton = this.findViewById(R.id.btn_save_layer)
        cancelButton = this.findViewById(R.id.btn_cancel_layer)
        effectTypeSpinner = this.findViewById(R.id.spinner_effect_type)
        advancedSelectionText = this.findViewById(R.id.input_advanced_selection)
        lightSelectionGroup = this.findViewById(R.id.group_radio_lights_selection)
        alternatingLightsLayout = this.findViewById(R.id.layout_alternating_lights_selection)
        advancedLightsLayout = this.findViewById(R.id.layout_advanced_lights_selection)
        effectOptionsLayout = this.findViewById(R.id.layout_effect_options)
        solidColorOptionsLayout = layoutInflater.inflate(R.layout.layout_solid_color_effect_options, ConstraintLayout(this), false)
        gradientOptionsLayout = layoutInflater.inflate(R.layout.layout_gradient_effect_options, ConstraintLayout(this), false)
        directionalOptionsLayout = layoutInflater.inflate(R.layout.layout_directional_effect_options, ConstraintLayout(this), false)
        speedOptionsLayout = layoutInflater.inflate(R.layout.layout_speed_effect_options, ConstraintLayout(this), false)

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
        colorOpacityBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
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

        //Views for directional effect options
        directionGroup = directionalOptionsLayout.findViewById(R.id.group_radio_direction_options)

        //Set behavior on direction change
        directionGroup.setOnCheckedChangeListener { _, checkedID ->
            when (checkedID) {
                R.id.radio_effect_start_to_end -> direction = EffectDirection.START_TO_END
                R.id.radio_effect_end_to_start -> direction = EffectDirection.END_TO_START
            }
        }

        //Views for delay options
//        delayText = delayOptionsLayout.findViewById(R.id.edit_text_delay_seconds)

        //Views for speed options
        speedSlider = speedOptionsLayout.findViewById(R.id.slider_effect_speed)
        speedSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //Set the duration display when the slider is moved
                speedOptionsLayout.findViewById<TextView>(R.id.text_layer_duration_display).text =
                    (MIN_EFFECT_DURATION * 2f.pow(speedSlider.max - speedSlider.progress)).roundToInt().toString()
            }
        })

        //Get the layer to be edited (if there is one)
        val givenLayer = intent.getSerializableExtra(getString(R.string.EXTRA_LAYER)) as Layer?
        if(givenLayer != null){
            effectTypeSpinner.setSelection((effectTypeSpinner.adapter as ArrayAdapter<String>).getPosition(givenLayer.effect.title))

            //Set color or gradient
            if(givenLayer.effect is GradientEffect){
                setGradient((givenLayer.effect as GradientEffect).gradient)
            } else {
                setColor((givenLayer.effect as ColorEffect).color)
            }

            //Set speed
            if(givenLayer.effect is DynamicEffect){
                speedSlider.progress = (10 - log2(((givenLayer.effect as DynamicEffect).duration / MIN_EFFECT_DURATION).toDouble())).toInt()
                speedOptionsLayout.findViewById<TextView>(R.id.text_layer_duration_display).text =
                    (MIN_EFFECT_DURATION * 2f.pow(speedSlider.max - speedSlider.progress)).roundToInt().toString()
            }

            //Set direction
            if(givenLayer.effect is GradientWaveEffect){
                when((givenLayer.effect as GradientWaveEffect).direction){
                    EffectDirection.START_TO_END -> radio_effect_start_to_end.isSelected = true
                    EffectDirection.END_TO_START -> radio_effect_end_to_start.isSelected = true
                }
            }
        }

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
            //Get values
            val duration = (MIN_EFFECT_DURATION * 2f.pow(speedSlider.max - speedSlider.progress)).roundToInt()
//            val delay = delayText.text.toString().toFloat()

            //Make sure the gradient is initialized if it needs to be
            if(!this::gradient.isInitialized && effectTypeSpinner.selectedItem != getString(R.string.solid_color)){
                Toast.makeText(this, "Please select a gradient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Make the effect
            val effect: Effect = when(effectTypeSpinner.selectedItem){
                getString(R.string.solid_gradient) -> { SolidGradientEffect(gradient) }
                getString(R.string.gradient_cycle) -> { GradientCycleEffect(gradient, duration) }
                getString(R.string.gradient_wave) -> { GradientWaveEffect(gradient, duration, direction) }
                else -> { SolidColorEffect(color) }
            }

            //Get the lights selection
            val lightsSelection = ArrayList<Int>()
            when(lightSelectionGroup.checkedRadioButtonId){
                R.id.radio_alternating_lights -> {
                    val numOn = edit_text_lights_on.text.toString().toInt()
                    val numOff = edit_text_lights_off.text.toString().toInt()

                    var counter = 0
                    while(counter < numLights){
                        for(i in 0 until numOn){
                            if(++counter < numLights){
                                lightsSelection.add(counter)
                            }
                        }
                        counter += numOff
                    }
                }

                //TODO: Advanced lights selection

                else -> {
                    for(i in 0 until numLights){
                        lightsSelection.add(i)
                    }
                }
            }

            //Build the layer and return from this activity with the layer as an extra
            val returnIntent = Intent()
            returnIntent.putExtra(getString(R.string.EXTRA_LAYER), Layer(effect, lightsSelection))
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    /**
     * Sets the views in the solidColorOptionsLayout to reflect the given color
     */
    private fun setColor(color: Int){
        this.color = color
        colorPreview.foreground = ColorDrawable(color)
        colorPreviewText.text = ColorUtils.getHexString(color)
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

        //Color or gradient options
        if (chosenItem == getString(R.string.solid_color)){
            effectOptionsLayout.addView(solidColorOptionsLayout)
        } else {
            effectOptionsLayout.addView(gradientOptionsLayout)
        }

        //Directional options
        if(chosenItem == getString(R.string.gradient_wave)){
            effectOptionsLayout.addView(directionalOptionsLayout)
        }

        //Speed options
        when(chosenItem){
            getString(R.string.gradient_wave) -> effectOptionsLayout.addView(speedOptionsLayout)
            getString(R.string.gradient_cycle) -> effectOptionsLayout.addView(speedOptionsLayout)
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