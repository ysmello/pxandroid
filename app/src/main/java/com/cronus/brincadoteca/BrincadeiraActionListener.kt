package com.cronus.brincadoteca

// Interface para comunicação entre o Adapter e a Activity
interface BrincadeiraActionListener {
    fun onEditClick(brincadeiraId: String)
    fun onDeleteClick(brincadeiraId: String)
}