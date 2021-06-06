package co.globalkineticapp.za.api

import co.globalkineticapp.za.model.WeatherModel
import co.globalkineticapp.za.utils.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("/data/2.5/weather")
    suspend fun getWeatherInformation(
        @Query("lat")
        latitude: Double ,

        @Query("lon")
        longitude: Double,

        @Query("appid")
        appId: String = API_KEY

    ): Response<WeatherModel>


}