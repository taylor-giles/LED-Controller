package giles.ledcontroller.views

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import giles.ledcontroller.R
import giles.views.Expandable

//Size values
const val NORMAL_FONT_SIZE = 18f
const val COMPACT_FONT_SIZE = 16f
const val NORMAL_HORIZONTAL_PADDING = 24f
const val COMPACT_HORIZONTAL_PADDING = 16f
const val NORMAL_VERTICAL_PADDING = 16f
const val COMPACT_VERTICAL_PADDING = 8f

//Images
const val REDIRECT_DRAWABLE = R.drawable.ic_chevron_right_gray_32dp
const val EXPAND_DRAWABLE = R.drawable.ic_expand_more_gray_32dp
const val COLLAPSE_DRAWABLE = R.drawable.ic_expand_less_gray_32dp

//Enum for the two different types of MenuItems
enum class MenuItemType{
    BASIC,
    EXPANDABLE
}

//Enum for the available sizes of MenuItemView
enum class MenuItemSize{
    NORMAL,
    COMPACT //Having a compact view means less padding and a smaller font size
}

/**
 * This file houses a set of classes that represent menu items,
 * i.e. the clickable rows shown on a menu that have the general form
 * [<Title>               <Arrow Image>]
 */
class MenuItem{
    val title: String
    val view: View
    val type: MenuItemType
    val viewSize: MenuItemSize

    /**
     * Constructor for basic menu items that redirect the user
     */
    constructor(context: Context, title: String, viewSize: MenuItemSize = MenuItemSize.NORMAL){
        this.title = title
        this.viewSize = viewSize
        this.type = MenuItemType.BASIC

        //Create the view
        view = BasicMenuItemView(context, title, viewSize)
    }

    /**
     * Constructor for expandable menu items that reveal more content
     */
    constructor(context: Context, title: String, content: View, startExpanded: Boolean = false, viewSize: MenuItemSize = MenuItemSize.NORMAL){
        this.title = title
        this.viewSize = viewSize
        this.type = MenuItemType.EXPANDABLE

        //Create the view
        val expandableView = ExpandableMenuItemView(context, title, viewSize, content, startExpanded)
        expandableView.header.setOnClickListener {
            if(expandableView.expanded){
                expandableView.collapse()
            } else {
                expandableView.expand()
            }
        }
        view = expandableView
    }
}

class ExpandableMenuItemView @JvmOverloads constructor(
    context: Context,
    title: String = "",
    size: MenuItemSize = MenuItemSize.NORMAL,
    override val content: View? = null,
    startExpanded: Boolean = false
) : Expandable, LinearLayout(context) {

    override val expandButtonImage: Drawable = getDrawable(context, EXPAND_DRAWABLE)!!
    override val collapseButtonImage: Drawable = getDrawable(context, COLLAPSE_DRAWABLE)!!
    override var expanded = false
    override val header: LinearLayout

    override val button = ImageView(context)
    private val titleView = TextView(context)

    init {
        //Set layout params, orientation, and transition
        this.orientation = VERTICAL
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutTransition = LayoutTransition()

        //Set up text view
        titleView.textSize = when(size){
            MenuItemSize.NORMAL -> NORMAL_FONT_SIZE
            MenuItemSize.COMPACT -> COMPACT_FONT_SIZE
        }
        titleView.text = title

        //Build the header
        this.header = LinearLayout(context)
        this.header.orientation = HORIZONTAL
        this.header.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.header.gravity = Gravity.CENTER
        val headerSpacer = View(context)
        headerSpacer.layoutParams = LayoutParams(0, 0, 1f)
        this.header.addView(titleView)
        this.header.addView(headerSpacer)
        this.header.addView(button)

        //Set header padding by converting dp to px (<dp> * density + 0.5f)
        val density = resources.displayMetrics.density
        val horizontalPadding = when(size){
            MenuItemSize.NORMAL -> (NORMAL_HORIZONTAL_PADDING * density + 0.5f).toInt()
            MenuItemSize.COMPACT -> (COMPACT_HORIZONTAL_PADDING * density + 0.5f).toInt()
        }
        val verticalPadding = when(size){
            MenuItemSize.NORMAL -> (NORMAL_VERTICAL_PADDING * density + 0.5f).toInt()
            MenuItemSize.COMPACT -> (COMPACT_VERTICAL_PADDING * density + 0.5f).toInt()
        }
        this.header.setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        //Build the expandable view
        val spacer = View(context)
        spacer.layoutParams = LayoutParams(0, 0, 1f)
        this.addView(header)
        this.addView(spacer)
        if(content != null){
            this.addView(content)
        }
        this.visibility = VISIBLE

        //Start expanded or collapsed
        if(startExpanded){
            expand()
        } else {
            collapse()
        }
    }
}

/**
 * A view for menu items that redirect the user to another activity on click
 */
class BasicMenuItemView @JvmOverloads constructor(
    context: Context,
    var title: String = "",
    size: MenuItemSize = MenuItemSize.NORMAL
) : LinearLayout(context) {

    private var image = ImageView(context)
    private var titleView = TextView(context)

    init {
        //Set layout params, orientation, and gravity
        this.orientation = HORIZONTAL
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.gravity = Gravity.CENTER

        //Set padding by converting dp to px (<dp> * density + 0.5f)
        val density = resources.displayMetrics.density
        val horizontalPadding = when(size){
            MenuItemSize.NORMAL -> (NORMAL_HORIZONTAL_PADDING * density + 0.5f).toInt()
            MenuItemSize.COMPACT -> (COMPACT_HORIZONTAL_PADDING * density + 0.5f).toInt()
        }
        val verticalPadding = when(size){
            MenuItemSize.NORMAL -> (NORMAL_VERTICAL_PADDING * density + 0.5f).toInt()
            MenuItemSize.COMPACT -> (COMPACT_VERTICAL_PADDING * density + 0.5f).toInt()
        }
        setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        //Set up text view
        titleView.textSize = when(size){
            MenuItemSize.NORMAL -> NORMAL_FONT_SIZE
            MenuItemSize.COMPACT -> COMPACT_FONT_SIZE
        }
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
}