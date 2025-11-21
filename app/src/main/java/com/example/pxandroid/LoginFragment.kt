package com.example.pxandroid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

  private lateinit var auth: FirebaseAuth
  private lateinit var googleSignInOptions: GoogleSignInOptions

  private val googleSignInLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val data = result.data
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      try {
        val account = task.getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
          .addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
              Toast.makeText(requireContext(), "Login com Google realizado", Toast.LENGTH_SHORT).show()
              goToHome()
            } else {
              val msg = signInTask.exception?.message ?: "Erro ao autenticar com Google"
              Log.e("LoginFragment", "Firebase signInWithCredential error: $msg", signInTask.exception)
              Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
          }
      } catch (e: ApiException) {
        Log.e("LoginFragment", "Google sign in failed: statusCode=${e.statusCode}", e)
        Toast.makeText(
          requireContext(),
          "Falha no login com Google. Código: ${e.statusCode}",
          Toast.LENGTH_LONG
        ).show()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    auth = FirebaseAuth.getInstance()
    googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_login, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val switchRemember = view.findViewById<SwitchCompat>(R.id.switchRemember)
    val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
    val recycler = view.findViewById<RecyclerView>(R.id.recyclerItems)

    recycler.layoutManager = LinearLayoutManager(requireContext())
    recycler.adapter = SimpleAdapter(
      listOf("Login apenas com Google", "Sua senha não é salva no app")
    )

    buttonLogin.setOnClickListener {
      val remember = switchRemember.isChecked
      val client = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
      googleSignInLauncher.launch(client.signInIntent)
      if (remember) {
        Toast.makeText(requireContext(), "Sua conta Google será lembrada pelo próprio Google", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun goToHome() {
    parentFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, HomeFragment())
      .commit()
  }
}
