package giles.ledcontroller.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.activity_patterns.*

class PatternsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patterns)

        //Set add button behavior
        val patternEditIntent = Intent(this, PatternEditActivity::class.java)
        val addButton = fab_add_pattern
        addButton.setOnClickListener {
            startActivity(patternEditIntent)
        }
    }
}

