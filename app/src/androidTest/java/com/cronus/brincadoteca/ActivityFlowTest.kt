package com.cronus.brincadoteca

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Classe de Testes de Fluxo e Componente focada na Activity principal (Login).
 */
@RunWith(AndroidJUnit4::class)
class ActivityFlowTest {

    // Regra da Activity de Login (MainActivity).
    @get:Rule
    var mainActivityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    // FT-01: Teste Funcional de Login Básico (Simulação)
    @Test
    fun ft01_simulatedLoginFlow_isSuccessful() {

        // 1. Digita E-mail
        onView(withId(R.id.inputEmailLogin))
            .perform(typeText("teste@facul.com"))

        // 2. Digita Senha. REMOVIDO: .scrollTo()
        onView(withId(R.id.inputPasswordLogin))
            .perform(typeText("123456"), closeSoftKeyboard()) // <-- Ação scrollTo() removida!

        // 3. Clica no botão de login
        onView(withId(R.id.btnLogin))
            .perform(click())

        // 4. Verificação de Sucesso (adicione aqui a verificação de HomeActivity)
        // Exemplo: onView(withId(R.id.algumIdDaHome)).check(matches(isDisplayed()))
    }
}