package com.example.journal

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView

class DayView(
        context: Context,
        date: String,
        lessons: ArrayList<String>,
        marks: ArrayList<String>,
        hometasks: ArrayList<String>,
        firstIndex: Int,
        private var lastIndex: Int,
        isLastDay: Boolean
) {
    private var dateView: TextView
    private var lessonsViews = ArrayList<LessonView>()

    init {
        dateView = TextView(context)
        dateView.id = View.generateViewId()

        dateView.text = date
        dateView.textSize = 16f
        dateView.gravity = Gravity.RIGHT
        dateView.setPadding(
                0,
                0,
                Calculation.dpToPx(16f, context),
                Calculation.dpToPx(1f, context)
        )
        dateView.setTextColor(Color.BLACK)
        dateView.layoutParams = TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        for (i in lastIndex downTo firstIndex) {
            lastIndex = i
            if (lessons[i].length > 1) break
        }

        if (firstIndex == lastIndex) {
            lessonsViews.add(LessonView(
                    context,
                    lessons[lastIndex], marks[lastIndex], hometasks[lastIndex],
                    LessonView.LessonType.THE_ONLY_LESSON, isLastDay
            ))
        } else if (lastIndex > firstIndex) {
            lessonsViews.add(LessonView(
                    context,
                    lessons[firstIndex], marks[firstIndex], hometasks[firstIndex],
                    LessonView.LessonType.FIRST_LESSON, isLastDay
            ))
            for (i in firstIndex + 1 until lastIndex) {
                lessonsViews.add(LessonView(
                        context,
                        lessons[i], marks[i], hometasks[i],
                        LessonView.LessonType.MIDDLE_LESSON, isLastDay
                ))
            }
            lessonsViews.add(LessonView(
                    context,
                    lessons[lastIndex], marks[lastIndex], hometasks[lastIndex],
                    LessonView.LessonType.MIDDLE_LESSON, isLastDay
            ))
        } else {

        }
    }
}