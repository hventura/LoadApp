package pt.hventura.loadapp.utilities

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("colorizeText")
fun TextView.colorizeText(text: String) {
    if (text == "Fail") {
        this.setTextColor(Color.RED)
    } else {
        this.setTextColor(Color.BLACK)
    }
}