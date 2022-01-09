package pt.hventura.loadapp

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    val optionSelected = mapOf(
        1 to R.id.option1,
        2 to R.id.option2,
        3 to R.id.option3
    )

}