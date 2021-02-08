package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

class DayUI {
    private LinearLayout dayContainer_;
    private TextView date_;
    private LessonUI lesson_;

    public boolean dayIsEmpty = false;


    DayUI(Context context, String date, int firstIndex, int lastIndex, ArrayList<String> lessons, ArrayList<String> marks, ArrayList<String> hometasks) {
        // <initialization>
        dayContainer_ = new LinearLayout(context);
        date_ = new TextView(context);

        dayContainer_.setId(View.generateViewId());
        date_.setId(View.generateViewId());
        // </initialization>

        dayContainer_.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dayContainer_.setLayoutParams(containerParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        date_.setLayoutParams(params);

        date_.setText(date);
        date_.setTextSize(16f);
        date_.setGravity(Gravity.RIGHT);
        date_.setPadding(0, 0, Calculation.dpToPx(16, context), Calculation.dpToPx(1, context));
        date_.setTextColor(Color.BLACK);
        dayContainer_.addView(date_);

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

        if (firstIndex == lastIndex) {
            LessonUI tempLesson = new LessonUI(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), LessonType.THE_ONLY_LESSON);
            dayContainer_.addView(tempLesson.getLessonMarkContainer());
            dayContainer_.addView(tempLesson.getHometaskContainer());
        } else {
            LessonUI tempLesson = new LessonUI(context, lessons.get(firstIndex), marks.get(firstIndex), hometasks.get(firstIndex), LessonType.FIRST_LESSON);
            dayContainer_.addView(tempLesson.getLessonMarkContainer());
            dayContainer_.addView(tempLesson.getHometaskContainer());
            dayContainer_.addView(tempLesson.getDivider());
            for (int i = firstIndex + 1; i < lastIndex; i++) {
                tempLesson = new LessonUI(context, lessons.get(i), marks.get(i), hometasks.get(i), LessonType.MIDDLE_LESSON);
                dayContainer_.addView(tempLesson.getLessonMarkContainer());
                dayContainer_.addView(tempLesson.getHometaskContainer());
                dayContainer_.addView(tempLesson.getDivider());
            }
            tempLesson = new LessonUI(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), LessonType.LAST_LESSON);
            dayContainer_.addView(tempLesson.getLessonMarkContainer());
            dayContainer_.addView(tempLesson.getHometaskContainer());
        }
    }

    public LinearLayout getDayContainer() {
        return dayContainer_;
    }
}

enum LessonType {
    FIRST_LESSON,
    MIDDLE_LESSON,
    LAST_LESSON,
    THE_ONLY_LESSON
}

class LessonUI {
    private ConstraintLayout lessonMarkContainer_;
    private ConstraintSet lessonMarkConstraints_;
    private TextView lessonName_;
    private TextView mark_;

    private LinearLayout hometaskContainer_;
    private TextView hometask_;

    private LinearLayout divider_;

