package com.example.pxandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      val auth = FirebaseAuth.getInstance()
      val fragment = if (auth.currentUser != null) {
        HomeFragment()
      } else {
        LoginFragment()
      }

      supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit()
    }
  }
}
