package com.example.opsc7311_sem2_2024

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opsc7311_sem2_2024.databinding.LoginMainBinding

class LoginActivity : AppCompatActivity() {

    // <editor-fold desc="View Binding Initialization">
    private lateinit var binding: LoginMainBinding
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // <editor-fold desc="Initialize Binding">
        binding = LoginMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // </editor-fold>

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // <editor-fold desc="Click Listeners">

        // When the register TextView is clicked
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java) // Adjust activity if different
            startActivity(intent)
        }

        // </editor-fold>

    }
}