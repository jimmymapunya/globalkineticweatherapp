package co.globalkineticapp.za.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import co.globalkineticapp.za.R
import co.globalkineticapp.za.databinding.ActivityWeatherBinding
import co.globalkineticapp.za.repository.WeatherRepository
import co.globalkineticapp.za.utils.Constants.Companion.showToast
import co.globalkineticapp.za.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit


class WeatherActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityWeatherBinding
    private lateinit var navController: NavController
    lateinit var viewModel: WeatherViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val weatherRepository = WeatherRepository()
        val viewModelProviderFactory = WeatherViewModelProviderFactory(weatherRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(WeatherViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        binding.bottomNavigationView.setupWithNavController(navController)

        checkPermissions()

        GlobalScope.launch {
            delay(TimeUnit.SECONDS.toMillis(3))
            withContext(Dispatchers.Main) {
                viewModel.setLatitude(lat)
                viewModel.setLongitude(lon)
            }
        }


    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@WeatherActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@WeatherActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSION_ACCESS_COARSE_LOCATION
            )
        } else {
            getLocations()
        }
    }

    private var lat: Double = 0.0
    private var lon: Double = 0.0


    @SuppressLint("MissingPermission")
    private fun getLocations() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                showToast(this, "No location, please turn device location on and try again", Toast.LENGTH_LONG)
            } else location.apply {
                lat = location.latitude
                lon = location.longitude
            }

        }.addOnFailureListener {
            showToast(this, "Failed on getting current location", Toast.LENGTH_LONG)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this@WeatherActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getLocations()
                    showToast(this, "Permission Granted", Toast.LENGTH_LONG)

                } else {
                    showToast(this, "Permission Not Granted", Toast.LENGTH_LONG)
                }
            }
        }
    }
}

private const val MY_PERMISSION_ACCESS_COARSE_LOCATION = 1000

