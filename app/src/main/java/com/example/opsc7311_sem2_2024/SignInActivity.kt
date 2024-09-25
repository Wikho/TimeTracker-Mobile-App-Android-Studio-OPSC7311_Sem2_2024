package com.example.opsc7311_sem2_2024

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opsc7311_sem2_2024.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    // <editor-fold desc="View Binding">
    private lateinit var binding: ActivitySignInBinding
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // <editor-fold desc="Binding Setup">
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // </editor-fold>

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // <editor-fold desc="Event Listeners">
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        // </editor-fold>
    }
}