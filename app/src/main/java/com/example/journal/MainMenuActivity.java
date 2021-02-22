package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;



public class MainMenuActivity extends AppCompatActivity {
    private String ROOT_DIRECTORY;
    private int SCREEN_WIDTH = 1080;
    private int SCREEN_HEIGHT = 1920;
    private int STATUS_BAR_HEIGHT = 0;
    private int fragmentHeight_;
    private int buttonHeight_;

    private ArrayList<Fragment> FRAGMENTS = new ArrayList<>();
    private ArrayList<LinearLayout> SCROLL_LAYOUTS = new ArrayList<>();
    private ArrayList<TextView> STATUS_TEXTS = new ArrayList<>();
    private ConstraintLayout ROOT_LAYOUT;

    private ConstraintSet rootLayoutSet_ = new ConstraintSet();
    private int[] currentOrder_ = new int[] {0, 1, 2};

    private WeekManager weekManager_;
    private PageParser parser_;

    private int middleQuarter_, middleWeek_;

    private boolean isOne_ = true;

    class UpdatePageThreadRunnable implements Runnable {
        private Context context_;

        private PageDownloader downloader_;


        private ArrayList<ViewSets.Day> week_ = new ArrayList<>();
        private int quarterNumber_, weekNumber_;
        private boolean weekIsReady_;
        private String pagePath_ = "";
        private String dataPath_ = "";
        private int index_;

        UpdatePageThreadRunnable(Context context, int index, int quarter, int week, boolean weekState) {
            context_ = context;
            index_ = index;
            quarterNumber_ = quarter;
            weekNumber_ = week;
            weekIsReady_ = weekState;

            downloader_ = new PageDownloader(ROOT_DIRECTORY, weekManager_.getSessionid(), weekManager_.getPupilUrl());

            pagePath_ = (ROOT_DIRECTORY + "/p" + Integer.toString(quarterNumber_) + "q/w" + Integer.toString(weekNumber_) + ".html");
            dataPath_ = (ROOT_DIRECTORY + "/d" + Integer.toString(quarterNumber_) + "q/w" + Integer.toString(weekNumber_) + ".txt");
        }

        private void buildWeek(int index) {
//            SCROLL_LAYOUTS.get(index).removeAllViews();
            for (int i = 0; i < week_.size(); i++) {
                SCROLL_LAYOUTS.get(index).addView(week_.get(i).getDate());
                for (ViewSets.Lesson l : week_.get(i).getLessons()) {
                    SCROLL_LAYOUTS.get(index).addView(l.getLessonMarkContainer());
                    SCROLL_LAYOUTS.get(index).addView(l.getHometask());
                }
            }
        }

