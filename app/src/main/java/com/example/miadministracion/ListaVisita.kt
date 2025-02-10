package com.example.miadministracion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miadministracion.data.AppDatabase
import com.example.miadministracion.data.Visita
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ListaVisita : AppCompatActivity() {

    private lateinit var visitaAdapter: VisitaAdapter
    private lateinit var rvVisitas: RecyclerView

    // Manejo de actividad para actualización de visitas
    private val updateVisitaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            obtenerVisitas() // Recargar la lista si se modificó una visita
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_visita)

        // Configurar RecyclerView
        rvVisitas = findViewById(R.id.rvVisitas)
        rvVisitas.layoutManager = LinearLayoutManager(this)
        visitaAdapter = VisitaAdapter(emptyList()) { visitaId ->
            val intent = Intent(this, UpdateList::class.java)
            intent.putExtra("VISITA_ID", visitaId)
            updateVisitaLauncher.launch(intent) // Usamos el launcher en lugar de startActivity()
        }
        rvVisitas.adapter = visitaAdapter

        // Obtener datos de la base de datos
        obtenerVisitas()
    }

    private fun obtenerVisitas() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val visitas = db.visitaDao().getAllVisitas()
            withContext(Dispatchers.Main) {
                visitaAdapter.updateVisitas(visitas)
            }
        }
    }

    class VisitaAdapter(
        private var visitas: List<Visita>,
        private val onEditClickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<VisitaAdapter.VisitaViewHolder>() {

        class VisitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
            val tvRut: TextView = itemView.findViewById(R.id.tvRut)
            val tvFechaIngreso: TextView = itemView.findViewById(R.id.tvFechaIngreso)
            val tvFechaSalida: TextView = itemView.findViewById(R.id.tvFechaSalida)
            val tvDestino: TextView = itemView.findViewById(R.id.tvDestino)
            val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitaViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visita, parent, false)
            return VisitaViewHolder(view)
        }

        override fun onBindViewHolder(holder: VisitaViewHolder, position: Int) {
            val visita = visitas[position]
            holder.tvNombre.text = visita.nombre
            holder.tvRut.text = visita.rut
            holder.tvFechaIngreso.text = "Ingreso: ${formatFechaHora(visita.fechaHoraIngreso)}"
            holder.tvFechaSalida.text = "Salida: ${visita.fechaHoraSalida?.let { formatFechaHora(it) } ?: "No registrada"}"
            holder.tvDestino.text = "Departamento/Casa: ${visita.destino}"
            holder.btnEditar.setOnClickListener {
                onEditClickListener(visita.id)
            }
        }

        override fun getItemCount(): Int {
            return visitas.size
        }

        fun updateVisitas(nuevasVisitas: List<Visita>) {
            visitas = nuevasVisitas
            notifyDataSetChanged()
        }

        private fun formatFechaHora(timestamp: Long): String {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return format.format(Date(timestamp))
        }
    }
}
