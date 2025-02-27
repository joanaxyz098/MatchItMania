package extensions

import android.view.View
import android.widget.EditText

fun String.fieldEmpty(et: EditText) : Boolean{
    if(this.isEmpty()){
        et.error = "required"
        return true
    }
    return false
}