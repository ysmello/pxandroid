package com.cronus.brincadoteca.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cronus.brincadoteca.databinding.ActivityTempoBinding
import com.cronus.brincadoteca.databinding.FragmentClimaAtualBinding
import com.cronus.brincadoteca.databinding.FragmentDetalhesBinding
import com.cronus.brincadoteca.databinding.FragmentPrevisao2diasBinding
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class TempoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTempoBinding
    private var isFahrenheit = false
    private var currentTempC: Double? = null

    // Referências para os Fragments
    private var climaAtualFragment: ClimaAtualFragment? = null
    private var previsaoFragment: Previsao2DiasFragment? = null
    private var detalhesFragment: DetalhesFragment? = null

    // VARIÁVEIS DE LOCALIZAÇÃO (FUSED LOCATION PROVIDER)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationRequestCode = 1000
    private var lastKnownLocation: Pair<Double, Double>? = null

    // --- FUNÇÕES DE CONVERSÃO ---

    private fun celsiusToFahrenheit(celsius: Double): Double {
        return (celsius * 9 / 5) + 32
    }

    private fun formatTemp(temp: Double): String {
        return if (isFahrenheit) {
            String.format(Locale.getDefault(), "%.1f°F", celsiusToFahrenheit(temp))
        } else {
            String.format(Locale.getDefault(), "%.1f°C", temp)
        }
    }

    // --- LÓGICA DE LOCALIZAÇÃO ---

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permissão se não tiver
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationRequestCode
            )
        } else {
            // Permissão concedida, inicia a captura de localização
            requestLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "Permissão de localização negada. Usando Lat/Lon Padrão.", Toast.LENGTH_LONG).show()
                // Se negar, usa os valores padrão (Curitiba/PR)
                lastKnownLocation = Pair(-25.4284, -49.2733)
                fetchWeatherAndSave(lastKnownLocation!!.first, lastKnownLocation!!.second, "Localização Desconhecida")
            }
        }
    }

    // 🟢 FUNÇÃO ATUALIZADA AQUI: Alta precisão e tempo de atualização reduzido
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // Deve ter sido tratado em checkLocationPermission
        }

        // Configuração da requisição: ALTA PRECISÃO (força o AVD a reportar a localização mock)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // ALTA PRECISÃO
            5000) // 5 segundos
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // --- CHAMADA DA API (COMPLETA E CORRIGIDA CONTRA NULL POINTER) ---

    private fun fetchWeatherAndSave(lat: Double, lon: Double, cityFallback: String) {
        // Chamadas iniciais para Fragments
        climaAtualFragment?.updateWeather(temp = "--°C", condition = "Buscando dados...", city = "Lat: ${lat}, Lon: ${lon}")
        previsaoFragment?.updateForecast("Buscando previsão...")
        detalhesFragment?.updateDetails(wind = "Buscando...", humidity = "Buscando...", sunrise = "--:--", sunset = "--:--")

        Thread {
            try {
                // Chave hardcoded (CONFORME SOLICITADO)
                val apiKey = "027d89ce"
                val urlString = "https://api.hgbrasil.com/weather?key=$apiKey&lat=$lat&lon=$lon"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                connection.disconnect()

                // Parsing do JSON
                val json = JSONObject(response)
                val results = json.getJSONObject("results")

                // Dados Atuais
                val temp = results.getDouble("temp")
                val condition = results.getString("description")
                val cityName = results.getString("city_name")

                // Dados de Detalhes
                val humidity = results.getInt("humidity").toString() + "%"
                val windSpeed = results.getString("wind_speedy")
                val sunrise = results.getString("sunrise")
                val sunset = results.getString("sunset")

                // Dados de Previsão
                val forecastArray = results.getJSONArray("forecast")
                val forecast1 = forecastArray.getJSONObject(1) // Próximo dia (índice 0 é o dia atual)
                val forecast2 = forecastArray.getJSONObject(2) // Dia seguinte

                val prev1 = "${forecast1.getString("weekday")} - Max: ${forecast1.getInt("max")}°C / Min: ${forecast1.getInt("min")}°C (${forecast1.getString("description")})"
                val prev2 = "${forecast2.getString("weekday")} - Max: ${forecast2.getInt("max")}°C / Min: ${forecast2.getInt("min")}°C (${forecast2.getString("description")})"

                currentTempC = temp // Armazena a temperatura em Celsius

                // Atualiza a UI nos Fragments
                runOnUiThread {
                    climaAtualFragment?.updateWeather(
                        temp = formatTemp(temp),
                        condition = condition,
                        city = cityName
                    )
                    climaAtualFragment?.updateDetails(
                        humidity = humidity,
                        windSpeed = windSpeed,
                        sunrise = sunrise,
                        sunset = sunset
                    )

                    previsaoFragment?.updateForecast(prev1 = prev1, prev2 = prev2)

                    detalhesFragment?.updateDetails(
                        humidity = humidity,
                        wind = windSpeed,
                        sunrise = sunrise,
                        sunset = sunset
                    )
                }

            } catch (e: Exception) {
                runOnUiThread {
                    climaAtualFragment?.updateWeather(
                        temp = "Erro",
                        condition = "Falha na API ou conexão: ${e.message}",
                        city = cityFallback
                    )
                    previsaoFragment?.updateForecast("Erro ao carregar previsão.")
                }
                Log.e("TempoActivity", "Erro fetchWeather: ${e.message}", e)
            }
        }.start()
    }

    // --- ON CREATE DA ACTIVITY ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTempoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Informações do Tempo"

        // 1. Inicializa Fused Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 2. Define o Callback de Localização
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    lastKnownLocation = Pair(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this) // Para de ouvir após a primeira localização

                    // Inicia a chamada da API com Lat/Lon obtidos do GPS
                    Toast.makeText(this@TempoActivity, "Localização GPS obtida. Buscando Clima...", Toast.LENGTH_SHORT).show()
                    fetchWeatherAndSave(location.latitude, location.longitude, "Buscando Cidade...")
                }
            }
        }

        // 3. Configurar ViewPager e Fragments
        val adapter = TempoFragmentAdapter(this)
        binding.viewPager.adapter = adapter

        // Mantém a referência aos Fragments criados pelo Adapter
        climaAtualFragment = adapter.climaAtualFragment
        previsaoFragment = adapter.previsaoFragment
        detalhesFragment = adapter.detalhesFragment

        // 4. Vincular TabLayout ao ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Clima Atual"
                1 -> "Previsão"
                2 -> "Detalhes"
                else -> ""
            }
        }.attach()

        // 5. Listener do Switch (Cº ou Fº)
        binding.switchTemp.setOnCheckedChangeListener { _, isChecked ->
            isFahrenheit = isChecked
            binding.textUnidade.text = if (isFahrenheit) "Fº" else "Cº"

            // Força a atualização da temperatura em todos os Fragments afetados
            currentTempC?.let { temp ->
                climaAtualFragment?.updateTemperatureOnly(formatTemp(temp))
                previsaoFragment?.updateTemperatureUnits(isFahrenheit, temp)
            }
        }

        // 6. Listener do Botão "Voltar/Fechar"
        binding.btnVoltarTela.setOnClickListener {
            finish()
        }

        // 7. Iniciar Localização
        checkLocationPermission()
    }

    // --- Ciclo de Vida e Navegação ---

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Pausa a escuta do GPS
    }

    override fun onResume() {
        super.onResume()
        if (lastKnownLocation == null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates() // Reinicia a escuta se a permissão foi concedida mas não localizou ainda
        }
    }


    // =========================================================
    // === CLASSES INTERNAS (FRAGMENTS E ADAPTER) ===
    // =========================================================

    class ClimaAtualFragment : Fragment() {
        var _binding: FragmentClimaAtualBinding? = null
        val binding get() = _binding!!

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentClimaAtualBinding.inflate(inflater, container, false)
            return binding.root
        }

        fun updateWeather(temp: String, condition: String, city: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            binding.tvClima.text = temp
            binding.tvCondicao.text = condition
            binding.tvCidade.text = city
        }

        fun updateTemperatureOnly(temp: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            binding.tvClima.text = temp
        }

        fun updateDetails(humidity: String, windSpeed: String, sunrise: String, sunset: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            binding.tvHumidade.text = "Umidade: $humidity"
            binding.tvVento.text = "Vento: $windSpeed"
            binding.tvSunrise.text = "Nascer do Sol: $sunrise"
            binding.tvSunset.text = "Pôr do Sol: $sunset"
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

    class Previsao2DiasFragment : Fragment() {
        var _binding: FragmentPrevisao2diasBinding? = null
        val binding get() = _binding!!

        // Armazenamento Simples dos Dados para conversão futura
        private var prev1Data: String = ""
        private var prev2Data: String = ""

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentPrevisao2diasBinding.inflate(inflater, container, false)
            binding.textViewConteudo1.text = "Buscando previsão..."
            binding.textViewConteudo2.text = ""
            return binding.root
        }

        fun updateForecast(prev1: String, prev2: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            prev1Data = prev1
            prev2Data = prev2
            binding.textViewConteudo1.text = prev1Data
            binding.textViewConteudo2.text = prev2Data
        }

        fun updateForecast(message: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            binding.textViewConteudo1.text = message
            binding.textViewConteudo2.text = ""
        }

        // Função de placeholder para conversão de temperatura
        fun updateTemperatureUnits(isFahrenheit: Boolean, currentTempC: Double) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            // Em uma implementação real, seria necessário refazer o parsing do prev1Data/prev2Data
            Toast.makeText(context, "Previsão requer recálculo de Min/Max para Fº", Toast.LENGTH_SHORT).show()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

    class DetalhesFragment : Fragment() {
        var _binding: FragmentDetalhesBinding? = null
        val binding get() = _binding!!

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentDetalhesBinding.inflate(inflater, container, false)
            binding.textViewConteudo.text = "Detalhes Simples (Vento, Umidade, Nascer do Sol)"
            return binding.root
        }

        fun updateDetails(humidity: String, wind: String, sunrise: String, sunset: String) {
            if (_binding == null) return // 🟢 VERIFICAÇÃO DE SEGURANÇA
            binding.textViewConteudo.text = "Status de Vento/Umidade: OK\nVento: $wind\nUmidade: $humidity\nNascer: $sunrise\nPôr do Sol: $sunset"
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

    class TempoFragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        val climaAtualFragment = ClimaAtualFragment()
        val previsaoFragment = Previsao2DiasFragment()
        val detalhesFragment = DetalhesFragment()

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> climaAtualFragment
                1 -> previsaoFragment
                2 -> detalhesFragment
                else -> throw IllegalStateException("Posição inválida do Fragment")
            }
        }
    }
}