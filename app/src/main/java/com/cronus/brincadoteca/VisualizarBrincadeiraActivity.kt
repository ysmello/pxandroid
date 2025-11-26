package com.cronus.brincadoteca

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityVisualizarBrincadeiraBinding // Assumindo que você criou este layout
import com.cronus.brincadoteca.Brincadeira // Se sua classe Brincadeira estiver em 'models'

class VisualizarBrincadeiraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisualizarBrincadeiraBinding

    // Use a chave que definiremos no Adapter
    companion object {
        const val EXTRA_BRINCADEIRA = "BRINCADEIRA_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflar e configurar o Binding
        binding = ActivityVisualizarBrincadeiraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Tentar receber o objeto Brincadeira
        val brincadeira: Brincadeira? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_BRINCADEIRA, Brincadeira::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_BRINCADEIRA) as Brincadeira?
        }

        // 3. Processar o objeto recebido
        if (brincadeira != null) {
            Log.d("Visualizar", "Brincadeira recebida: ${brincadeira.nome} | ID: ${brincadeira.id}")
            // Apenas para testar, mostre o nome da brincadeira
            binding.textNomeBrincadeira.text = brincadeira.nome
            Toast.makeText(this, "Brincadeira carregada: ${brincadeira.nome}", Toast.LENGTH_LONG).show()
        } else {
            // Este é o erro que você estava vendo, mas agora logamos onde ele ocorre.
            Log.e("Visualizar", "Erro: Brincadeira não encontrada no Intent.")
            Toast.makeText(this, "Erro: Brincadeira não encontrada.", Toast.LENGTH_LONG).show()
            finish() // Fecha a Activity se não houver dados
        }
    }
}