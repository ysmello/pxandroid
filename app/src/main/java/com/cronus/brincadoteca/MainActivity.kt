package com.cronus.brincadoteca

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cronus.brincadoteca.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 🚨 CHAVE DE ACESSO: Verifica se o usuário já está logado
        if (auth.currentUser != null) {
            checkUserRoleAndNavigate()
            return
        }

        // 1. Inicializa o Activity Result Launcher (para Google)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleGoogleSignInResult(result.data)
        }

        // 2. Google Sign In Config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        // 3. Login Tradicional
        setupTraditionalLogin()

        // 4. Google Login
        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // 5. Navegação
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // ----------------------------------------------------------------------
    // NOVO: Função central para ler o papel do usuário e navegar
    // ----------------------------------------------------------------------
// MainActivity.kt - Dentro da função checkUserRoleAndNavigate()

    private fun checkUserRoleAndNavigate() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")

                // 💡 1. DEFINIR E SALVAR O STATUS DE ADMIN AQUI
                val isAdmin = role == "admin"
                val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                prefs.edit().putBoolean("IS_ADMIN", isAdmin).apply()
                Log.d("RoleCheck", "Status Admin SALVO nas SharedPreferences: $isAdmin")

                // 💡 2. REDIRECIONAMENTO COM BASE NO PAPEL
                when (role) {
                    "admin" -> {
                        Toast.makeText(this, "Login Admin SUCESSO. Bem-vindo!", Toast.LENGTH_SHORT).show()
                        // 🚨 CORREÇÃO: Redirecione o Admin DIRETAMENTE para GerenciarBrincadeiras
                        startActivity(Intent(this, GerenciarBrincadeirasActivity::class.java))
                    }
                    "user" -> {
                        Toast.makeText(this, "Login Usuário SUCESSO.", Toast.LENGTH_SHORT).show()
                        // Usuário normal vai para Home (que deve ser a tela de catálogo público)
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    else -> {
                        Log.w("RoleCheck", "Papel não definido. Redirecionando para Home.")
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao ler dados do usuário. Tente novamente.", Toast.LENGTH_LONG).show()
                Log.e("RoleCheck", "Erro ao buscar papel", e)
                auth.signOut()
            }
    }

    // ----------------------------------------------------------------------
    // Login Tradicional (Inalterado)
    // ----------------------------------------------------------------------
    private fun setupTraditionalLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmailLogin.text.toString().trim()
            val password = binding.inputPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha email e senha para entrar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkUserRoleAndNavigate()
                    } else {
                        Toast.makeText(this, "Falha no Login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // Método para lidar com o resultado do Google Sign In (inalterado)
    private fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleAuth", "Login Google OK. Iniciando Auth Firebase.")
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            Log.e("GoogleAuth", "Login Falhou. Código de Status: $statusCode", e)
            val errorMessage = when (statusCode) {
                12501 -> "Login Cancelado pelo usuário."
                10 -> "Erro de Desenvolvedor (Verifique SHA-1 ou Client ID). Código: 10"
                else -> "Erro de Autenticação. Código: $statusCode"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // ----------------------------------------------------------------------
    // NOVO: Função para garantir que novos usuários Google tenham um documento Firestore
    // ----------------------------------------------------------------------
    private fun handleGoogleUserFirestoreData(user: FirebaseUser) {
        // 1. Checa se o documento do usuário já existe no Firestore
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 2. Se o documento existe (usuário antigo ou já salvo), apenas navega
                    checkUserRoleAndNavigate()
                } else {
                    // 3. Se NÃO existe (novo usuário Google), cria o documento com role "user"
                    saveNewGoogleUserToFirestore(user)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao verificar dados do usuário Google. Deslogando.", Toast.LENGTH_LONG).show()
                Log.e("GoogleAuth", "Erro ao buscar papel", e)
                auth.signOut()
            }
    }

    // ----------------------------------------------------------------------
    // NOVO: Função para salvar o novo usuário Google no Firestore
    // ----------------------------------------------------------------------
    private fun saveNewGoogleUserToFirestore(user: FirebaseUser) {
        val userMap = hashMapOf(
            "email" to user.email,
            "role" to "user" // Define o papel padrão
        )

        firestore.collection("users").document(user.uid).set(userMap)
            .addOnSuccessListener {
                Log.d("GoogleAuth", "Novo usuário Google salvo no Firestore com role: user")
                checkUserRoleAndNavigate() // Depois de salvar, navega
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar dados do novo usuário Google: ${e.message}", Toast.LENGTH_LONG).show()
                auth.signOut()
            }
    }

    // ----------------------------------------------------------------------
    // Método para autenticar no Firebase com as credenciais do Google (Revisado)
    // ----------------------------------------------------------------------
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // 💡 AGORA: Chama a nova função que verifica/cria o documento no Firestore
                        handleGoogleUserFirestoreData(user)
                    } else {
                        Toast.makeText(this, "Erro interno: Usuário Google nulo.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Falha na autenticação Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.e("GoogleAuth", "Falha Auth Firebase", task.exception)
                }
            }
    }
}