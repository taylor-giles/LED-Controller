package giles.views

import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout

interface Expandable{
    val expandButtonImage: Drawable
    val collapseButtonImage: Drawable
    val header: View
    val content: View?
    val button: ImageView
    var expanded: Boolean


    /**
     * Puts the dropdown in the expanded state, showing the content
     */
    fun expand(){
        expanded = true
        button.setImageDrawable(collapseButtonImage)
        if(content != null){
            content!!.visibility = VISIBLE
        }
    }


    /**
     * Puts the dropdown in the collapsed state, hiding the content
     */
    fun collapse(){
        expanded = false
        button.setImageDrawable(expandButtonImage)
        if(content != null){
            content!!.visibility = GONE
        }
    }
}