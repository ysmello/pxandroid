package com.cronus.brincadoteca

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue // Importação necessária

@Parcelize
data class Brincadeira(
    @DocumentId
    val id: String? = null,
    val nome: String? = null,
    val descricao: String? = null,
    val regras: String? = null,
    val material_necessario: String? = null,
    val jogadores: Map<String, Int>? = null,
    val faixa_etaria: Map<String, Int>? = null,
    val categoria: @RawValue Any? = null, // CORREÇÃO: Usando @RawValue
    val ambiente: String? = null,
    val duracao_minutos: Int? = null,
    val status: String? = null,
    val data_criacao: java.util.Date? = null
) : Parcelable