package giles.ledcontroller.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import giles.ledcontroller.R
import giles.views.Expandable

const val MENU_ITEM_FONT_SIZE = 18f
const val REDIRECT_DRAWABLE = R.drawable.ic_chevron_right_gray_32dp
const val EXPAND_DRAWABLE = R.drawable.ic_chevron_right_gray_32dp
const val COLLAPSE_DRAWABLE = R.drawable.ic_chevron_right_gray_32dp

enum class MenuItemType{
    BASIC,
    EXPANDABLE
}

/**
 * This file houses a set of classes that represent menu items,
 * i.e. the clickable rows shown on the menu that have the general form
 * [<Title>               <Arrow Image>]
 */
@Suppress("USELESS_CAST")
class MenuItem{
    val context: Context
    var title: String
    val type: MenuItemType
    val view: View

    /**
     * Constructor for basic menu items that redirect the user
     */
    constructor(context: Context, title: String){
        this.context = context
        this.title = title
        this.type = MenuItemType.BASIC

        //Create the view
        view = BasicMenuItemView(context, title)
    }

    /**
     * Constructor for expandable menu items that reveal more options
     */
    constructor(context: Context, title: String, content: View){
        this.context = context
        this.title = title
        this.type = MenuItemType.EXPANDABLE

        //Create the view
        val expandableView = ExpandableMenuItemView(context, title, content)
        expandableView.button.setOnClickListener {
            Log.d("Log", "clicked")
            if(expandableView.expanded){
                expandableView.collapse()
            } else {
                expandableView.expand()
            }
        }
        view = expandableView
    }
}

class ExpandableMenuItemView(
    context: Context,
    title: String,
    override val content: View?
) : Expandable, LinearLayout(context) {

    override val expandButtonImage: Drawable = getDrawable(context, EXPAND_DRAWABLE)!!
    override val collapseButtonImage: Drawable = getDrawable(context, COLLAPSE_DRAWABLE)!!
    override var expanded = false
    override val header: LinearLayout

    override val button = ImageView(context)
    private val titleView = TextView(context)

    init {
        //Set layout params and orientation
        this.orientation = VERTICAL
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        //Set up text view
        titleView.textSize = MENU_ITEM_FONT_SIZE
        titleView.text = title

        //Build the header
        this.header = LinearLayout(context)
        this.header.orientation = HORIZONTAL
        this.header.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val headerSpacer = View(context)
        headerSpacer.layoutParams = LayoutParams(0, 0, 1f)
        this.header.addView(titleView)
        this.header.addView(headerSpacer)
        this.header.addView(button)

        //Build the expandable view
        val spacer = View(context)
        spacer.layoutParams = LayoutParams(0, 0, 1f)
        this.addView(header)
        this.addView(spacer)
        this.addView(content)
        this.visibility = VISIBLE

        //Start collapsed
        collapse()
    }

    /**
     * Constructor taking only context, to satisfy LinearLayout requirements
     */
    constructor(context: Context): this(context, "",null)
}

/**
 * A view for menu items that redirect the user to another activity on click
 */
class BasicMenuItemView(
    context: Context
) : LinearLayout(context) {

    var title = ""
    var image = ImageView(context)
    private var titleView = TextView(context)

    init {
        //Set layout params and orientation
        this.orientation = HORIZONTAL
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        //Set up text view
        titleView.textSize = MENU_ITEM_FONT_SIZE
        titleView.text = title

        //Set up image (size handled by vector asset dimensions)
        image.setImageDrawable(getDrawable(context, REDIRECT_DRAWABLE))

        //Build the view
        val headerSpacer = View(context)
        headerSpacer.layoutParams = LayoutParams(0, 0, 1f)
        this.addView(titleView)
        this.addView(headerSpacer)
        this.addView(image)
    }

    constructor(context: Context, name: String): this(context){
        this.title = name
        titleView.text = name
    }
}