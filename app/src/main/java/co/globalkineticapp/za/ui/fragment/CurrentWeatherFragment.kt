package co.globalkineticapp.za.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.globalkineticapp.za.R
import co.globalkineticapp.za.databinding.FragmentCurrentWeatherBinding
import co.globalkineticapp.za.location.LocationViewModel
import co.globalkineticapp.za.ui.view.WeatherActivity
import co.globalkineticapp.za.utils.Constants.Companion.ICON_URL
import co.globalkineticapp.za.utils.Constants.Companion.hideProgressBar
import co.globalkineticapp.za.utils.Constants.Companion.showProgressBar
import co.globalkineticapp.za.utils.Constants.Companion.showToast
import co.globalkineticapp.za.utils.GPS_REQUEST
import co.globalkineticapp.za.utils.GpsUtils
import co.globalkineticapp.za.utils.LOCATION_REQUEST
import co.globalkineticapp.za.utils.Resource
import co.globalkineticapp.za.viewmodel.WeatherViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class CurrentWeatherFragment : Fragment(R.layout.fragment_current_weather) {

    lateinit var binding: FragmentCurrentWeatherBinding
    lateinit var viewModel: WeatherViewModel
    private lateinit var unit: String

    private lateinit var locationViewModel: LocationViewModel
    private var isGPSEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrentWeatherBinding.bind(view)

        viewModel = (activity as WeatherActivity).viewModel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        GlobalScope.launch {
            delay(TimeUnit.SECONDS.toMillis(3))
            withContext(Dispatchers.Main) {
                locationViewModel.getLocationData().observe(viewLifecycleOwner, {
                    getCurrentWeather(it.latitude, it.longitude)
                })
            }
        }

        GpsUtils(context as Activity).turnGPSOn(object : GpsUtils.OnGpsListener {

            override fun gpsStatus(isGPSEnable: Boolean) {
                isGPSEnabled = isGPSEnable
            }
        })

        initListener()

    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        unit = sharedPref.getString(getString(R.string.pref_key_unit), "").toString()
    }

    private fun initListener(){
        binding.btnRefresh.setOnClickListener {
            startLocationUpdate()
        }
    }

    //Get current weather by location
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
                        showToast(context as Activity, "An error occurred: $message", Toast.LENGTH_LONG)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(binding.progressBar)
                }
            }
        })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                invokeLocationAction()
            }
        }
    }

    private fun invokeLocationAction() {
        when {
            !isGPSEnabled -> binding.tvTemperature.text = getString(R.string.enable_gps)

            isPermissionsGranted() -> startLocationUpdate()

            shouldShowRequestPermissionRationale() -> binding.tvTemperature.text = getString(R.string.updated)

            else -> ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST
            )
        }
    }

    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            getCurrentWeather(it.latitude, it.longitude)
        })
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            context as Activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context as Activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                invokeLocationAction()
            }
        }
    }

    private fun convertKelvinToCelsius(kelvin: Double): Double{
        return kelvin - 273.15
    }



}