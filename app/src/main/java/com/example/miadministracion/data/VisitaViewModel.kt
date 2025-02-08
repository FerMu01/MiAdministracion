package com.example.miadministracion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miadministracion.data.Visita
import com.example.miadministracion.data.VisitaRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
class VisitaViewModel(private val repository: VisitaRepository) : ViewModel() {
    val visitas = repository.todasLasVisitas

    fun insertar(visita: Visita) = viewModelScope.launch {
        repository.insertar(visita)
    }

    fun actualizar(visita: Visita) = viewModelScope.launch {
        repository.actualizar(visita)
    }
}
class VisitaViewModelFactory(private val repository: VisitaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}