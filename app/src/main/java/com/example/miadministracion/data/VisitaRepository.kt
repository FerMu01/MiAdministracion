package com.example.miadministracion.data

import kotlinx.coroutines.flow.Flow

class VisitaRepository(private val visitaDao: VisitaDao) {
    val todasLasVisitas: Flow<List<Visita>> = visitaDao.obtenerVisitas()

    suspend fun insertar(visita: Visita) {
        visitaDao.insertarVisita(visita)
    }

    suspend fun actualizar(visita: Visita) {
        visitaDao.actualizarVisita(visita)
    }
}