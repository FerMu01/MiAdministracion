package com.example.miadministracion

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.example.miadministracion.data.AppDatabase
import com.example.miadministracion.data.Visita
import com.example.miadministracion.data.VisitaRepository
import com.example.miadministracion.data.VisitaViewModel
import com.example.miadministracion.data.VisitaViewModelFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class RegistroVisita : AppCompatActivity() {

    // Variables para almacenar en milisegundos la fecha y hora seleccionadas
    private var fechaHoraIngresoMillis: Long = 0
    private var fechaHoraSalidaMillis: Long = 0
    private lateinit var visitaViewModel: VisitaViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_visita)

        // Configuración de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de la base de datos, repositorio y ViewModel
        val database = AppDatabase.getDatabase(this)
        val visitaDao = database.visitaDao()
        val repository = VisitaRepository(visitaDao)
        val viewModelFactory = VisitaViewModelFactory(repository)
        visitaViewModel = ViewModelProvider(this, viewModelFactory).get(VisitaViewModel::class.java)

        // Referencias a vistas
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnSalir = findViewById<Button>(R.id.btnSalir)
        val btnFechaHoraIngreso = findViewById<Button>(R.id.button_date_picker)
        val btnFechaHoraSalida = findViewById<Button>(R.id.btnFechaHoraSalida)
        val tvFechaHoraIngreso = findViewById<TextView>(R.id.tvFechaHoraIngreso)
        val tvFechaHoraSalida = findViewById<TextView>(R.id.tvFechaHoraSalida)

        // Guardar visita
        btnGuardar.setOnClickListener {
            val rut = findViewById<TextView>(R.id.etRut).text.toString()
            val nombreApellido = findViewById<TextView>(R.id.etNombreApellido).text.toString()
            val destino = findViewById<TextView>(R.id.etDepartamentoCasa).text.toString()

            if (rut.isEmpty() || nombreApellido.isEmpty() || destino.isEmpty() || tvFechaHoraIngreso.text.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val visita = Visita(
                rut = rut,
                nombre = nombreApellido,
                fechaHoraIngreso = fechaHoraIngresoMillis,
                fechaHoraSalida = if (tvFechaHoraSalida.text.isNotEmpty()) fechaHoraSalidaMillis else null,
                destino = destino
            )

            visitaViewModel.insertar(visita)
            Toast.makeText(this, "Visita registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSalir.setOnClickListener {
            finish()
        }

        // Seleccionar fecha y hora de ingreso
        btnFechaHoraIngreso.setOnClickListener {
            seleccionarFechaHora { dateTime ->
                fechaHoraIngresoMillis = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
                tvFechaHoraIngreso.text = formatoFechaHora(dateTime)
            }
        }

        // Seleccionar fecha y hora de salida
        btnFechaHoraSalida.setOnClickListener {
            seleccionarFechaHora { dateTime ->
                fechaHoraSalidaMillis = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
                tvFechaHoraSalida.text = formatoFechaHora(dateTime)
            }
        }
    }

    /**
     * Abre un MaterialDatePicker para seleccionar la fecha, luego un TimePickerDialog para la hora.
     * Combina ambos en un LocalDateTime y lo devuelve mediante el callback.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun seleccionarFechaHora(onDateTimeSelected: (LocalDateTime) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona la fecha")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // Se convierte el timestamp a LocalDate tratándolo en UTC
            val localDate = Instant.ofEpochMilli(selection)
                .atOffset(ZoneOffset.UTC)
                .toLocalDate()

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val localTime = LocalTime.of(selectedHour, selectedMinute)
                val localDateTime = LocalDateTime.of(localDate, localTime)
                onDateTimeSelected(localDateTime)
                Toast.makeText(this, "Fecha y hora seleccionadas: ${formatoFechaHora(localDateTime)}", Toast.LENGTH_SHORT).show()
            }, 12, 0, true).show()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    /**
     * Formatea un LocalDateTime al formato "dd/MM/yyyy HH:mm".
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatoFechaHora(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateTime.format(formatter)
    }
}
