package com.example.journal;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class ViewSets {
    public static class Lesson {
        enum LessonType {
            FIRST_LESSON,
            MIDDLE_LESSON,
            LAST_LESSON,
            THE_ONLY_LESSON
        }

        private LinearLayout lessonMarkContainer_;
        private TextView lessonName_;
        private TextView mark_;

        private TextView hometask_;

        public Lesson(Context context, String lessonName, String mark, String hometask, LessonType lessonType, boolean lastDay) {
            // initialization
            lessonMarkContainer_ = new LinearLayout(context);
            lessonName_ = new TextView(context);
            mark_ = new TextView(context);
            hometask_ = new TextView(context);

            lessonMarkContainer_.setId(View.generateViewId());
            lessonName_.setId(View.generateViewId());
            mark_.setId(View.generateViewId());
            hometask_.setId(View.generateViewId());

            // configure lessonName
            lessonName_.setText(lessonName);
            lessonName_.setTextSize(30f);
            lessonName_.setTextColor(Color.BLACK);
            lessonName_.setPadding(Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), 0, Calculation.dpToPx(12, context));
            lessonName_.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            // configure mark
            if (mark.equals("N/A")) {
                mark_.setText("");
            } else {
                mark_.setText(mark);
            }
            mark_.setTextSize(30f);
            mark_.setTextColor(Color.BLACK);
            mark_.setPadding(0, Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context));
            mark_.setGravity(Gravity.CENTER);

            // configure lessonMarkContainer
            lessonMarkContainer_.addView(lessonName_);
            lessonMarkContainer_.addView(mark_);

            LinearLayout.LayoutParams lessonMarkParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lessonMarkParams.setLayoutDirection(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lessonNameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            LinearLayout.LayoutParams markParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);

            // configure hometask
            TableLayout.LayoutParams hometaskParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hometask_.setPadding(Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context));
            if (hometask.equals("")) {
                hometask_.setText("-");
            } else {
                hometask_.setText(hometask);
            }
            hometask_.setTextSize(18f);
            hometask_.setTextColor(Color.BLACK);
            hometask_.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            // fill lessons array
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
            switch (lessonType) {
                case FIRST_LESSON:
                    lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                    hometask_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                    hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
                    break;
                case MIDDLE_LESSON:
                    lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                    hometask_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                    hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
                    break;
                case LAST_LESSON:
                    lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                    hometask_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                    if (lastDay) {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(32, context));
                    } else {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(80, context));
                    }
                    break;
                case THE_ONLY_LESSON:
                    lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                    hometask_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                    if (lastDay) {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(32, context));
                    } else {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(80, context));
                    }
                    break;
            }

            // apply layout params
            lessonMarkContainer_.setLayoutParams(lessonMarkParams);
            lessonName_.setLayoutParams(lessonNameParams);
            mark_.setLayoutParams(markParams);
            hometask_.setLayoutParams(hometaskParams);
        }

        public LinearLayout getLessonMarkContainer() {
            return lessonMarkContainer_;
        }
        public TextView getHometask() {
            return hometask_;
        }
    }
    public static class Day {
        private TextView date_;
        private ArrayList<Lesson> lessons_ = new ArrayList<>();

        public boolean dayIsEmpty = false;


        public Day(Context context, int firstIndex, int lastIndex, String date, ArrayList<String> lessons, ArrayList<String> marks, ArrayList<String> hometasks, boolean lastDay) {
            // initialization
            date_ = new TextView(context);
            date_.setId(View.generateViewId());

            // configure date
            date_.setText(date);
            date_.setTextSize(16f);
            date_.setGravity(Gravity.RIGHT);
            date_.setPadding(0, 0, Calculation.dpToPx(16, context), Calculation.dpToPx(1, context));
            date_.setTextColor(Color.BLACK);

            TableLayout.LayoutParams dateParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            date_.setLayoutParams(dateParams);

            // define empty day (in progress)
            boolean triggered = false;
            for (int i = lastIndex; i >= firstIndex; i--) {
                if (!lessons.get(i).equals("-")) {
                    lastIndex = i;
                    triggered = true;
                    break;
                }
            }

            if (!triggered) {
                dayIsEmpty = true;
                return;
            }

            // fill lessons array
            if (firstIndex == lastIndex) {
                lessons_.add(new Lesson(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), Lesson.LessonType.THE_ONLY_LESSON, lastDay));
            } else {
                lessons_.add(new Lesson(context, lessons.get(firstIndex), marks.get(firstIndex), hometasks.get(firstIndex), Lesson.LessonType.FIRST_LESSON, false));
                for (int i = firstIndex + 1; i < lastIndex; i++) {
                    lessons_.add(new Lesson(context, lessons.get(i), marks.get(i), hometasks.get(i), Lesson.LessonType.MIDDLE_LESSON, false));
                }
                lessons_.add(new Lesson(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), Lesson.LessonType.LAST_LESSON, lastDay));
            }
        }

        public TextView getDate() {
            return date_;
        }
        public ArrayList<Lesson> getLessons() {
            return lessons_;
        }
    }
}
