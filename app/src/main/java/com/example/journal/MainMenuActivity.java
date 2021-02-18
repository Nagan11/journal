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
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

enum LessonType {
    FIRST_LESSON,
    MIDDLE_LESSON,
    LAST_LESSON,
    THE_ONLY_LESSON
}

class LessonUI {
    private LinearLayout lessonMarkContainer_;
    private TextView lessonName_;
    private TextView mark_;

    private TextView hometask_;

    public LessonUI(Context context, String lessonName, String mark, String hometask, LessonType lessonType, boolean lastDay) {
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

class DayUI {
    private TextView date_;
    private ArrayList<LessonUI> lessons_ = new ArrayList<>();

    public boolean dayIsEmpty = false;


    public DayUI(Context context, int firstIndex, int lastIndex, String date, ArrayList<String> lessons, ArrayList<String> marks, ArrayList<String> hometasks, boolean lastDay) {
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
            lessons_.add(new LessonUI(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), LessonType.THE_ONLY_LESSON, lastDay));
        } else {
            lessons_.add(new LessonUI(context, lessons.get(firstIndex), marks.get(firstIndex), hometasks.get(firstIndex), LessonType.FIRST_LESSON, false));
            for (int i = firstIndex + 1; i < lastIndex; i++) {
                lessons_.add(new LessonUI(context, lessons.get(i), marks.get(i), hometasks.get(i), LessonType.MIDDLE_LESSON, false));
            }
            lessons_.add(new LessonUI(context, lessons.get(lastIndex), marks.get(lastIndex), hometasks.get(lastIndex), LessonType.LAST_LESSON, lastDay));
        }
    }

    public TextView getDate() {
        return date_;
    }
    public ArrayList<LessonUI> getLessons() {
        return lessons_;
    }
}

public class MainMenuActivity extends AppCompatActivity {
    private String ROOT_DIRECTORY;

    private LinearLayout SCROLL_LAYOUT;
    private ConstraintLayout ROOT_LAYOUT;
    private ConstraintSet rootLayoutSet = new ConstraintSet();

    private TextView statusText_;

    private ArrayList<DayUI> week_ = new ArrayList<>();

    private WeekManager weekManager_;

    private boolean isOne_ = true;

    int statusBarHeight_ = 0;

    private void buildWeek() {
        for (int i = 0; i < week_.size(); i++) {
            SCROLL_LAYOUT.addView(week_.get(i).getDate());
            for (LessonUI l : week_.get(i).getLessons()) {
                SCROLL_LAYOUT.addView(l.getLessonMarkContainer());
                SCROLL_LAYOUT.addView(l.getHometask());
            }
        }
    }

    class UpdatePageThreadRunnable implements Runnable {
        private Context context_;
        private int quarterNumber_, weekNumber_;
        private boolean weekIsReady_;
        private String pagePath_ = "";
        private String dataPath_ = "";
        long start_;

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
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(0), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(1), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(2), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(3), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new DayUI(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(4), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), true));

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

        setStatusBarHeight();
        int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        int buttonHeight = (int)(screenHeight / 10);
        int fragmentHeight = screenHeight - buttonHeight - statusBarHeight_;

        ViewCompat.setTranslationZ(findViewById(R.id.JournalButton), 100f);
        ViewCompat.setTranslationZ(findViewById(R.id.JournalFragment), 100f);

        rootLayoutSet.clone(ROOT_LAYOUT);
        rootLayoutSet.constrainHeight(R.id.JournalButton, buttonHeight);
        rootLayoutSet.constrainHeight(R.id.JournalFragment, fragmentHeight);
        rootLayoutSet.applyTo(ROOT_LAYOUT);

        setRealName();

        SCROLL_LAYOUT = findViewById(R.id.ScrollLayout);

        Thread downloadThread = new Thread(new UpdatePageThreadRunnable(this, weekManager_.getCurrentQuarter(), weekManager_.getCurrentWeek(), false));
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
    private void setStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight_ = getResources().getDimensionPixelSize(resourceId);
        }
    }
}