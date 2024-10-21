package com.example.pillpop_doctor

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AcercaPillPop : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acerca_pillpop)
        // Referencia al ImageButton
        val retrocederButton = findViewById<ImageButton>(R.id.AcercaPillpopRetoceder)

        // Establecer el OnClickListener
        retrocederButton.setOnClickListener {
            finish() // O puedes usar finish()
        }
    }
}