package com.cronus.brincadoteca

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cronus.brincadoteca.GerenciarBrincadeirasActivity
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.textAdminWelcome.text = "BEM-VINDO, ADMINISTRADOR!"
        binding.textAdminInfo.text = "UID: ${auth.currentUser?.uid}\nEmail: ${auth.currentUser?.email}"

        // Navegação para gerenciamento de brincadeiras (Mantido)
        binding.btnGerenciarCadastros.setOnClickListener {
            val intent = Intent(this, GerenciarBrincadeirasActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logout realizado.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}