        class SetDownloadingStatusRunnable implements Runnable {
            @Override
            public void run() {
                STATUS_TEXTS.get(index_).setText("Downloading...");
            }
        }
        class SetGatheringInfoStatusRunnable implements Runnable {
            @Override
            public void run() {
                STATUS_TEXTS.get(index_).setText("Gathering info...");
            }
        }
        class SetDownloadingErrorStatusRunnable implements Runnable {
            @Override
            public void run() {
                STATUS_TEXTS.get(index_).setText("Downloading\nerror");
            }
        }
        class SetGatheringInfoErrorStatusRunnable implements Runnable {
            @Override
            public void run() {
                STATUS_TEXTS.get(index_).setText("Gathering info\nerror");
            }
        }
        class SetLoadingStatusRunnable implements Runnable {
            @Override
            public void run() {
                STATUS_TEXTS.get(index_).setText("Loading...");
            }
        }
        class BuildWeekRunnable implements Runnable {
            @Override
            public void run() {
                buildWeek(index_);
                STATUS_TEXTS.get(index_).setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void run() {
            if (!weekIsReady_) {
                runOnUiThread(new SetDownloadingStatusRunnable());
                if (downloader_.downloadPage(quarterNumber_, weekNumber_, weekManager_.getLink(quarterNumber_, weekNumber_))) {
                    runOnUiThread(new SetGatheringInfoStatusRunnable());
                } else {
                    runOnUiThread(new SetDownloadingErrorStatusRunnable());
                    return;
                }

                synchronized (parser_) {
                    parser_.parsePage(pagePath_, dataPath_);
                }

                try {
                    synchronized (weekManager_) {
                        weekManager_.readWeek(dataPath_, quarterNumber_, weekNumber_);
                    }
                } catch (Exception e) {
                    runOnUiThread(new SetGatheringInfoErrorStatusRunnable());
                    return;
                }
                runOnUiThread(new SetLoadingStatusRunnable());
            } else {
                runOnUiThread(new SetGatheringInfoStatusRunnable());
                try {
                    synchronized (weekManager_) {
                        weekManager_.readWeek(dataPath_, quarterNumber_, weekNumber_);
                    }
                } catch (Exception e) {
                    runOnUiThread(new SetGatheringInfoErrorStatusRunnable());
                    return;
                }
                runOnUiThread(new SetLoadingStatusRunnable());
            }

            int startIndex = 0;
            week_.add(new ViewSets.Day(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(0), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new ViewSets.Day(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(1), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new ViewSets.Day(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(2), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new ViewSets.Day(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    weekManager_.getDates(quarterNumber_, weekNumber_).get(3), weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_), weekManager_.getHometasks(quarterNumber_, weekNumber_), false));
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            week_.add(new ViewSets.Day(context_, startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
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
        weekManager_ = new WeekManager(ROOT_DIRECTORY, getIntent().getStringExtra("csrftoken"));
        parser_ = new PageParser(ROOT_DIRECTORY);
        fillFragmentArrays();

        setScreenInfo();
        buttonHeight_ = (int)(SCREEN_HEIGHT * 0.085f);
        fragmentHeight_ = SCREEN_HEIGHT - buttonHeight_ - buttonHeight_ - STATUS_BAR_HEIGHT;

        setRootLayoutStartState();

        setRealName();

        middleQuarter_ = weekManager_.getCurrentQuarter();
        middleWeek_ = weekManager_.getCurrentWeek();
        System.out.println(middleQuarter_ + " " + middleWeek_);

        Thread downloadThread0 = new Thread(new UpdatePageThreadRunnable(this, 0, weekManager_.getCurrentQuarter(), weekManager_.getCurrentWeek() - 1, false));
        Thread downloadThread1 = new Thread(new UpdatePageThreadRunnable(this, 1, weekManager_.getCurrentQuarter(), weekManager_.getCurrentWeek(), false));
        Thread downloadThread2 = new Thread(new UpdatePageThreadRunnable(this, 2, weekManager_.getCurrentQuarter(), weekManager_.getCurrentWeek() + 1, false));
        downloadThread0.start();
        downloadThread1.start();
        downloadThread2.start();
    }

    @Override
    public void onBackPressed() {
        if (!isOne_) {
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 1f);
            rootLayoutSet_.applyTo(ROOT_LAYOUT);
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
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 0f);
        } else {
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 1f);
        }
        rootLayoutSet_.applyTo(ROOT_LAYOUT);
        isOne_ = !isOne_;
    }
    public void leftArrowOnClick(View view) {
        scrollLeft();
    }
    public void rightArrowOnClick(View view) {
        scrollRight();
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
    private void setScreenInfo() {
        SCREEN_WIDTH = this.getResources().getDisplayMetrics().widthPixels;
        SCREEN_HEIGHT = this.getResources().getDisplayMetrics().heightPixels;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            STATUS_BAR_HEIGHT = getResources().getDimensionPixelSize(resourceId);
        }
    }
    private void fillFragmentArrays() {
        FRAGMENTS.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment0));
        FRAGMENTS.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment1));
        FRAGMENTS.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment2));

        SCROLL_LAYOUTS.add((LinearLayout)findViewById(R.id.ScrollLayout0));
        SCROLL_LAYOUTS.add((LinearLayout)findViewById(R.id.ScrollLayout1));
        SCROLL_LAYOUTS.add((LinearLayout)findViewById(R.id.ScrollLayout2));

        STATUS_TEXTS.add((TextView)findViewById(R.id.StatusText0));
        STATUS_TEXTS.add((TextView)findViewById(R.id.StatusText1));
        STATUS_TEXTS.add((TextView)findViewById(R.id.StatusText2));
    }

    private void alignFragments() {
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[0]).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[0]).getId(), ConstraintSet.RIGHT);
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.RIGHT);
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[2]).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(currentOrder_[2]).getId(), ConstraintSet.RIGHT);

        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(currentOrder_[1]).getId(), 0.5f);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[0]).getId(), ConstraintSet.RIGHT, FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[0]).getId(), ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(currentOrder_[0]).getId(), 1f);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[2]).getId(), ConstraintSet.LEFT, FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[2]).getId(), ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(currentOrder_[2]).getId(), 0f);

        rootLayoutSet_.applyTo(ROOT_LAYOUT);
    }
    private void scrollLeft() {
        middleWeek_--;
        if (middleWeek_ < 1) {
            middleQuarter_--;
            if (middleQuarter_ < 1) {
                middleQuarter_ = 1;
                middleWeek_ = 1;
                return;
            } else {
                middleWeek_ = weekManager_.getAmountOfWeeks(middleQuarter_);
            }
        }

        boolean download = true;
        int quarterToDownload  = middleQuarter_, weekToDownload = middleWeek_;
        weekToDownload--;
        if (weekToDownload < 1) {
            quarterToDownload--;
            if (quarterToDownload < 1) {
                download = false;
            } else {
                weekToDownload = weekManager_.getAmountOfWeeks(quarterToDownload);
            }
        }

        int[] tempAr = new int[] {currentOrder_[2], currentOrder_[0], currentOrder_[1]};
        currentOrder_ = tempAr;

        System.out.println("middle index -> " + currentOrder_[1]);
        System.out.println("qw: " + quarterToDownload + " " + weekToDownload);

        if (download) {
            SCROLL_LAYOUTS.get(currentOrder_[0]).removeAllViews();
            STATUS_TEXTS.get(currentOrder_[0]).setVisibility(View.VISIBLE);
            Thread dt = new Thread(new UpdatePageThreadRunnable(this, currentOrder_[0], quarterToDownload, weekToDownload, false));
            dt.start();
        }

        alignFragments();
    }
    private void scrollRight() {
        middleWeek_++;
        if (middleWeek_ > weekManager_.getAmountOfWeeks(middleQuarter_)) {
            middleQuarter_++;
            if (middleQuarter_ > 4) {
                middleQuarter_ = 4;
                middleWeek_ = weekManager_.getAmountOfWeeks(middleQuarter_);
                return;
            } else {
                middleWeek_ = 1;
            }
        }

        boolean download = true;
        int quarterToDownload  = middleQuarter_, weekToDownload = middleWeek_;
        weekToDownload++;
        if (weekToDownload > weekManager_.getAmountOfWeeks(middleQuarter_)) {
            quarterToDownload++;
            if (quarterToDownload > 4) {
                download = false;
            } else {
                weekToDownload = 1;
            }
        }

        int[] tempAr = new int[] {currentOrder_[1], currentOrder_[2], currentOrder_[0]};
        currentOrder_ = tempAr;

        if (download) {
            SCROLL_LAYOUTS.get(currentOrder_[2]).removeAllViews();
            STATUS_TEXTS.get(currentOrder_[2]).setVisibility(View.VISIBLE);
            Thread dt = new Thread(new UpdatePageThreadRunnable(this, currentOrder_[2], quarterToDownload, weekToDownload, false));
            dt.start();
        }

        alignFragments();
    }
    private void setRootLayoutStartState() {
        rootLayoutSet_.clone(ROOT_LAYOUT);
        rootLayoutSet_.clear(R.id.JournalFragment0);
        rootLayoutSet_.clear(R.id.JournalFragment1);
        rootLayoutSet_.clear(R.id.JournalFragment2);

        rootLayoutSet_.setTranslationZ(ROOT_LAYOUT.getId(), 0f);
        rootLayoutSet_.setTranslationZ(R.id.JournalButton, 100f);
        rootLayoutSet_.setTranslationZ(R.id.ControlPanel, 100f);
        rootLayoutSet_.setTranslationZ(R.id.JournalFragment0, 100f);
        rootLayoutSet_.setTranslationZ(R.id.JournalFragment1, 100f);
        rootLayoutSet_.setTranslationZ(R.id.JournalFragment2, 100f);

        rootLayoutSet_.constrainHeight(R.id.JournalFragment0, fragmentHeight_);
        rootLayoutSet_.constrainWidth(R.id.JournalFragment0, SCREEN_WIDTH);
        rootLayoutSet_.constrainHeight(R.id.JournalFragment1, fragmentHeight_);
        rootLayoutSet_.constrainWidth(R.id.JournalFragment1, SCREEN_WIDTH);
        rootLayoutSet_.constrainHeight(R.id.JournalFragment2, fragmentHeight_);
        rootLayoutSet_.constrainWidth(R.id.JournalFragment2, SCREEN_WIDTH);

        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[0]).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[1]).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(currentOrder_[2]).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);

        rootLayoutSet_.constrainHeight(R.id.JournalButton, buttonHeight_);
        rootLayoutSet_.constrainHeight(R.id.ControlPanel, buttonHeight_);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, fragmentHeight_);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);

        alignFragments();
    }
}