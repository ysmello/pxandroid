// UtilValidator.kt (na pasta main)
package com.cronus.brincadoteca

class UtilValidator {

    fun isEmailValid(email: String): Boolean {
        // Implementação real da lógica de email
        return email.contains("@") && email.contains(".")
    }

    fun isFieldEmpty(text: String): Boolean {
        // Implementação real da lógica de campo vazio
        return text.trim().isEmpty()
    }
}