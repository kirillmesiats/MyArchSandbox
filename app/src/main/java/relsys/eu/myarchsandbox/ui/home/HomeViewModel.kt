package relsys.eu.myarchsandbox.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class HomeViewModel : ViewModel() {

    val name : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setName() {
        name.value = "John"
    }
}
