package com.example.pxandroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    auth = FirebaseAuth.getInstance()
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

    val editEmail = view.findViewById<EditText>(R.id.editEmail)
    val editPassword = view.findViewById<EditText>(R.id.editPassword)
    val switchRemember = view.findViewById<SwitchCompat>(R.id.switchRemember)
    val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
    val recycler = view.findViewById<RecyclerView>(R.id.recyclerItems)

    recycler.layoutManager = LinearLayoutManager(requireContext())
    recycler.adapter = SimpleAdapter(
      listOf("Dica 1: use senha forte", "Dica 2: não compartilhe sua senha")
    )

    buttonLogin.setOnClickListener {
      val email = editEmail.text.toString().trim()
      val password = editPassword.text.toString().trim()
      val remember = switchRemember.isChecked

      if (email.isBlank() || password.isBlank()) {
        Toast.makeText(requireContext(), "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
      } else {
        auth.signInWithEmailAndPassword(email, password)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              Toast.makeText(
                requireContext(),
                "Login realizado. Lembrar: $remember",
                Toast.LENGTH_SHORT
              ).show()
              goToHome()
            } else {
              auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { createTask ->
                  if (createTask.isSuccessful) {
                    Toast.makeText(
                      requireContext(),
                      "Conta criada e login realizado",
                      Toast.LENGTH_SHORT
                    ).show()
                    goToHome()
                  } else {
                    Toast.makeText(
                      requireContext(),
                      createTask.exception?.message ?: "Erro ao autenticar",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
            }
          }
      }
    }
  }

  private fun goToHome() {
    parentFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, HomeFragment())
      .commit()
  }
}
