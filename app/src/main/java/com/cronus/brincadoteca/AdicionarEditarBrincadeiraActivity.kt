package com.cronus.brincadoteca.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.Brincadeira
import com.cronus.brincadoteca.databinding.ActivityAdicionarEditarBrincadeiraBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import com.cronus.brincadoteca.R

class AdicionarEditarBrincadeiraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarEditarBrincadeiraBinding
    private val db = FirebaseFirestore.getInstance()
    private var brincadeiraExistente: Brincadeira? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarEditarBrincadeiraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe o objeto Brincadeira se estiver no modo de edição
        brincadeiraExistente = intent.getParcelableExtra("brincadeira") as? Brincadeira

        // Configuração inicial da barra de ação
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

        brincadeiraExistente?.let {
            carregarDadosParaEdicao(it)
        }

        binding.btnSalvarBrincadeira.setOnClickListener {
            salvarBrincadeira()
        }

        // 🆕 NOVO: Comportamento do Botão Cancelar
        binding.btnCancelarBrincadeira.setOnClickListener {
            // Fecha a Activity e volta para a tela anterior
            finish()
        }

        title = if (brincadeiraExistente != null) "Editar Brincadeira" else "Adicionar Brincadeira"
    }

    private fun carregarDadosParaEdicao(brincadeira: Brincadeira) {
        // CORREÇÃO: IDs de View Binding alterados para 'edit...'
        binding.editNome.setText(brincadeira.nome ?: "")
        binding.editDescricao.setText(brincadeira.descricao ?: "")
        binding.editRegras.setText(brincadeira.regras ?: "")
        binding.editMateriais.setText(brincadeira.material_necessario ?: "")

        val minJogadores = brincadeira.jogadores?.get("min")
        val maxJogadores = brincadeira.jogadores?.get("max")
        val minIdade = brincadeira.faixa_etaria?.get("min")
        val maxIdade = brincadeira.faixa_etaria?.get("max")

        // CORREÇÃO: IDs de View Binding alterados para 'edit...'
        binding.editMinJogadores.setText(minJogadores?.toString() ?: "")
        binding.editMaxJogadores.setText(maxJogadores?.toString() ?: "")
        binding.editMinIdade.setText(minIdade?.toString() ?: "")
        binding.editMaxIdade.setText(maxIdade?.toString() ?: "")

        // Lógica para carregar outros campos (SpinnerCategoria, SpinnerAmbiente, etc.)
    }

    private fun salvarBrincadeira() {
        // CORREÇÃO: IDs de View Binding alterados para 'edit...'
        val nome = binding.editNome.text.toString().trim()
        val descricao = binding.editDescricao.text.toString().trim()
        val regras = binding.editRegras.text.toString().trim()
        val material = binding.editMateriais.text.toString().trim()

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Nome e Descrição são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        // CORREÇÃO: IDs de View Binding alterados para 'edit...'
        val minJog = binding.editMinJogadores.text.toString().toIntOrNull() ?: 1
        val maxJog = binding.editMaxJogadores.text.toString().toIntOrNull() ?: 10
        val minId = binding.editMinIdade.text.toString().toIntOrNull() ?: 3
        val maxId = binding.editMaxIdade.text.toString().toIntOrNull() ?: 12

        val jogadoresMap = mapOf("min" to minJog, "max" to maxJog)
        val idadeMap = mapOf("min" to minId, "max" to maxId)

        // Use valores reais da UI ou padrões razoáveis
        val categoria = "Recreativa" // Ajuste conforme a lógica do seu Spinner
        val ambiente = "Interno"     // Ajuste conforme a lógica do seu Spinner
        val duracao = 30

        val novaBrincadeira = Brincadeira(
            id = brincadeiraExistente?.id,
            nome = nome,
            descricao = descricao,
            regras = regras,
            material_necessario = material,
            jogadores = jogadoresMap,
            faixa_etaria = idadeMap,
            categoria = categoria,
            ambiente = ambiente,
            duracao_minutos = duracao,
            status = "ativo",
            data_criacao = brincadeiraExistente?.data_criacao ?: Date()
        )

        val colecao = db.collection("brincadeiras")
        val documento = novaBrincadeira.id?.let { colecao.document(it) } ?: colecao.document()

        documento.set(novaBrincadeira)
            .addOnSuccessListener {
                Toast.makeText(this, "Brincadeira salva com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}