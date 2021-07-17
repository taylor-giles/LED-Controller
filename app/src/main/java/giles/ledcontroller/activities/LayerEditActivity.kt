package giles.ledcontroller.activities

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import giles.ledcontroller.R

class LayerEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer_edit)

        val lightSelectionGroup: RadioGroup = this.findViewById(R.id.group_radio_lights_selection)
        val alternatingLightsLayout: ConstraintLayout = this.findViewById(R.id.layout_alternating_lights_selection)
        val advancedLightsLayout: ConstraintLayout = this.findViewById(R.id.layout_advanced_lights_selection)

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
}