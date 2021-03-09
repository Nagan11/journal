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

        private LinearLayout lessonMarkContainer;
        private TextView lessonName;
        private TextView mark;

        private TextView hometask;

        public Lesson(Context context, String lessonName, String mark, String hometask, LessonType lessonType, boolean lastDay) {
            // initialization
            lessonMarkContainer = new LinearLayout(context);
            this.lessonName = new TextView(context);
            this.mark = new TextView(context);
            this.hometask = new TextView(context);

            lessonMarkContainer.setId(View.generateViewId());
            this.lessonName.setId(View.generateViewId());
            this.mark.setId(View.generateViewId());
            this.hometask.setId(View.generateViewId());

            // configure lessonName
            this.lessonName.setText(lessonName);
            this.lessonName.setTextSize(30f);
            this.lessonName.setTextColor(Color.BLACK);
            this.lessonName.setPadding(Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), 0, Calculation.dpToPx(12, context));
            this.lessonName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            // configure mark
            if (mark.equals("N/A")) {
                this.mark.setText("");
            } else {
                this.mark.setText(mark);
            }
            this.mark.setTextSize(30f);
            this.mark.setTextColor(Color.BLACK);
            this.mark.setPadding(0, Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context));
            this.mark.setGravity(Gravity.CENTER);

            // configure lessonMarkContainer
            lessonMarkContainer.addView(this.lessonName);
            lessonMarkContainer.addView(this.mark);

            LinearLayout.LayoutParams lessonMarkParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lessonMarkParams.setLayoutDirection(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lessonNameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            LinearLayout.LayoutParams markParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);

            // configure hometask
            TableLayout.LayoutParams hometaskParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.hometask.setPadding(Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context));
            if (hometask.equals("")) {
                this.hometask.setText("-");
            } else {
                this.hometask.setText(hometask);
            }
            this.hometask.setTextSize(18f);
            this.hometask.setTextColor(Color.BLACK);
            this.hometask.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            // fill lessons array
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
            switch (lessonType) {
                case FIRST_LESSON:
                    lessonMarkContainer.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                    this.hometask.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                    hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
                    break;
                case MIDDLE_LESSON:
                    lessonMarkContainer.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                    this.hometask.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                    hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
                    break;
                case LAST_LESSON:
                    lessonMarkContainer.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                    this.hometask.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                    if (lastDay) {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(32, context));
                    } else {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(80, context));
                    }
                    break;
                case THE_ONLY_LESSON:
                    lessonMarkContainer.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                    this.hometask.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                    if (lastDay) {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(32, context));
                    } else {
                        hometaskParams.setMargins(0, 0, 0, Calculation.dpToPx(80, context));
                    }
                    break;
            }

            // apply layout params
            lessonMarkContainer.setLayoutParams(lessonMarkParams);
            this.lessonName.setLayoutParams(lessonNameParams);
            this.mark.setLayoutParams(markParams);
            this.hometask.setLayoutParams(hometaskParams);
        }

        public LinearLayout getLessonMarkContainer() {
            return lessonMarkContainer;
        }
        public TextView getHometask() {
            return hometask;
        }
    }

    public static class Day {
        private TextView date;
        private ArrayList<Lesson> lessons = new ArrayList<>();

        public boolean dayIsEmpty = false;

        public Day(Context context, int firstIndex, int lastIndex, String date, ArrayList<String> lessons, ArrayList<String> marks, ArrayList<String> hometasks, boolean lastDay) {
            // initialization
            this.date = new TextView(context);
            this.date.setId(View.generateViewId());

            // configure date
            this.date.setText(date);
            this.date.setTextSize(16f);
            this.date.setGravity(Gravity.RIGHT);
            this.date.setPadding(0, 0, Calculation.dpToPx(16, context), Calculation.dpToPx(1, context));
            this.date.setTextColor(Color.BLACK);

            TableLayout.LayoutParams dateParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.date.setLayoutParams(dateParams);

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
                this.lessons.add(new Lesson(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), Lesson.LessonType.THE_ONLY_LESSON, lastDay));
            } else {
                this.lessons.add(new Lesson(context, lessons.get(firstIndex), marks.get(firstIndex), hometasks.get(firstIndex), Lesson.LessonType.FIRST_LESSON, false));
                for (int i = firstIndex + 1; i < lastIndex; i++) {
                    this.lessons.add(new Lesson(context, lessons.get(i), marks.get(i), hometasks.get(i), Lesson.LessonType.MIDDLE_LESSON, false));
                }
                this.lessons.add(new Lesson(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), Lesson.LessonType.LAST_LESSON, lastDay));
            }
        }

        public TextView getDate() {
            return date;
        }
        public ArrayList<Lesson> getLessons() {
            return lessons;
        }
    }
}
