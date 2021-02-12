package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

class DayUI {
    private LinearLayout dayContainer_;
    private TextView date_;
    private LessonUI lesson_;

    public boolean dayIsEmpty = false;


    public DayUI(Context context, int firstIndex, int lastIndex, String date, ArrayList<String> lessons, ArrayList<String> marks, ArrayList<String> hometasks) {
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
            for (int i = firstIndex + 1; i < lastIndex; i++) {
                tempLesson = new LessonUI(context, lessons.get(i), marks.get(i), hometasks.get(i), LessonType.MIDDLE_LESSON);
                dayContainer_.addView(tempLesson.getLessonMarkContainer());
                dayContainer_.addView(tempLesson.getHometaskContainer());
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

    public LessonUI(Context context, String lessonName, String mark, String hometask, LessonType lessonType) {
        // <initialization>
        lessonMarkContainer_ = new ConstraintLayout(context);
        lessonName_ = new TextView(context);
        mark_ = new TextView(context);
        hometaskContainer_ = new LinearLayout(context);
        hometask_ = new TextView(context);

        lessonMarkContainer_.setId(View.generateViewId());
        lessonName_.setId(View.generateViewId());
        mark_.setId(View.generateViewId());
        hometaskContainer_.setId(View.generateViewId());
        hometask_.setId(View.generateViewId());
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, Calculation.dpToPx(6, context));
        switch (lessonType) {
            case FIRST_LESSON:
                hometaskContainer_.setLayoutParams(params);
                lessonMarkContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.first_lesson_mark_background, null));
                hometaskContainer_.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.middle_hometask_background, null));
                break;
            case MIDDLE_LESSON:
                hometaskContainer_.setLayoutParams(params);
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
}

public class MainMenuActivity extends AppCompatActivity {
    private String ROOT_DIRECTORY;

    private ConstraintLayout SCROLL_LAYOUT;
    private ConstraintSet scrollLayoutSet = new ConstraintSet();;
    private ConstraintLayout ROOT_LAYOUT;
    private ConstraintSet rootLayoutSet = new ConstraintSet();
    private TextView statusText_;

    private ArrayList<DayUI> week_ = new ArrayList<>();

    private WeekManager weekManager_;

    private boolean isOne_ = true;

