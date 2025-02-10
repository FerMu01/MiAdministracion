package com.example.miadministracion

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.miadministracion.data.AppDatabase
import com.example.miadministracion.data.Visita
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class UpdateList : AppCompatActivity() {

    private lateinit var etRut: TextInputEditText
    private lateinit var etNombreApellido: TextInputEditText
    private lateinit var btnFechaHoraIngreso: Button
    private lateinit var btnFechaHoraSalida: Button
    private lateinit var btnGuardar: Button
    private var fechaHoraIngreso: LocalDateTime? = null
    private var fechaHoraSalida: LocalDateTime? = null
    private var visitaId: Int = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_list)

        etRut = findViewById(R.id.etRut)
        etNombreApellido = findViewById(R.id.etNombreApellido)
        btnFechaHoraIngreso = findViewById(R.id.btnFechaHoraIngreso)
        btnFechaHoraSalida = findViewById(R.id.btnFechaHoraSalida)
        btnGuardar = findViewById(R.id.btnGuardar)

        visitaId = intent.getIntExtra("VISITA_ID", -1)
        if (visitaId != -1) {
            cargarVisita(visitaId)
        }

        btnFechaHoraIngreso.setOnClickListener {
            seleccionarFechaHora { fechaHoraIngreso = it; btnFechaHoraIngreso.text = formatoFechaHora(it) }
        }

        btnFechaHoraSalida.setOnClickListener {
            seleccionarFechaHora { fechaHoraSalida = it; btnFechaHoraSalida.text = formatoFechaHora(it) }
        }

        btnGuardar.setOnClickListener {
            actualizarVisita()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun seleccionarFechaHora(onDateTimeSelected: (LocalDateTime) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { date ->
            val localDate = Instant.ofEpochMilli(date)
                .atOffset(ZoneOffset.UTC)
                .toLocalDate()

            TimePickerDialog(this, { _, hour, minute ->
                val dateTime = LocalDateTime.of(localDate, LocalTime.of(hour, minute))
                onDateTimeSelected(dateTime)
                Toast.makeText(this, "Fecha y hora seleccionadas: ${formatoFechaHora(dateTime)}", Toast.LENGTH_SHORT).show()
            }, 12, 0, true).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarVisita(visitaId: Int) {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val visita = db.visitaDao().getVisitaById(visitaId)
            withContext(Dispatchers.Main) {
                visita?.let { actualizarUI(it) }
                    ?: Toast.makeText(this@UpdateList, "Visita no encontrada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarUI(visita: Visita) {
        etRut.setText(visita.rut ?: "")
        etNombreApellido.setText(visita.nombre ?: "")

        visita.fechaHoraIngreso?.let {
            val fechaHora = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            btnFechaHoraIngreso.text = formatoFechaHora(fechaHora)
            fechaHoraIngreso = fechaHora
        }

        visita.fechaHoraSalida?.let {
            val fechaHora = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            btnFechaHoraSalida.text = formatoFechaHora(fechaHora)
            fechaHoraSalida = fechaHora
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarVisita() {
        val rut = etRut.text.toString().trim()
        val nombre = etNombreApellido.text.toString().trim()

        if (rut.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val visitaExistente = db.visitaDao().getVisitaById(visitaId)
            visitaExistente?.let {
                val visitaActualizada = it.copy(
                    rut = rut,
                    nombre = nombre,
                    fechaHoraIngreso = fechaHoraIngreso?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: it.fechaHoraIngreso,
                    fechaHoraSalida = fechaHoraSalida?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: it.fechaHoraSalida
                )

                db.visitaDao().updateVisita(visitaActualizada)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateList, "Visita actualizada correctamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Indica que la actividad terminó con éxito y hay cambios
                    finish()
                }

            } ?: withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateList, "No se encontró la visita para actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatoFechaHora(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateTime.format(formatter)
    }
}
