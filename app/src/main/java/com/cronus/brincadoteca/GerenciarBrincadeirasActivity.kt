package com.cronus.brincadoteca

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.cronus.brincadoteca.R
import com.cronus.brincadoteca.databinding.ActivityGerenciarBrincadeirasBinding
import com.cronus.brincadoteca.activity.AdicionarEditarBrincadeiraActivity
import com.cronus.brincadoteca.activity.DetalhesBrincadeiraActivity
import com.google.firebase.firestore.Query
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.cronus.brincadoteca.activity.TempoActivity // Importação da TempoActivity

class GerenciarBrincadeirasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGerenciarBrincadeirasBinding
    private lateinit var brincadeiraAdapter: BrincadeiraAdapter
    private val db = FirebaseFirestore.getInstance()
    private val brincadeirasList = mutableListOf<Brincadeira>()
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGerenciarBrincadeirasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. CONECTAR A TOOLBAR
        setSupportActionBar(binding.toolbarGerenciarBrincadeiras)

        // 2. HABILITAR A SETA
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

        // ***************************************************************
        // >>> SOLUÇÃO DEFINITIVA: RECUPERAR STATUS DO SharedPreferences <<<
        // ***************************************************************

        // Acessa as SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Recupera o status de Admin SALVO. Se não houver, o padrão será 'false'.
        isAdmin = prefs.getBoolean("IS_ADMIN", false)

        Log.d("ADMIN_VAL_FINAL", "Valor FINAL de isAdmin (Recuperado das Preferências): $isAdmin")

        // ***************************************************************
        // FIM DA SOLUÇÃO DEFINITIVA (FORÇAMENTO REMOVIDO)
        // ***************************************************************

        // RESTAURAÇÃO 2: Configurar o RecyclerView antes de carregar os dados
        setupRecyclerView()
        loadBrincadeiras()

        // RESTAURAÇÃO 3: Configuração do botão de Adicionar (FAB)
        if (isAdmin) { // Agora usa o valor persistido
            binding.fabAddBrincadeira.visibility = View.VISIBLE
            binding.fabAddBrincadeira.setOnClickListener {
                val intent = Intent(this, AdicionarEditarBrincadeiraActivity::class.java)
                intent.putExtra("MODE", "ADD")
                startActivity(intent)
            }
        } else {
            binding.fabAddBrincadeira.visibility = View.GONE
        }

        // ☀️ NOVO: Floating Action Button (FAB) de Clima
        binding.fabClima.setOnClickListener {
            val intent = Intent(this, TempoActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Abrindo informações de Tempo...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("FROM_BACK_BUTTON", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadBrincadeiras()
    }

    private fun setupRecyclerView() {
        // A passagem do 'isAdmin' aqui está correta!
        brincadeiraAdapter = BrincadeiraAdapter(
            brincadeiras = brincadeirasList,
            isAdmin = isAdmin,
            onActionClicked = { brincadeira, actionType ->
                handleBrincadeiraAction(brincadeira, actionType)
            }
        )
        binding.recyclerViewBrincadeiras.apply {
            layoutManager = LinearLayoutManager(this@GerenciarBrincadeirasActivity)
            adapter = brincadeiraAdapter
        }
    }

    private fun loadBrincadeiras() {
        binding.progressBar.visibility = View.VISIBLE

        var query = db.collection("brincadeiras").orderBy("data_criacao", Query.Direction.DESCENDING)

        if (!isAdmin) {
            query = query.whereEqualTo("status", "ativo")
            binding.textAdminTitle.text = "Catálogo de Brincadeiras Ativas"
        } else {
            binding.textAdminTitle.text = "Gerenciar Brincadeiras (ADMIN)"
        }

        query.get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                brincadeirasList.clear()

                Log.d("FirestoreLoad", "Documentos recebidos (Admin=${isAdmin}): ${result.size()}")

                for (document in result) {
                    val brincadeira = document.toObject(Brincadeira::class.java)
                    brincadeirasList.add(brincadeira)
                }
                brincadeiraAdapter.updateData(brincadeirasList)

                if (brincadeirasList.isEmpty()) {
                    binding.textEmptyMessage.visibility = View.VISIBLE
                    if (!isAdmin) {
                        binding.textEmptyMessage.text = "Nenhuma brincadeira ativa encontrada."
                    } else {
                        binding.textEmptyMessage.text = "Nenhuma brincadeira cadastrada. Adicione uma nova!"
                    }
                } else {
                    binding.textEmptyMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.w("Firestore", "Erro ao carregar brincadeiras: ", exception)
                Toast.makeText(this, "Falha ao carregar dados.", Toast.LENGTH_SHORT).show()
            }
    }

    // AQUI ESTÁ A CORREÇÃO CRÍTICA PARA O FLUXO DE VISUALIZAÇÃO!
    private fun handleBrincadeiraAction(brincadeira: Brincadeira, actionType: BrincadeiraAdapter.ActionType) {
        when (actionType) {
            BrincadeiraAdapter.ActionType.VISUALIZAR -> {
                // Passa o objeto Brincadeira inteiro e usa a chave da DetalhesBrincadeiraActivity
                val intent = Intent(this, DetalhesBrincadeiraActivity::class.java).apply {
                    putExtra(DetalhesBrincadeiraActivity.EXTRA_BRINCADEIRA, brincadeira)
                }
                startActivity(intent)
            }
            BrincadeiraAdapter.ActionType.EDITAR -> {
                val intent = Intent(this, AdicionarEditarBrincadeiraActivity::class.java)
                intent.putExtra("MODE", "EDIT")
                intent.putExtra("brincadeira", brincadeira)
                startActivity(intent)
            }
            BrincadeiraAdapter.ActionType.EXCLUIR -> {
                confirmDeleteBrincadeira(brincadeira)
            }
        }
    }

    private fun confirmDeleteBrincadeira(brincadeira: Brincadeira) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir a brincadeira '${brincadeira.nome}'?")
            .setPositiveButton("Excluir") { dialog, which ->
                deleteBrincadeira(brincadeira)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBrincadeira(brincadeira: Brincadeira) {
        brincadeira.id?.let { id ->
            db.collection("brincadeiras").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Brincadeira excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    loadBrincadeiras()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao excluir brincadeira: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("Firestore", "Erro ao excluir: ", e)
                }
        }
    }
}