    private void buildWeek() {
        for (int i = 0; i < week_.size(); i++) {
            SCROLL_LAYOUT.addView(week_.get(i).getDayContainer());
        }

        scrollLayoutSet.clone(SCROLL_LAYOUT);

        scrollLayoutSet.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.LEFT, SCROLL_LAYOUT.getId(), ConstraintSet.LEFT, Calculation.dpToPx(16, this));
        scrollLayoutSet.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.TOP, SCROLL_LAYOUT.getId(), ConstraintSet.TOP, Calculation.dpToPx(16, this));
        scrollLayoutSet.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.RIGHT, SCROLL_LAYOUT.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(16, this));
        scrollLayoutSet.connect(week_.get(0).getDayContainer().getId(), ConstraintSet.BOTTOM, SCROLL_LAYOUT.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(16, this));
        scrollLayoutSet.setVerticalBias(week_.get(0).getDayContainer().getId(), 0f);

        for (int i = 1; i < week_.size(); i++) {
            scrollLayoutSet.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.LEFT, SCROLL_LAYOUT.getId(), ConstraintSet.LEFT, Calculation.dpToPx(16, this));
            scrollLayoutSet.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.TOP, week_.get(i - 1).getDayContainer().getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(80, this));
            scrollLayoutSet.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.RIGHT, SCROLL_LAYOUT.getId(), ConstraintSet.RIGHT, Calculation.dpToPx(16, this));
            scrollLayoutSet.connect(week_.get(i).getDayContainer().getId(), ConstraintSet.BOTTOM, SCROLL_LAYOUT.getId(), ConstraintSet.BOTTOM, Calculation.dpToPx(16, this));
            scrollLayoutSet.setVerticalBias(week_.get(i).getDayContainer().getId(), 0f);
        }

        scrollLayoutSet.applyTo(SCROLL_LAYOUT);
    }

    class UpdatePageThreadRunnable implements Runnable {
        private Context context_;
        private int quarterNumber_, weekNumber_;
        private boolean weekIsReady_;
        private String pagePath_ = "";
        private String dataPath_ = "";

        UpdatePageThreadRunnable(Context context, int quarter, int week, boolean weekState) {
            context_ = context;
            quarterNumber_ = quarter;
            weekNumber_ = week;
            weekIsReady_ = weekState;

            pagePath_ = (ROOT_DIRECTORY + "/p" + Integer.toString(quarterNumber_) + "q/w" + Integer.toString(weekNumber_) + ".html");
            dataPath_ = (ROOT_DIRECTORY + "/d" + Integer.toString(quarterNumber_) + "q/w" + Integer.toString(weekNumber_) + ".txt");
        }

        class SetDownloadingStatusRunnable implements Runnable {
            @Override
            public void run() {
                statusText_.setText("Downloading...");
            }
        }
        class SetGatheringInfoStatusRunnable implements Runnable {
            @Override
            public void run() {
                statusText_.setText("Gathering info...");
            }
        }
        class SetDownloadingErrorStatusRunnable implements Runnable {
            @Override
            public void run() {
                statusText_.setText("Downloading\nerror");
            }
        }
        class SetGatheringInfoErrorStatusRunnable implements Runnable {
            @Override
            public void run() {
                statusText_.setText("Gathering info\nerror");
            }
        }
        class SetLoadingStatusRunnable implements Runnable {
            @Override
            public void run() {
                statusText_.setText("Loading...");
            }
        }
        class BuildWeekRunnable implements Runnable {
            @Override
            public void run() {
                buildWeek();
                statusText_.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void run() {
            if (!weekIsReady_) {
                runOnUiThread(new SetDownloadingStatusRunnable());
                if (weekManager_.downloadPage(quarterNumber_, weekNumber_)) {
                    runOnUiThread(new SetGatheringInfoStatusRunnable());
                } else {
                    runOnUiThread(new SetDownloadingErrorStatusRunnable());
                    return;
                }

                weekManager_.pageParser.parsePage(pagePath_, dataPath_);
                try {
                    weekManager_.readWeek(dataPath_, quarterNumber_, weekNumber_);
                } catch (Exception e) {
                    runOnUiThread(new SetGatheringInfoErrorStatusRunnable());
                    return;
                }
                runOnUiThread(new SetLoadingStatusRunnable());
            } else {
                runOnUiThread(new SetGatheringInfoStatusRunnable());
                try {
                    weekManager_.readWeek(dataPath_, quarterNumber_, weekNumber_);
                } catch (Exception e) {
                    runOnUiThread(new SetGatheringInfoErrorStatusRunnable());
                    return;
                }
                runOnUiThread(new SetLoadingStatusRunnable());
            }

            int startIndex = 0;
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    "1", weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_)));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    "2", weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_)));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    "3", weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_)));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    "4", weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_)));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    "5", weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_)));

            runOnUiThread(new BuildWeekRunnable());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        ROOT_LAYOUT = findViewById(R.id.RootLayout);
        statusText_ = findViewById(R.id.StatusText);

        weekManager_ = new WeekManager(ROOT_DIRECTORY, getIntent().getStringExtra("csrftoken"));

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        int buttonHeight = (int)(screenHeight / 10);
        int fragmentHeight = screenHeight - buttonHeight - statusBarHeight;

        ViewCompat.setTranslationZ(findViewById(R.id.JournalButton), 100f);
        ViewCompat.setTranslationZ(findViewById(R.id.JournalFragment), 100f);

        rootLayoutSet.clone(ROOT_LAYOUT);
        rootLayoutSet.constrainHeight(R.id.JournalButton, buttonHeight);
        rootLayoutSet.constrainHeight(R.id.JournalFragment, fragmentHeight);
        rootLayoutSet.applyTo(ROOT_LAYOUT);

        setRealName();

        SCROLL_LAYOUT = findViewById(R.id.ScrollLayout);

        Thread downloadThread = new Thread(new UpdatePageThreadRunnable(this, 3, 1, false));
        downloadThread.start();
    }

    @Override
    public void onBackPressed() {
        if (!isOne_) {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 1f);
            rootLayoutSet.applyTo(ROOT_LAYOUT);
            isOne_ = !isOne_;
        }
    }
    public void logOutButtonOnClick(View view) {
        cleanUserData();
        Intent logInActivity = new Intent(this, LogInActivity.class);
        startActivity(logInActivity);
    }
    public void journalButtonOnClick(View view) {
        if (isOne_) {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 0f);
        } else {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 1f);
        }
        rootLayoutSet.applyTo(ROOT_LAYOUT);
        isOne_ = !isOne_;
    }

    private void setUserName() {
        String buffer = new String("");
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/username.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = (TextView)findViewById(R.id.UserNameTextView);
        userNameTextView.setText(buffer);
    }
    private void setRealName() {
        String buffer = new String("");
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/realName.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = (TextView)findViewById(R.id.UserNameTextView);
        userNameTextView.setText(buffer);
    }

    private void cleanUserData() {
        File userDataFolder = new File(ROOT_DIRECTORY + "/UserData");
        File statusText = new File(ROOT_DIRECTORY + "/UserData/status.txt");
        for (File f : userDataFolder.listFiles()) {
            f.delete();
        }

        try {
            FileWriter fout = new FileWriter(statusText.getPath());
            fout.write("NO");
            fout.flush();
            fout.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}