    LessonUI(Context context, String lessonName, String mark, String hometask, LessonType lessonType) {
        // <initialization>
        lessonMarkContainer_ = new ConstraintLayout(context);
        lessonName_ = new TextView(context);
        mark_ = new TextView(context);
        hometaskContainer_ = new LinearLayout(context);
        hometask_ = new TextView(context);
        divider_ = new LinearLayout(context);

        lessonMarkContainer_.setId(View.generateViewId());
        lessonName_.setId(View.generateViewId());
        mark_.setId(View.generateViewId());
        hometaskContainer_.setId(View.generateViewId());
        hometask_.setId(View.generateViewId());
        divider_.setId(View.generateViewId());
        // </initialization>



        lessonMarkContainer_.addView(lessonName_);
        lessonMarkContainer_.addView(mark_);
        lessonMarkContainer_.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

        lessonName_.setText(lessonName);
        lessonName_.setTextSize(30f);
        lessonName_.setTextColor(Color.BLACK);

        if (mark.equals("N/A")) {
            mark_.setText("");
        } else {
            mark_.setText(mark);
        }
        mark_.setTextSize(30f);
        mark_.setTextColor(Color.BLACK);

        lessonMarkConstraints_ = new ConstraintSet();
        lessonMarkConstraints_.clone(lessonMarkContainer_);

        lessonMarkConstraints_.connect(lessonName_.getId(), ConstraintSet.LEFT, lessonMarkContainer_.getId(), ConstraintSet.LEFT, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.connect(lessonName_.getId(), ConstraintSet.TOP, lessonMarkContainer_.getId(), ConstraintSet.TOP, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.connect(lessonName_.getId(), ConstraintSet.RIGHT, lessonMarkContainer_.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(84, context));
        lessonMarkConstraints_.connect(lessonName_.getId(), ConstraintSet.BOTTOM, lessonMarkContainer_.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.setHorizontalBias(lessonName_.getId(), 0f);

        lessonMarkConstraints_.connect(mark_.getId(), ConstraintSet.LEFT, lessonMarkContainer_.getId(), ConstraintSet.LEFT, Calculation.dpToPx(0, context));
        lessonMarkConstraints_.connect(mark_.getId(), ConstraintSet.TOP, lessonMarkContainer_.getId(), ConstraintSet.TOP, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.connect(mark_.getId(), ConstraintSet.RIGHT, lessonMarkContainer_.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.connect(mark_.getId(), ConstraintSet.BOTTOM, lessonMarkContainer_.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(12, context));
        lessonMarkConstraints_.setHorizontalBias(mark_.getId(), 1f);

        lessonMarkConstraints_.applyTo(lessonMarkContainer_);


        LinearLayout.LayoutParams hometaskParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hometask_.setLayoutParams(hometaskParams);
        hometask_.setPadding(Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context), Calculation.dpToPx(12, context));

        if (hometask.equals("")) {
            hometask_.setText("-");
        } else {
            hometask_.setText(hometask);
        }
        hometask_.setTextSize(18f);
        hometask_.setTextColor(Color.BLACK);
        hometaskContainer_.setOrientation(LinearLayout.VERTICAL);
        hometaskContainer_.addView(hometask_);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Calculation.dpToPx(6, context));
        divider_.setLayoutParams(params);
        switch (lessonType) {
            case FIRST_LESSON:
                lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                hometaskContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                break;
            case MIDDLE_LESSON:
                lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                hometaskContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                break;
            case LAST_LESSON:
                lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_lesson_mark_background, null));
                hometaskContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                break;
            case THE_ONLY_LESSON:
                lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                hometaskContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.last_hometask_background, null));
                break;
        }
    }

    public ConstraintLayout getLessonMarkContainer() {
        return lessonMarkContainer_;
    }

    public LinearLayout getHometaskContainer() {
        return hometaskContainer_;
    }

    public LinearLayout getDivider() {
        return divider_;
    }
}

public class JournalActivity extends AppCompatActivity {
    private ConstraintLayout SCROLL_LAYOUT;
    private String ROOT_DIRECTORY;

    private ArrayList<String> lessonNames_ = new ArrayList<>();
    private ArrayList<String> marks_ = new ArrayList<>();
    private ArrayList<String> hometasks_ = new ArrayList<>();

    private PageParser parser_;

    private DayUI day_;
    private ArrayList<DayUI> week_ = new ArrayList<>();
    private ConstraintSet cs_;

    private int maxLessons_ = -1;

    private enum ReadStage {
        LESSON,
        MARK,
        HOMETASK
    }

