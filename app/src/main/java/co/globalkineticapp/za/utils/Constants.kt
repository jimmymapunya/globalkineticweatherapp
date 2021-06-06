package co.globalkineticapp.za.utils

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

class Constants {

    companion object{
        const val API_KEY = "661ca68a90b623de7d9f20e4e38927b7"
        const val BASE_URL = "http://api.openweathermap.org"
        const val ICON_URL = "http://openweathermap.org/img/wn/"
        const val DEBUG = "debug"

        fun hideProgressBar(progressBar: ProgressBar){
            progressBar.visibility = View.INVISIBLE
        }
        fun showProgressBar(progressBar: ProgressBar) {
            progressBar.visibility = View.VISIBLE
        }

        fun showToast(context: Context, message: String, duration: Int){
            Toast.makeText(context, message , duration).show()
        }
    }
}