package com.vincnx.androidsistemakademik.presentation.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.vincnx.androidsistemakademik.MyApplication
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.data.repository.CourseRepo
import com.vincnx.androidsistemakademik.data.repository.EnrollmentRepo
import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient
import com.vincnx.androidsistemakademik.data.source.local.SessionManager
import com.vincnx.androidsistemakademik.domain.entities.User
import com.vincnx.androidsistemakademik.presentation.student.course.CourseAdapter

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val btnLogout by lazy { view?.findViewById<Button>(R.id.btn_logout)}
    private val tvUserEmail by lazy { view?.findViewById<TextView>(R.id.tv_user_email)}
    private val tvUserRole by lazy { view?.findViewById<TextView>(R.id.tv_user_role)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (requireActivity().application as MyApplication).appContainer
        sessionManager = appContainer.sessionManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        view?.let {
            tvUserEmail?.text = sessionManager.getUserDetails()[SessionManager.KEY_EMAIL]
            tvUserRole?.text = sessionManager.getUserDetails()[SessionManager.KEY_ROLE]

            btnLogout?.setOnClickListener {
                sessionManager.logoutUser()
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }
    }
}