    private void readWeek(String weekPath) throws Exception {
        ReadStage stage = ReadStage.LESSON;
        FileReader fin = new FileReader(weekPath);
        int currentChar;
        String buf = "";

        if (maxLessons_ == -1) {
            int stagesCounter = 0;
            while ((currentChar = fin.read()) != -1) {
                if (currentChar == '>') {
                    stagesCounter++;
                    switch (stage) {
                        case LESSON:
                            stage = ReadStage.MARK;
                            lessonNames_.add(buf);
                            break;
                        case MARK:
                            stage = ReadStage.HOMETASK;
                            marks_.add(buf);
                            break;
                        case HOMETASK:
                            stage = ReadStage.LESSON;
                            hometasks_.add(buf);
                            break;
                    }
                    buf = "";
                } else {
                    buf += (char)currentChar;
                }
            }
            maxLessons_ = stagesCounter / 18;
        } else {
            while ((currentChar = fin.read()) != -1) {
                if (currentChar == '>') {
                    switch (stage) {
                        case LESSON:
                            stage = ReadStage.MARK;
                            lessonNames_.add(buf);
                            break;
                        case MARK:
                            stage = ReadStage.HOMETASK;
                            marks_.add(buf);
                            break;
                        case HOMETASK:
                            stage = ReadStage.LESSON;
                            hometasks_.add(buf);
                            break;
                    }
                    buf = "";
                } else {
                    buf += (char)currentChar;
                }
            }
        }

    }

    private void buildWeek() {
        for (int i = 0; i < week_.size(); i++) {
            SCROLL_LAYOUT.addView(week_.get(i).getDayContainer());
        }

        cs_ = new ConstraintSet();
        cs_.clone(SCROLL_LAYOUT);

        cs_.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.LEFT, SCROLL_LAYOUT.getId(), ConstraintSet.LEFT, Calculation.dpToPx(16, this));
        cs_.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.TOP, SCROLL_LAYOUT.getId(), ConstraintSet.TOP, Calculation.dpToPx(16, this));
        cs_.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.RIGHT, SCROLL_LAYOUT.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(16, this));
        cs_.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.BOTTOM, SCROLL_LAYOUT.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(16, this));
        cs_.setVerticalBias(week_.get(0).getDayContainer().getId(), 0f);

        for (int i = 1; i < week_.size(); i++) {
            cs_.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.LEFT, SCROLL_LAYOUT.getId(), ConstraintSet.LEFT, Calculation.dpToPx(16, this));
            cs_.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.TOP, week_.get(i - 1).getDayContainer().getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(80, this));
            cs_.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.RIGHT, SCROLL_LAYOUT.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(16, this));
            cs_.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.BOTTOM, SCROLL_LAYOUT.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(16, this));
            cs_.setVerticalBias(week_.get(i).getDayContainer().getId(), 0f);
        }


        cs_.applyTo(SCROLL_LAYOUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        SCROLL_LAYOUT = findViewById(R.id.ScrollLayout);
        ROOT_DIRECTORY = String.valueOf(getFilesDir());

        parser_ = new PageParser(ROOT_DIRECTORY);
        parser_.parsePage(ROOT_DIRECTORY + "/p3q/w2.html", ROOT_DIRECTORY + "/d3q/w2.txt");

        try {
            readWeek(ROOT_DIRECTORY + "/d3q/w2.txt");
        } catch (Exception e) {
            System.out.println("READING ERROR");
        }

        int startIndex = 0;
        week_.add(new DayUI(this, "1", startIndex, startIndex + maxLessons_ - 1, lessonNames_, marks_, hometasks_));
        startIndex += maxLessons_;
        week_.add(new DayUI(this, "2", startIndex, startIndex + maxLessons_ - 1, lessonNames_, marks_, hometasks_));
        startIndex += maxLessons_;
        week_.add(new DayUI(this, "3", startIndex, startIndex + maxLessons_ - 1, lessonNames_, marks_, hometasks_));
        startIndex += maxLessons_;
        week_.add(new DayUI(this, "4", startIndex, startIndex + maxLessons_ - 1, lessonNames_, marks_, hometasks_));
        startIndex += maxLessons_;
        week_.add(new DayUI(this, "5", startIndex, startIndex + maxLessons_ - 1, lessonNames_, marks_, hometasks_));

        buildWeek();
    }
}