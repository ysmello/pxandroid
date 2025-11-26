package com.cronus.brincadoteca

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.hamcrest.Matchers.not

/**
 * Teste Não Funcional 2: Verificação de Usabilidade/Acessibilidade.
 * Garante que elementos de entrada tenham Hints para melhor UX e Acessibilidade.
 */
@RunWith(AndroidJUnit4::class)
class UsabilityTest {

    // Regra da Activity de Login.
    @get:Rule
    var mainActivityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        // ❌ REMOVIDO: Thread.sleep(5000)
        // O ActivityScenarioRule garante que a Activity é inicializada.
        // A espera fixa prejudica a estabilidade e a velocidade do teste.
    }

    // TNS-02: Teste de Usabilidade/Acessibilidade (Dica/Hint do Campo de Email)
    @Test
    fun tns02_emailInput_hasContentDescriptionOrHint() {
        // 1. Verifica se o campo de email está visível (implicitamente espera o foco da Activity)
        onView(withId(R.id.inputEmailLogin))
            .check(matches(isDisplayed()))

        // 2. Verifica se o campo de email possui uma dica (hint) que NÃO está vazia
        onView(withId(R.id.inputEmailLogin))
            .check(matches(withHint(not(""))))
    }
}