package co.globalkineticapp.za.ui.fragment

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.globalkineticapp.za.R
import co.globalkineticapp.za.databinding.FragmentCurrentWeatherBinding
import co.globalkineticapp.za.ui.view.WeatherActivity
import co.globalkineticapp.za.utils.Constants.Companion.ICON_URL
import co.globalkineticapp.za.utils.Constants.Companion.hideProgressBar
import co.globalkineticapp.za.utils.Constants.Companion.showProgressBar
import co.globalkineticapp.za.utils.Resource
import co.globalkineticapp.za.viewmodel.WeatherViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class CurrentWeatherFragment : Fragment(R.layout.fragment_current_weather) {

    lateinit var binding: FragmentCurrentWeatherBinding
    lateinit var viewModel: WeatherViewModel
    private lateinit var unit: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrentWeatherBinding.bind(view)
        viewModel = (activity as WeatherActivity).viewModel

        GlobalScope.launch {
            delay(TimeUnit.SECONDS.toMillis(3))
            withContext(Dispatchers.Main) {
                val lat = viewModel.getLatitude()
                val lon = viewModel.getLongitude()

                getCurrentWeather(lat!!, lon!!)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        unit = sharedPref.getString(getString(R.string.pref_key_unit), "").toString()
    }

    private fun getCurrentWeather(lat: Double, lon: Double) {

        viewModel.getWeatherInformation(lat, lon)
        viewModel.weather.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(binding.progressBar)
                    response.data?.let { weatherResponse ->
                        binding.tvCity.text = weatherResponse.name
                        binding.tvDescription.text =
                            weatherResponse.weather[0].description

                        if (unit == "Kelvin"){
                            binding.tvTemperature.text = "${weatherResponse.main.temp} \u212A"
                        }else {
                            val celsius = convertKelvinToCelsius(weatherResponse.main.temp).toInt()
                            binding.tvTemperature.text = "$celsius \u2103"
                        }

                        binding.tvWind.text = "Wind Speed: ${weatherResponse.wind.speed}km/h"
                        val icon = weatherResponse.weather[0].icon
                        val iconUrl = "$ICON_URL$icon@2x.png"

                        Glide.with(binding.imageViewConditionIcon)
                            .load(iconUrl)
                            .into(binding.imageViewConditionIcon)

                    }
                }
                is Resource.Error -> {
                    hideProgressBar(binding.progressBar)
                    response.message?.let { message ->
                       Toast.makeText(context, "An error occurred: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
            }
        })
    }


    private fun convertKelvinToCelsius(kelvin: Double): Double{
        return kelvin - 273.15
    }

}