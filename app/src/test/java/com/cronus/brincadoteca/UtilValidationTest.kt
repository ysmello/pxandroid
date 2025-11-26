package com.cronus.brincadoteca
import com.cronus.brincadoteca.UtilValidator
import org.junit.Test
import org.junit.Assert.*

class UtilValidationTest {

    private val validator = UtilValidator()

    // UT-01: Validação de Email Básico
    @Test
    fun testEmailValidation_isCorrect() {
        // Cenário 1: Email Válido
        assertTrue(validator.isEmailValid("teste@email.com"))

        // Cenário 2: Email Inválido (sem @)
        assertFalse(validator.isEmailValid("testeemail.com"))
    }

    // UT-02: Validação de Campo Vazio
    @Test
    fun testFieldValidation_isEmpty() {
        // Cenário 1: Campo Vazio ou com espaços
        assertTrue(validator.isFieldEmpty("  "))

        // Cenário 2: Campo Preenchido
        assertFalse(validator.isFieldEmpty("Nome"))
    }
}