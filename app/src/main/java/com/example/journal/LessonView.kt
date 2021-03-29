package com.example.journal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

@SuppressLint("RtlHardcoded")
class LessonView(
        context: Context,
        lessonName: String,
        mark: String,
        hometask: String,
        lessonType: LessonType,
        isLastDay: Boolean
) {
    enum class LessonType {
        FIRST_LESSON, MIDDLE_LESSON, LAST_LESSON, THE_ONLY_LESSON
    }

    private val FIRST_LESSON_LM_BACKGROUND = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.first_lesson_lm_background,
            null
    )
    private val NOT_FIRST_LESSON_LM_BACKGROUND = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.not_first_lesson_lm_background,
            null
    )
    private val NOT_LAST_LESSON_HT_BACKGROUND = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.not_last_ht_background,
            null
    )
    private val LAST_LESSON_HT_BACKGROUND = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.last_hometask_background,
            null
    )

    var lessonMarkView: LinearLayout
    var lessonNameView: TextView
    var markView: TextView
    var hometaskView: TextView

    init {
        lessonMarkView = LinearLayout(context)
        lessonNameView = TextView(context)
        markView       = TextView(context)
        hometaskView   = TextView(context)

        lessonMarkView.id = View.generateViewId()
        lessonNameView.id = View.generateViewId()
        markView.id       = View.generateViewId()
        hometaskView.id   = View.generateViewId()

        lessonNameView.text = lessonName
        lessonNameView.textSize = 30f
        lessonNameView.setTextColor(Color.BLACK)
        lessonNameView.setPadding(
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context),
                0,
                Calculation.dpToPx(12f, context)
        )
        lessonNameView.gravity = (Gravity.LEFT or Gravity.CENTER_VERTICAL)
        lessonMarkView.addView(lessonNameView)

        markView.text = if (mark == "N/A") "" else mark
        markView.textSize = 30f
        markView.setTextColor(Color.BLACK)
        markView.setPadding(
                0,
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context)
        )
        markView.gravity = Gravity.CENTER
        lessonMarkView.addView(markView)


        val lessonMarkParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lessonMarkParams.layoutDirection = LinearLayout.HORIZONTAL
        val lessonNameParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        )
        val markParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f
        )

        val hometaskParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        hometaskView.text = if (hometask == "") "-" else hometask
        hometaskView.textSize = 18f
        hometaskView.setTextColor(Color.BLACK)
        hometaskView.setPadding(
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context),
                Calculation.dpToPx(12f, context)
        )
        hometaskView.gravity = (Gravity.LEFT or Gravity.CENTER_VERTICAL)

        when (lessonType) {
            LessonType.FIRST_LESSON -> {
                lessonMarkView.background = FIRST_LESSON_LM_BACKGROUND
                hometaskView.background   = NOT_LAST_LESSON_HT_BACKGROUND
                hometaskParams.setMargins(
                        0,
                        0,
                        0,
                        Calculation.dpToPx(6f, context)
                )
            }
            LessonType.MIDDLE_LESSON -> {
                lessonMarkView.background = NOT_FIRST_LESSON_LM_BACKGROUND
                hometaskView.background   = NOT_LAST_LESSON_HT_BACKGROUND
                hometaskParams.setMargins(
                        0,
                        0,
                        0,
                        Calculation.dpToPx(6f, context)
                )
            }
            LessonType.LAST_LESSON -> {
                lessonMarkView.background = NOT_FIRST_LESSON_LM_BACKGROUND
                hometaskView.background   = LAST_LESSON_HT_BACKGROUND
                hometaskParams.setMargins(
                        0,
                        0,
                        0,
                        Calculation.dpToPx(if (isLastDay) 32f else 80f, context)
                )
            }
            LessonType.THE_ONLY_LESSON -> {
                lessonMarkView.background = FIRST_LESSON_LM_BACKGROUND
                hometaskView.background   = LAST_LESSON_HT_BACKGROUND
                hometaskParams.setMargins(
                        0,
                        0,
                        0,
                        Calculation.dpToPx(if (isLastDay) 32f else 80f, context)
                )
            }
        }

        lessonMarkView.layoutParams = lessonMarkParams
        hometaskView.layoutParams   = hometaskParams
        lessonNameView.layoutParams = lessonNameParams
        markView.layoutParams       = markParams
    }
}