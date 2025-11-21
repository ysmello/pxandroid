package com.example.pxandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val auth = FirebaseAuth.getInstance()

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(text = "Login")

    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("E-mail") },
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Senha") },
      modifier = Modifier.fillMaxWidth()
    )

    Button(
      onClick = {
        auth.signInWithEmailAndPassword(email, password)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              errorMessage = null
              onLoginSuccess()
            } else {
              errorMessage = task.exception?.message
            }
          }
      },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Entrar")
    }

    Button(
      onClick = {
        auth.createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              errorMessage = null
              onLoginSuccess()
            } else {
              errorMessage = task.exception?.message
            }
          }
      },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Criar conta")
    }

    if (errorMessage != null) {
      Text(text = "Erro: $errorMessage")
    }
  }
}
