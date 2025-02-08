package com.example.miadministracion

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import java.text.SimpleDateFormat
import java.util.*
class RegistroVisita : AppCompatActivity() {

    private var selectedDateInMillis: Long = 0
    private lateinit var visitaViewModel: VisitaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_visita)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val database = AppDatabase.getDatabase(this)
        val visitaDao = database.visitaDao()
        val repository = VisitaRepository(visitaDao)
        val viewModelFactory = VisitaViewModelFactory(repository)
        visitaViewModel = ViewModelProvider(this, viewModelFactory).get(VisitaViewModel::class.java)

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnSalir = findViewById<Button>(R.id.btnSalir)
        val btnFechaHoraIngreso = findViewById<Button>(R.id.button_date_picker)
        val btnFechaHoraSalida = findViewById<Button>(R.id.btnFechaHoraSalida)
        val tvFechaHoraIngreso = findViewById<TextView>(R.id.tvFechaHoraIngreso)
        val tvFechaHoraSalida = findViewById<TextView>(R.id.tvFechaHoraSalida)

        btnGuardar.setOnClickListener {
            val rut = findViewById<TextView>(R.id.etRut).text.toString()
            val nombreApellido = findViewById<TextView>(R.id.etNombreApellido).text.toString()
            val destino = findViewById<TextView>(R.id.etDepartamentoCasa).text.toString()
            val fechaHoraIngreso = tvFechaHoraIngreso.text.toString()
            val fechaHoraSalida = tvFechaHoraSalida.text.toString()

            if (rut.isEmpty() || nombreApellido.isEmpty() || destino.isEmpty() || fechaHoraIngreso.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaHoraIngresoMillis = convertToMillis(fechaHoraIngreso)
            val fechaHoraSalidaMillis = if (fechaHoraSalida.isNotEmpty()) convertToMillis(fechaHoraSalida) else null

            val visita = Visita(
                rut = rut,
                nombre = nombreApellido,  // Usando el campo combinado
                fechaHoraIngreso = fechaHoraIngresoMillis,
                fechaHoraSalida = fechaHoraSalidaMillis,
                destino = destino
            )

            visitaViewModel.insertar(visita)
            Toast.makeText(this, "Visita registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSalir.setOnClickListener {
            finish()
        }

        btnFechaHoraIngreso.setOnClickListener {
            showDateTimePicker(tvFechaHoraIngreso)
        }

        btnFechaHoraSalida.setOnClickListener {
            showDateTimePicker(tvFechaHoraSalida)
        }
    }

    private fun showDateTimePicker(tv: TextView) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona la fecha")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDateInMillis = selection
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = Date(selectedDateInMillis)
            tv.text = dateFormat.format(date)
            showTimePicker(tv)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker(tv: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateInMillis)) + " $selectedTime"
                tv.text = fullDate
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun convertToMillis(dateTime: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.parse(dateTime)?.time ?: 0
    }
}
