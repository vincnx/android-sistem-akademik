package com.vincnx.androidsistemakademik.presentation.lecturer.enrollment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.domain.entities.Course
import com.vincnx.androidsistemakademik.domain.entities.Enrollment
import com.vincnx.androidsistemakademik.presentation.student.course.CourseAdapter

class EnrollmentAdapter(
    private val onGradeEdit: (Enrollment) -> Unit
) : RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder>() {
    private var enrollments = listOf<Enrollment>()

    fun submitList(newList: List<Enrollment>) {
        enrollments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lecturer_enroll, parent, false)
        return EnrollmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        holder.bind(enrollments[position], onGradeEdit)
    }

    override fun getItemCount() = enrollments.size

    class EnrollmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        private val tvGrade: TextView = itemView.findViewById(R.id.tv_grade)
        private val btnEdit: Button = itemView.findViewById(R.id.btn_edit)

        fun bind(enrollment: Enrollment, onGradeEdit: (Enrollment) -> Unit) {
            tvStudentName.text = enrollment.student?.name ?: "Unknown Student"
            tvGrade.text = enrollment.grade ?: "No Grade"
            
            btnEdit.setOnClickListener {
                onGradeEdit(enrollment)
            }
        }
    }
}
