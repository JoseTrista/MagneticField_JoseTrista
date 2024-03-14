package trista.josecarlos.magneticfield_josetrista

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(){
    private lateinit var manejadorSensor: SensorManager
    private var sensorMagnetico: Sensor? = null

    // Barras de progreso para los ejes X, Y, Z
    private lateinit var barraProgresoX: ProgressBar
    private lateinit var barraProgresoY: ProgressBar
    private lateinit var barraProgresoZ: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos las barras de progreso y el sensor magnético
        barraProgresoX = findViewById(R.id.progressBarX)
        barraProgresoY = findViewById(R.id.progressBarY)
        barraProgresoZ = findViewById(R.id.progressBarZ)

        manejadorSensor = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorMagnetico = manejadorSensor.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        manejadorSensor.registerListener(escuchaSensor, sensorMagnetico, SensorManager.SENSOR_DELAY_NORMAL)

        // Mostramos los últimos valores guardados
        val prefs = getSharedPreferences("DatosCampoMagnetico", Context.MODE_PRIVATE)
        mostrarUltimosValores(prefs)
    }

    private fun mostrarUltimosValores(prefs: SharedPreferences) {
        val ultimoX = prefs.getFloat("ultimoX", 0.0f)
        val ultimoY = prefs.getFloat("ultimoY", 0.0f)
        val ultimoZ = prefs.getFloat("ultimoZ", 0.0f)

        findViewById<TextView>(R.id.lastValueX).text = "Último X: $ultimoX"
        findViewById<TextView>(R.id.lastValueY).text = "Último Y: $ultimoY"
        findViewById<TextView>(R.id.lastValueZ).text = "Último Z: $ultimoZ"
    }

    private val escuchaSensor: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(evento: SensorEvent) {
            val x = Math.abs(evento.values[0])
            val y = Math.abs(evento.values[1])
            val z = Math.abs(evento.values[2])

            // Actualizamos la interfaz y guardamos los nuevos valores
            actualizarInterfaz(x, y, z)
            guardarValores(x, y, z)
        }

        override fun onAccuracyChanged(sensor: Sensor?, precision: Int) {}
    }

    private fun actualizarInterfaz(x: Float, y: Float, z: Float) {
        findViewById<TextView>(R.id.textViewX).text = "X: $x"
        findViewById<TextView>(R.id.textViewY).text = "Y: $y"
        findViewById<TextView>(R.id.textViewZ).text = "Z: $z"

        barraProgresoX.progress = (x / CAMPO_MAGNETICO_MAX * 100).toInt()
        barraProgresoY.progress = (y / CAMPO_MAGNETICO_MAX * 100).toInt()
        barraProgresoZ.progress = (z / CAMPO_MAGNETICO_MAX * 100).toInt()
    }

    private fun guardarValores(x: Float, y: Float, z: Float) {
        val prefs = getSharedPreferences("DatosCampoMagnetico", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat("ultimoX", x)
            putFloat("ultimoY", y)
            putFloat("ultimoZ", z)
            apply()
        }
    }

    companion object {
        const val CAMPO_MAGNETICO_MAX = 100.0f
    }

    override fun onPause() {
        super.onPause()
        manejadorSensor.unregisterListener(escuchaSensor)
    }

    override fun onResume() {
        super.onResume()
        sensorMagnetico?.also {
            manejadorSensor.registerListener(escuchaSensor, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}