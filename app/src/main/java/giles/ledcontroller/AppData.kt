package giles.ledcontroller

import android.content.Context
import giles.util.SortedArrayList
import java.io.*
import java.util.*

/**
 * Static object to store app data shared by multiple classes
 */
object AppData{
    //Set (no duplicates) to store saved colors
    var savedColors = HashSet<Int>()

    //ArrayList to store saved gradients
    var savedGradients = SortedArrayList(Gradient.Companion.GradientComparator())

    //The LightDisplay being used by the app
    var display = LightDisplay(300)

    //ArrayList to store saved patterns
    var patterns = SortedArrayList(Pattern.Companion.PatternComparator())


    /**
     * Checks whether or not the given String can be used as a name for a [Gradient].
     * A name is valid iff there are no [Gradient]s already saved with that name.
     *
     * @return true if the name is valid, false otherwise.
     */
    fun isGradientNameValid(name: String) : Boolean{
        for(gradient: Gradient in savedGradients){
            if(gradient.name == name){
                return false
            }
        }
        return true
    }

    /**
     * Functions to save data to file
     */
    fun save(context: Context){
        saveColors(context)
        saveGradients(context)
        savePatterns(context)
        saveDisplay(context)
    }
    fun saveColors(context: Context){
        //Save colors
        val colorsFileOut: FileOutputStream = context.openFileOutput(context.getString(R.string.FILE_COLORS), Context.MODE_PRIVATE)
        val colorsObjOut = ObjectOutputStream(colorsFileOut)
        colorsObjOut.writeObject(savedColors)
        colorsObjOut.close()
        colorsFileOut.close()
    }
    fun saveGradients(context: Context){
        //Save gradients
        val gradientsFileOut: FileOutputStream = context.openFileOutput(context.getString(R.string.FILE_GRADIENTS), Context.MODE_PRIVATE)
        val gradientsObjOut = ObjectOutputStream(gradientsFileOut)
        gradientsObjOut.writeObject(savedGradients)
        gradientsObjOut.close()
        gradientsFileOut.close()
    }
    fun savePatterns(context: Context){
        //Save patterns
        val patternsFileOut: FileOutputStream = context.openFileOutput(context.getString(R.string.FILE_PATTERNS), Context.MODE_PRIVATE)
        val patternsObjOut = ObjectOutputStream(patternsFileOut)
        patternsObjOut.writeObject(patterns)
        patternsObjOut.close()
        patternsFileOut.close()
    }
    fun saveDisplay(context: Context){
        //Save displays
        val displayFileOut: FileOutputStream = context.openFileOutput(context.getString(R.string.FILE_DISPLAYS), Context.MODE_PRIVATE)
        val displayObjOut = ObjectOutputStream(displayFileOut)
        displayObjOut.writeObject(display)
        displayObjOut.close()
        displayFileOut.close()
    }

    /**
     * Load data from file
     */
    //TODO: Add error handling and backwards compatibility for when classes change
    fun load(context: Context){
        //Load colors
        try{
            val colorsFileIn: FileInputStream = context.openFileInput(context.getString(R.string.FILE_COLORS))
            val colorsObjectIn = ObjectInputStream(colorsFileIn)
            this.savedColors = colorsObjectIn.readObject() as HashSet<Int>
            colorsObjectIn.close()
            colorsFileIn.close()
        } catch(ex: FileNotFoundException){
            //Do nothing
        }

        //Load gradients
        try{
            val gradientsFileIn: FileInputStream = context.openFileInput(context.getString(R.string.FILE_GRADIENTS))
            val gradientsObjectIn = ObjectInputStream(gradientsFileIn)
            this.savedGradients = gradientsObjectIn.readObject() as SortedArrayList<Gradient>
            gradientsObjectIn.close()
            gradientsFileIn.close()
        } catch(ex: FileNotFoundException){
            //Do nothing
        }

        //Load patterns
        try{
            val patternsFileIn: FileInputStream = context.openFileInput(context.getString(R.string.FILE_PATTERNS))
            val patternsObjectIn = ObjectInputStream(patternsFileIn)
            this.patterns = patternsObjectIn.readObject() as SortedArrayList<Pattern>
            patternsObjectIn.close()
            patternsFileIn.close()
        } catch(ex: FileNotFoundException){
            //Do nothing
        }

        //Load displays
        try{
            val displayFileIn: FileInputStream = context.openFileInput(context.getString(R.string.FILE_DISPLAYS))
            val displayObjectIn = ObjectInputStream(displayFileIn)
            this.display = displayObjectIn.readObject() as LightDisplay
            displayObjectIn.close()
            displayFileIn.close()
        } catch(ex: FileNotFoundException){
            //Do nothing
        }

    }
}