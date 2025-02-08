package com.example.miadministracion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class Visita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rut: String,
    val nombre: String,
    val fechaHoraIngreso: Long,
    val fechaHoraSalida: Long?,
    val destino: String
)