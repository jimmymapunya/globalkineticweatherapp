package co.globalkineticapp.za.ui.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.globalkineticapp.za.repository.WeatherRepository
import co.globalkineticapp.za.viewmodel.WeatherViewModel

class WeatherViewModelProviderFactory (
    private val weatherRepository: WeatherRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherViewModel(weatherRepository) as T
    }
}