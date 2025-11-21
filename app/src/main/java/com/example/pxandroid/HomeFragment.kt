package com.example.pxandroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

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
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val textWelcome = view.findViewById<TextView>(R.id.textWelcome)
    val buttonLogout = view.findViewById<Button>(R.id.buttonLogout)

    val email = auth.currentUser?.email ?: ""
    textWelcome.text = "Bem-vindo: $email"

    buttonLogout.setOnClickListener {
      auth.signOut()
      parentFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, LoginFragment())
        .commit()
    }
  }
}
