package com.vincnx.androidsistemakademik.presentation.auth.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vincnx.androidsistemakademik.MyApplication
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient

class LoginFragment : Fragment() {
    private lateinit var dbClient: DatabaseClient
    private val inputEmail by lazy {view?.findViewById<EditText>(R.id.et_email)}
    private val inputPassword by lazy {view?.findViewById<EditText>(R.id.et_password)}
    private val btnLogin by lazy {view?.findViewById<View>(R.id.btn_login)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbClient = (requireActivity().application as MyApplication).appContainer.database
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogin?.setOnClickListener {
            handleLogin(inputEmail?.text.toString(), inputPassword?.text.toString())
        }
    }

    private fun handleLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin?.isEnabled = false

        dbClient.database.getReference("users")
            .orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User found with matching email
                        for (userSnapshot in snapshot.children) {
                            val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                            if (dbPassword == password) {
                                // Password matches
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                val role = userSnapshot.child("role").getValue()
                                if (role === "student") {
//                                    navigate to student home
                                }
//                                findNavController().navigate(R.id.action_loginFragment_to_courseFragment)
                                return
                            }
                        }
                        // Password doesn't match
                        Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
                    } else {
                        // No user found with this email
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    }
                    btnLogin?.isEnabled = true
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    btnLogin?.isEnabled = true
                }
            })
    }

}