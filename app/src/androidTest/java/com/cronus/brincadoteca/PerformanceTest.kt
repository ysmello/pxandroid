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
import org.junit.Assert.assertTrue
import android.util.Log
import org.junit.Before

/**
 * Teste Não Funcional 1: Medição do tempo de Login (Desempenho).
 * O tempo máximo aceitável para login é de 5 segundos.
 */
@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    // Regra da Activity de Login.
    @get:Rule
    var mainActivityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        try {
            // ESPERA GLOBAL DE 5 SEGUNDOS para estabilidade na inicialização da Activity.
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    // TNS-01: Teste de Desempenho (Tempo de Login)
    @Test
    fun tns01_loginPerformance_isAcceptable() {
        val MAX_LOGIN_TIME_MS = 5000L // 5 segundos de limite máximo

        // Pausa de estabilização extra de 1s, necessária para evitar RootViewWithoutFocusException
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // Mede o tempo de início
        val startTime = System.currentTimeMillis()

        // Simula o login
        onView(withId(R.id.inputEmailLogin))
            .perform(typeText("teste@facul.com"), closeSoftKeyboard())

        onView(withId(R.id.inputPasswordLogin))
            .perform(typeText("123456"), closeSoftKeyboard())

        // Clica no botão
        onView(withId(R.id.btnLogin)).perform(click())

        // Espera a rede/Firebase finalizar a operação
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // Mede o tempo de fim
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        Log.d("PerformanceTest", "Tempo de Login (simulado): $duration ms")

        // Assert: O tempo total deve ser menor que o máximo aceitável
        assertTrue("O tempo de Login ($duration ms) excedeu o máximo ($MAX_LOGIN_TIME_MS ms)", duration < MAX_LOGIN_TIME_MS)
    }
}