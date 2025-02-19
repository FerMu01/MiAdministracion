package com.example.miadministracion.data

import androidx.room.*

@Dao
interface VisitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVisita(visita: Visita)

    @Update
    suspend fun updateVisita(visita: Visita) // Se mantiene solo un método de actualización

    @Query("SELECT * FROM visitas ORDER BY fechaHoraIngreso DESC")
    fun obtenerVisitas(): kotlinx.coroutines.flow.Flow<List<Visita>>

    @Query("SELECT * FROM visitas WHERE id = :id LIMIT 1")
    suspend fun obtenerVisitaPorId(id: Int): Visita?

    @Query("DELETE FROM visitas")
    suspend fun eliminarTodasLasVisitas()

    @Query("SELECT * FROM visitas")
    suspend fun getAllVisitas(): List<Visita>

    @Query("SELECT * FROM visitas WHERE id = :id")
    suspend fun getVisitaById(id: Int): Visita?
}
