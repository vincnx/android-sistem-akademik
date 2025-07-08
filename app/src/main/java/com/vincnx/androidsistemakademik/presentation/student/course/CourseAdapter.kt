package com.vincnx.androidsistemakademik.presentation.student.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.domain.entities.Enrollment

class CourseAdapter : RecyclerView.Adapter<CourseAdapter.EnrollmentViewHolder>() {
    private var enrollments = listOf<Enrollment>()

    fun submitList(newList: List<Enrollment>) {
        enrollments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return EnrollmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        holder.bind(enrollments[position])
    }

    override fun getItemCount() = enrollments.size

    class EnrollmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tv_course_name)
        private val tvGrade: TextView = itemView.findViewById(R.id.tv_grade)

        fun bind(enrollment: Enrollment) {
            tvCourseName.text = enrollment.course?.name
            tvGrade.text = enrollment.grade ?: "-"
        }
    }
}