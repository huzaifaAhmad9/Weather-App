package com.example.weatherapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.notification.Condition
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.SearchView
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 57995ef8c75cda8ff0cd1da96c065a03
class MainActivity : AppCompatActivity() {

    // initaialize the view binding
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // fetch weatherdata
        fetchWeatherData("Lahore")
        searchCity()

        changeStatusBarColor()
        // Change navigation bar color
        changeNavigationBarColor()


    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                    // Clear focus to dismiss the keyboard
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }




    private fun fetchWeatherData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(Apiinterface::class.java)

        // response
        val response = retrofit.getWeatherData(cityName, "57995ef8c75cda8ff0cd1da96c065a03", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    val temperature  = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val wind = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise
                    val sunset = responseBody.sys.sunset
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown" // null safety is achieved by using question mark
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val rain = responseBody.main.feels_like



                    // Display weather information with slide-up animation
                        binding.temperature.apply {
                            text = "$temperature ℃"
                            startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_down))
                            startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in))
                    }



                    binding.temperature.text = "$temperature ℃"
                    binding.condition.text = condition
                    binding.min.text = "Min Temp: $minTemp ℃"
                    binding.max.text = "Max Temp: $maxTemp ℃"
                    binding.humidity.text = "$humidity %"
                    binding.rain.text = "$rain"
                    binding.speed.text = "$wind m/s"
                    binding.sunrise.text = "${time(sunrise.toLong())}"
                    binding.sunset.text = "${time(sunset.toLong())}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.city.text = "$cityName"
//                    Log.d("TAG", "onResponse: $temperature") // for checking on logcat only

                    changeConditionsAccordingToWeather(condition)
                    binding.animation.playAnimation()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun changeConditionsAccordingToWeather(condition: String) {
        when(condition){

            "Clear Sky", "Sunny" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.animation.setAnimation(R.raw.sun)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.animation.setAnimation(R.raw.cloud)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.animation.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Bliizard", "Snow" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.animation.setAnimation(R.raw.snow)
            }
            "Clear", "Smoke", "Haze" ->{
                binding.root.setBackgroundResource(R.drawable.clear)
                binding.animation.setAnimation(R.raw.cloud)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.animation.setAnimation(R.raw.sun)
            }
        }
        binding.animation.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }


    // time for sunset
    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp * 1000)
    }



    //        function for calling present date or day
    fun dayName(timestamp: Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }


    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = getColor(R.color.black) // Set your desired color
        }
    }


    private fun changeNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.navigationBarColor = getColor(R.color.black) // Set your desired color
        }
    }
}