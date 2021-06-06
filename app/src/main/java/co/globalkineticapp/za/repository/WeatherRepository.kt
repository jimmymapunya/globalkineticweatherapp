package co.globalkineticapp.za.repository

import co.globalkineticapp.za.api.RetrofitInstance.Companion.api

class WeatherRepository {

    suspend fun getWeatherInformation(latitude: Double, longitude: Double) =
        api.getWeatherInformation(latitude, longitude)
}