package com.example.miadministracion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val btnRegistroVisita = findViewById<Button>(R.id.btnRegistroVisita)
        val btnListaVisita = findViewById<Button>(R.id.btnListaVisita)

        // Configurar el botón para ir a RegistroVisita
        btnRegistroVisita.setOnClickListener {
            val intent = Intent(this, RegistroVisita::class.java)
            startActivity(intent)
        }

        // Configurar el botón para ir a ListaVisita
        btnListaVisita.setOnClickListener {
            val intent = Intent(this, ListaVisita::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}