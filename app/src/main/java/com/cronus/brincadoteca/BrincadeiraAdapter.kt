package com.cronus.brincadoteca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cronus.brincadoteca.databinding.ItemBrincadeiraBinding

class BrincadeiraAdapter(
    private var brincadeiras: List<Brincadeira>,
    private val isAdmin: Boolean = false,
    private val onActionClicked: (Brincadeira, ActionType) -> Unit
) : RecyclerView.Adapter<BrincadeiraAdapter.BrincadeiraViewHolder>() {

    enum class ActionType {
        VISUALIZAR, EDITAR, EXCLUIR
    }

    fun updateData(novaLista: List<Brincadeira>) {
        this.brincadeiras = novaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrincadeiraViewHolder {
        val binding = ItemBrincadeiraBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BrincadeiraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrincadeiraViewHolder, position: Int) {
        val brincadeira = brincadeiras[position]
        holder.bind(brincadeira)
    }

    override fun getItemCount(): Int = brincadeiras.size

    inner class BrincadeiraViewHolder(private val binding: ItemBrincadeiraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Lógica de clique (usa o callback onActionClicked)
            binding.btnVisualizar.setOnClickListener {
                val brincadeira = brincadeiras[adapterPosition]
                onActionClicked(brincadeira, ActionType.VISUALIZAR)
            }

            binding.btnEditar.setOnClickListener {
                val brincadeira = brincadeiras[adapterPosition]
                onActionClicked(brincadeira, ActionType.EDITAR)
            }

            binding.btnExcluir.setOnClickListener {
                val brincadeira = brincadeiras[adapterPosition]
                onActionClicked(brincadeira, ActionType.EXCLUIR)
            }
        }

        fun bind(brincadeira: Brincadeira) {
            binding.textNomeBrincadeira.text = brincadeira.nome
            binding.textDetalhes.text = "Categoria: ${brincadeira.categoria ?: "N/A"} | Ambiente: ${brincadeira.ambiente ?: "N/A"}"

            // Lógica de exibição de metadados (Jogadores/Idade) e Status
            val minJog = brincadeira.jogadores?.get("min") ?: 0
            val maxJog = brincadeira.jogadores?.get("max") ?: 0
            val minIdade = brincadeira.faixa_etaria?.get("min") ?: 0
            val maxIdade = brincadeira.faixa_etaria?.get("max") ?: 0
            binding.textMetadata.text = "${minJog}-${maxJog} jogadores | ${minIdade} a ${maxIdade} anos"
            binding.textStatus.text = "Status: ${brincadeira.status ?: "Desconhecido"}"

            // Lógica de visibilidade dos botões de administração (CORRETA)
            if (isAdmin) {
                binding.layoutAdminActions.visibility = View.VISIBLE
            } else {
                binding.layoutAdminActions.visibility = View.GONE
            }
        }
    }
}