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
import com.vincnx.androidsistemakademik.data.source.local.SessionManager

class LoginFragment : Fragment() {
    private lateinit var dbClient: DatabaseClient
    private lateinit var sessionManager: SessionManager
    private val inputEmail by lazy { view?.findViewById<EditText>(R.id.et_email) }
    private val inputPassword by lazy { view?.findViewById<EditText>(R.id.et_password) }
    private val btnLogin by lazy { view?.findViewById<View>(R.id.btn_login) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (requireActivity().application as MyApplication).appContainer
        dbClient = appContainer.database
        sessionManager = appContainer.sessionManager

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnRole(sessionManager.getUserDetails()[SessionManager.KEY_ROLE])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                        for (userSnapshot in snapshot.children) {
                            val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                            if (dbPassword == password) {
                                val role = userSnapshot.child("role").getValue(String::class.java)
                                // Get the user ID (which is the key/node name in Firebase)
                                val userId = userSnapshot.key
                                // Save session with user ID
                                sessionManager.createLoginSession(email, role ?: "", userId ?: "")
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                navigateBasedOnRole(role)
                                return
                            }
                        }
                        Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
                    } else {
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

    private fun navigateBasedOnRole(role: String?) {
        when (role) {
            "student" -> findNavController().navigate(R.id.action_loginFragment_to_courseFragment)
            "lecturer" -> findNavController().navigate(R.id.action_loginFragment_to_lecturerEnrollFragment)
            "coordinator" -> findNavController().navigate(R.id.action_loginFragment_to_coordinatorCourseFragment)
            // Add other role navigation cases here
            else -> Toast.makeText(context, "Unknown role: $role", Toast.LENGTH_SHORT).show()
        }
    }
}