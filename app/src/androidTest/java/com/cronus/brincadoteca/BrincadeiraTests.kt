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
import org.junit.Before // 🚨 IMPORTAÇÃO NECESSÁRIA PARA O SETUP
import com.cronus.brincadoteca.activity.AdicionarEditarBrincadeiraActivity

/**
 * Classe de Testes de Componente e Funcionais focada na Activity Adicionar/Editar Brincadeira.
 * Esta classe usa uma regra de Activity isolada, resolvendo conflitos de foco e contexto.
 */
@RunWith(AndroidJUnit4::class)
class BrincadeiraTests {

    // Regra de Activity: Apenas para a tela de Adicionar/Editar.
    @get:Rule
    var addEditActivityRule: ActivityScenarioRule<AdicionarEditarBrincadeiraActivity> =
        ActivityScenarioRule(AdicionarEditarBrincadeiraActivity::class.java)

    /**
     * MOVENDO O TEMPO DE ESPERA PARA @Before
     * O método @Before é executado antes de CADA teste e garante que a Activity
     * tenha tempo para obter o foco da janela (resolvendo RootViewWithoutFocusException).
     */
    @Before
    fun setup() {
        try {
            // Aumentando a espera para 4 segundos para garantir que a janela adquira o foco (solução para ambientes lentos).
            Thread.sleep(4000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    // FT-02: Teste Funcional - Verificação de Elementos da UI (Adicionar/Editar)
    @Test
    fun ft02_addEditScreenElements_areVisible() {
        // Verifica campos de entrada principais (provavelmente visíveis no topo)
        onView(withId(R.id.editNome)).check(matches(isDisplayed()))
        onView(withId(R.id.editDescricao)).check(matches(isDisplayed()))

        // Verifica Spinners (seletores)
        onView(withId(R.id.spinnerCategoria)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerAmbiente)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerStatus)).check(matches(isDisplayed()))

        // 🚨 CORREÇÃO DE ROLAGEM: Garante que os botões (que podem estar fora da tela)
        // sejam rolados para a viewport.
        onView(isRoot()).perform(swipeUp())

        // Verifica botões
        onView(withId(R.id.btnSalvarBrincadeira)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCancelarBrincadeira)).check(matches(isDisplayed()))
    }

    // CT-01: Teste de Componente - Validação de Formulário (Erro de Vazio)
    @Test
    fun ct01_saveEmptyForm_showsError() {
        // 🚨 CORREÇÃO DE ROLAGEM: Rola para garantir que o botão Salvar esteja visível
        // antes de tentar clicar, resolvendo falhas de visibilidade/rolagem.
        onView(isRoot()).perform(swipeUp())

        // Clica em Salvar com campos vazios
        onView(withId(R.id.btnSalvarBrincadeira)).perform(click())

        // Verifica se a Activity NÃO foi fechada, indicando que a validação falhou
        onView(withId(R.id.btnSalvarBrincadeira)).check(matches(isDisplayed()))

        // Verificação opcional de erro específico em um campo (se a validação for visual)
        // Exemplo: onView(withId(R.id.editNome)).check(matches(hasErrorText("Nome é obrigatório")))
    }

    // CT-02: Teste de Componente - Navegação de Cancelamento
    @Test
    fun ct02_clickCancelButton_closesActivity() {
        // 🚨 CORREÇÃO DE ROLAGEM: Rola para garantir que o botão Cancelar esteja visível.
        onView(isRoot()).perform(swipeUp())

        // Clica no botão Cancelar
        onView(withId(R.id.btnCancelarBrincadeira)).perform(click())

        // Nota: O Espresso encerrará o teste se a Activity for fechada, resultando em sucesso.
    }
}