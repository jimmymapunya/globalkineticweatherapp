package co.globalkineticapp.za.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.globalkineticapp.za.model.WeatherModel
import co.globalkineticapp.za.repository.WeatherRepository
import co.globalkineticapp.za.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val weather: MutableLiveData<Resource<WeatherModel>> = MutableLiveData()
    var weatherResponse: WeatherModel? = null

    fun getWeatherInformation(lat: Double, lon: Double) = viewModelScope.launch {
        weather.postValue(Resource.Loading())
        val response = weatherRepository.getWeatherInformation(lat, lon)
        weather.postValue(handleWeatherResponse(response))
    }

    private fun handleWeatherResponse(response: Response<WeatherModel>) : Resource<WeatherModel> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (weatherResponse == null){
                    weatherResponse = resultResponse
                }
                return Resource.Success(weatherResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
 }