package com.cronus.brincadoteca

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura a Toolbar
        setSupportActionBar(binding.toolbarForgotPassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()

        // Lógica do botão de envio de link
        binding.btnSendRecoveryLink.setOnClickListener {
            val email = binding.inputRecoveryEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envia o email de redefinição de senha
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Link de redefinição enviado para $email. Verifique sua caixa de entrada!",
                            Toast.LENGTH_LONG
                        ).show()
                        // Volta para a tela de login após o envio
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Erro ao enviar link. Verifique o email: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    // Método para lidar com o clique no ícone "Voltar" na barra de ferramentas
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Volta para a Activity anterior (MainActivity)
        return true
    }
}