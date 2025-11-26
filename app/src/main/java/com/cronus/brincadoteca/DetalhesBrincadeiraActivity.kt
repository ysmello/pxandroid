package com.cronus.brincadoteca.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.Brincadeira
import com.cronus.brincadoteca.databinding.ActivityDetalhesBrincadeiraBinding // Assuma este nome de binding

class DetalhesBrincadeiraActivity : AppCompatActivity() {

    // 1. Definição do Binding e da chave
    private lateinit var binding: ActivityDetalhesBrincadeiraBinding

    companion object {
        // Chave que a GerenciarBrincadeirasActivity usará
        const val EXTRA_BRINCADEIRA = "BRINCADEIRA_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Configuração do Binding
        binding = ActivityDetalhesBrincadeiraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar a Toolbar (opcional, mas recomendado)
        setSupportActionBar(binding.toolbarDetalhes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes da Brincadeira"

        // 3. Receber o objeto Brincadeira
        val brincadeira: Brincadeira? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_BRINCADEIRA, Brincadeira::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_BRINCADEIRA) as Brincadeira?
        }

        // 4. Exibir os dados ou mostrar erro
        if (brincadeira != null) {
            exibirDetalhes(brincadeira)
        } else {
            Log.e("Detalhes", "Erro: Brincadeira não encontrada no Intent.")
            Toast.makeText(this, "Erro: Brincadeira não encontrada.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // 5. Método para preencher a tela
    private fun exibirDetalhes(brincadeira: Brincadeira) {

        binding.textNomeBrincadeira.text = brincadeira.nome
        binding.textDescricao.text = brincadeira.descricao
        binding.textRegras.text = brincadeira.regras
        binding.textMaterial.text = brincadeira.material_necessario

        // Metadados
        val minJog = brincadeira.jogadores?.get("min") ?: 0
        val maxJog = brincadeira.jogadores?.get("max") ?: 0
        val minIdade = brincadeira.faixa_etaria?.get("min") ?: 0
        val maxIdade = brincadeira.faixa_etaria?.get("max") ?: 0

        binding.textDetalhesCategoriaAmbiente.text =
            "Categoria: ${brincadeira.categoria ?: "N/A"} | Ambiente: ${brincadeira.ambiente ?: "N/A"}"

        binding.textDetalhesJogadoresIdade.text =
            "Jogadores: ${minJog}-${maxJog} | Idade: ${minIdade} a ${maxIdade} anos"

        binding.textDetalhesDuracaoStatus.text =
            "Duração: ${brincadeira.duracao_minutos ?: "N/A"} minutos | Status: ${brincadeira.status ?: "Desconhecido"}"
    }

    // 6. Configuração do botão de voltar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}