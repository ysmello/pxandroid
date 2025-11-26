package com.cronus.brincadoteca

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.view.MenuItem
// A importação de GerenciarBrincadeirasActivity não é estritamente necessária aqui, mas não atrapalha.
// import com.cronus.brincadoteca.GerenciarBrincadeirasActivity


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("ActivityFlow", ">>> HomeActivity INICIADA")

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // *******************************************************************
        // >>> MODIFICAÇÃO CHAVE PARA QUEBRAR O LOOP <<<
        // *******************************************************************

        // 1. Verifica se a chamada para HomeActivity veio do botão "Voltar" da outra tela.
        // O valor default é false, então só será true se explicitamente definido.
        val isComingFromBackButton = intent.getBooleanExtra("FROM_BACK_BUTTON", false)

        // Se o usuário está logado E NÃO veio do botão voltar (seta), redireciona.
        if (auth.currentUser != null && !isComingFromBackButton) {

            Log.d("ActivityFlow", "Usuário logado e primeira entrada. Redirecionando para GerenciarBrincadeirasActivity.")
            navigateToGerenciarBrincadeiras(auth.currentUser!!)

            // O 'finish()' dentro de navigateToGerenciarBrincadeiras já encerra a execução,
            // mas adicionamos 'return' por segurança de fluxo.
            return

        } else {
            // Se veio do botão voltar (seta), ou se não há usuário logado,
            // o fluxo continua na HomeActivity para exibir seu conteúdo (ou tela de login).
            Log.d("ActivityFlow", "Usuário logado, mas veio do Voltar, ou não está logado. Permanecendo na HomeActivity.")
            updateUI(auth.currentUser)
        }

        // *******************************************************************
        // FIM DA MODIFICAÇÃO
        // *******************************************************************


        // O botão Logout (mantenha a checagem do ID no seu layout)
        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    // O método onOptionsItemSelected foi removido desta Activity,
    // pois ele é mais relevante para GerenciarBrincadeirasActivity,
    // que é onde a seta de "Voltar" (up navigation) está localizada.
    // Se esta Activity não tiver a seta, este método é desnecessário ou deve ser revisado.
    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // ... lógica anterior para HomeActivity. Se HomeActivity for a raiz,
            // este código pode não ser o ideal, pois ele reinicia a si mesma.
            // Para simplificar, vou removê-lo.
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    */

    // Função para atualizar a UI com base no usuário logado
    // OBS: O redirecionamento primário foi movido para o onCreate.
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // A HomeActivity agora deve exibir o conteúdo de "Home" para o usuário logado
            // quando ela for acessada pelo botão Voltar (se este método for chamado).
            // Seu código aqui deve ser apenas para carregar o conteúdo da Home,
            // e não para redirecionar novamente.
            Log.d("HomeActivity", "Usuário ID: ${user.uid}, isAdmin: (Lógica na próxima tela)")

            // Aqui, você deve carregar o conteúdo da HomeActivity, se ela tiver algum.

        } else {
            // Se o usuário não está logado, redireciona para a tela inicial/login
            Toast.makeText(this, "Sessão expirada ou nenhum usuário logado.", Toast.LENGTH_LONG).show()
            redirectToMain()
        }
    }

    private fun navigateToGerenciarBrincadeiras(user: FirebaseUser) {
        // 1. Volte para a lógica real, removendo o forçamento:
        val ADMIN_UID = "DEjoBoEWhlVR1my0llL0VxucGeL2"
        val isUserAdmin: Boolean = user.uid == ADMIN_UID

        // 2. AÇÃO DEFINITIVA: SALVAR O STATUS DE ADMIN NAS SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("IS_ADMIN", isUserAdmin).apply() // Salva o valor real (true ou false)

        Log.d("HomeActivity", "Status Admin SALVO como: $isUserAdmin. Redirecionamento.")

        val intent = Intent(this, GerenciarBrincadeirasActivity::class.java)

        // 3. REMOVA o putExtra ("IS_ADMIN") se você o adicionou antes,
        // pois a GerenciarBrincadeirasActivity lerá das SharedPreferences.

        startActivity(intent)
        finish()
    }

    // Função para sair e redirecionar para a tela de login
    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logout realizado com sucesso.", Toast.LENGTH_SHORT).show()
        redirectToMain()
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // Limpa todas as activities anteriores (flag para evitar que o usuário volte)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}