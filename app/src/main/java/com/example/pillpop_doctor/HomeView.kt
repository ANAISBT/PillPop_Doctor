package com.example.pillpop_doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.pillpop_doctor.databinding.ActivityHomeViewBinding

class HomeView : AppCompatActivity() {

    private lateinit var binding: ActivityHomeViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HoyFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.Hoy -> replaceFragment(HoyFragment())
                R.id.progreso -> replaceFragment(ProgresoFragment())
                R.id.perfil -> replaceFragment(PerfilFragment())

                else -> {

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            commit()
        }
    }
}