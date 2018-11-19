package relsys.eu.myarchsandbox.ui.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import relsys.eu.myarchsandbox.R


class HomeViewModel : ViewModel() {

    val name : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setName() {
        name.value = "John"
    }

    fun showDetails(view: View) {
        Navigation.findNavController(view).navigate(R.id.actionToHomeDetails)
    }
}
