package com.cronus.brincadoteca

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // 👈 IMPORTADO

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore // 👈 Declarado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // 👈 Inicializado

        binding.btnCreateAccount.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // 1. Criar mapa de dados do usuário
                        val userMap = hashMapOf(
                            "email" to email,
                            "role" to "user" // 👈 Define o papel padrão
                        )

                        // 2. Salvar o documento no Firestore com o UID
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Conta e dados criados com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Falha ao criar conta: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}