package giles.ledcontroller.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import giles.ledcontroller.Layer
import giles.ledcontroller.R
import giles.ledcontroller.R.id.spinner_effect_type


class LayerEditActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var effectTypeSpinner: Spinner
    private lateinit var lightSelectionGroup: RadioGroup
    private lateinit var alternatingLightsLayout: ConstraintLayout
    private lateinit var advancedLightsLayout: ConstraintLayout
    private lateinit var effectOptionsLayout: LinearLayout
    private lateinit var solidColorOptionsLayout: View
    private lateinit var gradientOptionsLayout: View
    private lateinit var directionalOptionsLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer_edit)

        effectTypeSpinner = this.findViewById(spinner_effect_type)
        lightSelectionGroup = this.findViewById(R.id.group_radio_lights_selection)
        alternatingLightsLayout = this.findViewById(R.id.layout_alternating_lights_selection)
        advancedLightsLayout = this.findViewById(R.id.layout_advanced_lights_selection)
        effectOptionsLayout = this.findViewById(R.id.layout_effect_options)
        solidColorOptionsLayout = layoutInflater.inflate(R.layout.layout_solid_color_effect_options, ConstraintLayout(this), false)
        gradientOptionsLayout = layoutInflater.inflate(R.layout.layout_gradient_effect_options, ConstraintLayout(this), false)
        directionalOptionsLayout = layoutInflater.inflate(R.layout.layout_directional_effect_options, ConstraintLayout(this), false)


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
    }

    /**
     * Methods for spinner selection
     **/
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
}