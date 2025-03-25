package extensions

import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

fun String.fieldEmpty(et: EditText, message: String) : Boolean{
    if(this.isEmpty()){
        et.error = "message"
        return true
    }
    return false
}
