package giles.ledcontroller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import giles.ledcontroller.R
import kotlinx.android.synthetic.main.activity_saved_colors.*

const val NUM_FRAGMENTS = 2
class SavedColorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_colors)

        val adapter = ViewPagerAdapter(this)
        viewpager_saved_colors.adapter = adapter

        //Create tabs
        val tabNames = arrayOf(getString(R.string.colors), getString(R.string.gradients))
        TabLayoutMediator(tabs_saved_colors, viewpager_saved_colors) { tab, position ->
            tab.text = tabNames[position]
        }.attach()
    }
}

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = NUM_FRAGMENTS

    override fun createFragment(position: Int): Fragment {
        return when(position){
            1 -> { SavedGradientsFragment() }
            else -> { SavedColorsFragment() }
        }
    }

}
