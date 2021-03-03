package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;



public class MainMenuActivity extends AppCompatActivity {
    private Context CONTEXT = this;
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

    private WeekManager weekManager_;
    private PageParser parser_;

    private Pair<Integer, Integer>[] weekIndexes_ = new Pair[3]; // .first - quarter, .second - week

    private boolean journalClosed_ = true;

    private int weekShift_ = 0;



    private static class Order {
        private static int[] currentOrder_ = new int[] {0, 1, 2};

        public static int get(int index) {
            return currentOrder_[index];
        }

        public static int left() {
            return currentOrder_[0];
        }
        public static int center() {
            return currentOrder_[1];
        }
        public static int right() {
            return currentOrder_[2];
        }

        public static void rollLeft() {
            currentOrder_ = new int[] {currentOrder_[2], currentOrder_[0], currentOrder_[1]};
        }
        public static void rollRight() {
            currentOrder_ = new int[] {currentOrder_[1], currentOrder_[2], currentOrder_[0]};
        }

        public static void resetOrder() {
            currentOrder_ = new int[] {0, 1, 2};
        }
    }

    private class PageStateUpdaterR implements Runnable {
        private PageLoadState[] previousStates = new PageLoadState[3];
        private PageLoadState[] currentStates = new PageLoadState[3];

        private Pair<Integer, Integer>[] previousWeekIndexes = weekIndexes_.clone();

        private void buildWeek(int index) {
            if (weekIndexes_[index].first == -1 || weekIndexes_[index].second == -1) return;
            ArrayList<ViewSets.Day> week = weekManager_.weeks.get(weekIndexes_[index].first - 1).get(weekIndexes_[index].second - 1);
            for (int i = 0; i < week.size(); i++) {
                SCROLL_LAYOUTS.get(index).addView(week.get(i).getDate());
                for (ViewSets.Lesson l : week.get(i).getLessons()) {
                    SCROLL_LAYOUTS.get(index).addView(l.getLessonMarkContainer());
                    SCROLL_LAYOUTS.get(index).addView(l.getHometask());
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    System.out.println(e);
                }

                if (weekShift_ != 0) {
                    while (weekShift_ > 0) {
                        if (!(weekIndexes_[Order.center()].first == 4 && weekIndexes_[Order.center()].second == YearData.getAmountOfWeeks(4))) {
                            Order.rollRight();

                            if (weekIndexes_[Order.right()].first != -1 && weekIndexes_[Order.right()].second != -1) {
                                if (weekManager_.getWeekState(weekIndexes_[Order.right()].first, weekIndexes_[Order.right()].second) == PageLoadState.ACTIVE) {
                                    weekManager_.setWeekState(weekIndexes_[Order.right()].first, weekIndexes_[Order.right()].second, PageLoadState.INACTIVE);
                                }
                            }
                            weekIndexes_[Order.right()] = rightWeekIndex(weekIndexes_[Order.center()]);
                        }
                        weekShift_--;
                    }
                    while (weekShift_ < 0) {
                        if (!(weekIndexes_[Order.center()].first == 1 && weekIndexes_[Order.center()].second == 1)) {
                            Order.rollLeft();

                            if (weekIndexes_[Order.left()].first != -1 && weekIndexes_[Order.left()].second != -1) {
                                if (weekManager_.getWeekState(weekIndexes_[Order.left()].first, weekIndexes_[Order.left()].second) == PageLoadState.ACTIVE) {
                                    weekManager_.setWeekState(weekIndexes_[Order.left()].first, weekIndexes_[Order.left()].second, PageLoadState.INACTIVE);
                                }
                            }
                            weekIndexes_[Order.left()] = leftWeekIndex(weekIndexes_[Order.center()]);
                        }
                        weekShift_++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alignFragments();
                        }
                    });
                }

                currentStates[Order.center()] = weekManager_.getWeekState(weekIndexes_[Order.center()].first, weekIndexes_[Order.center()].second);
                if (weekIndexes_[Order.left()].first > 0 && weekIndexes_[Order.left()].second > 0) {
                    currentStates[Order.left()] = weekManager_.getWeekState(weekIndexes_[Order.left()].first, weekIndexes_[Order.left()].second);
                }
                if (weekIndexes_[Order.right()].first > 0 && weekIndexes_[Order.right()].second > 0) {
                    currentStates[Order.right()] = weekManager_.getWeekState(weekIndexes_[Order.right()].first, weekIndexes_[Order.right()].second);
                }

                for (int i = 0; i <= 2; i++) {
                    final int fI = Order.get(i);
                    if (currentStates[fI] != previousStates[fI] || weekIndexes_[fI].first != previousWeekIndexes[fI].first || weekIndexes_[fI].second != previousWeekIndexes[fI].second) {
                        switch (currentStates[fI]) {
                            case DOWNLOADING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SCROLL_LAYOUTS.get(fI).removeAllViews();
                                        STATUS_TEXTS.get(fI).setText("Downloading...");
                                        STATUS_TEXTS.get(fI).setVisibility(View.VISIBLE);
                                        Thread updater = new Thread(new UpdateWeekR(CONTEXT, ROOT_DIRECTORY,
                                                weekIndexes_[fI].first, weekIndexes_[fI].second));
                                        updater.start();
                                    }
                                });
                                break;
                            case DOWNLOADING_ERROR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        STATUS_TEXTS.get(fI).setText("Downloading error");
                                    }
                                });
                                break;
                            case GATHERING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        STATUS_TEXTS.get(fI).setText("Gathering info...");
                                    }
                                });
                                break;
                            case GATHERING_ERROR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        STATUS_TEXTS.get(fI).setText("Gathering info error");
                                    }
                                });
                                break;
                            case BUILDING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        STATUS_TEXTS.get(fI).setText("Loading...");
                                    }
                                });
                                break;
                            case INACTIVE:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SCROLL_LAYOUTS.get(fI).removeAllViews();
                                        STATUS_TEXTS.get(fI).setText("Loading...");
                                    }
                                });
                                weekManager_.setWeekState(weekIndexes_[fI].first, weekIndexes_[fI].second, PageLoadState.ACTIVE);
                                break;
                            case ACTIVE:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println(weekIndexes_[fI].first + "/" + weekIndexes_[fI].second);
                                        STATUS_TEXTS.get(fI).setVisibility(View.INVISIBLE);
                                        buildWeek(fI);
                                    }
                                });
                                break;
                        }
                    }
                }
                previousStates = currentStates.clone();
                previousWeekIndexes = weekIndexes_.clone();
            }
        }
    }

    public class UpdateWeekR implements Runnable {
        private Context CONTEXT;
        private String ROOT_DIRECTORY;

        private PageDownloader downloader_;

        private int quarterNumber_, weekNumber_;
        private String pagePath_;
        private String dataPath_;

        UpdateWeekR(Context context, String rtDir, int quarter, int week) {
            CONTEXT = context;
            ROOT_DIRECTORY = rtDir;
            quarterNumber_ = quarter;
            weekNumber_ = week;

            downloader_ = new PageDownloader(ROOT_DIRECTORY);

            pagePath_ = (ROOT_DIRECTORY + "/p" + quarterNumber_ + "q/w" + weekNumber_ + ".html");
            dataPath_ = (ROOT_DIRECTORY + "/d" + quarterNumber_ + "q/w" + weekNumber_ + ".txt");
        }

        @Override
        public void run() {
            System.out.println(quarterNumber_ + "/" + weekNumber_ + " started");
            if (quarterNumber_ == -1 || weekNumber_ == -1) return;
            if (downloader_.downloadPage(quarterNumber_, weekNumber_, YearData.getLink(quarterNumber_, weekNumber_))) {
                weekManager_.setWeekState(quarterNumber_, weekNumber_, PageLoadState.GATHERING);
            } else {
                weekManager_.setWeekState(quarterNumber_, weekNumber_, PageLoadState.DOWNLOADING_ERROR);
                return;
            }

            synchronized (parser_) {
                parser_.parsePage(pagePath_, dataPath_);
            }

            try {
                weekManager_.readWeek(dataPath_, quarterNumber_, weekNumber_);
            } catch (Exception e) {
                weekManager_.setWeekState(quarterNumber_, weekNumber_, PageLoadState.GATHERING_ERROR);
                return;
            }
            weekManager_.setWeekState(quarterNumber_, weekNumber_, PageLoadState.BUILDING);

            int startIndex = 0;
            weekManager_.weeks.get(quarterNumber_ - 1).get(weekNumber_ - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    YearData.getDates(quarterNumber_, weekNumber_).get(0),
                    weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_),
                    weekManager_.getHometasks(quarterNumber_, weekNumber_), false)
            );
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            weekManager_.weeks.get(quarterNumber_ - 1).get(weekNumber_ - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    YearData.getDates(quarterNumber_, weekNumber_).get(1),
                    weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_),
                    weekManager_.getHometasks(quarterNumber_, weekNumber_), false)
            );
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            weekManager_.weeks.get(quarterNumber_ - 1).get(weekNumber_ - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    YearData.getDates(quarterNumber_, weekNumber_).get(2),
                    weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_),
                    weekManager_.getHometasks(quarterNumber_, weekNumber_), false)
            );
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            weekManager_.weeks.get(quarterNumber_ - 1).get(weekNumber_ - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    YearData.getDates(quarterNumber_, weekNumber_).get(3),
                    weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_),
                    weekManager_.getHometasks(quarterNumber_, weekNumber_), false)
            );
            startIndex += weekManager_.getMaxLessons(quarterNumber_, weekNumber_);
            weekManager_.weeks.get(quarterNumber_ - 1).get(weekNumber_ - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager_.getMaxLessons(quarterNumber_, weekNumber_) - 1,
                    YearData.getDates(quarterNumber_, weekNumber_).get(4),
                    weekManager_.getLessonNames(quarterNumber_, weekNumber_),
                    weekManager_.getMarks(quarterNumber_, weekNumber_),
                    weekManager_.getHometasks(quarterNumber_, weekNumber_), true)
            );

            weekManager_.setWeekState(quarterNumber_, weekNumber_, PageLoadState.INACTIVE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        ROOT_LAYOUT = findViewById(R.id.RootLayout);
        weekManager_ = new WeekManager(ROOT_DIRECTORY);
        parser_ = new PageParser(ROOT_DIRECTORY);
        fillFragmentArrays();
        Order.resetOrder();

        try {
            YearData.readData(ROOT_DIRECTORY);
            YearData.setLinks();
        } catch (Exception e) {
            System.out.println("YD initialization failed, " + e);
        }

        weekIndexes_[1] = new Pair<>(YearData.getCurrentQuarter(), YearData.getCurrentWeek());
        weekIndexes_[0] = leftWeekIndex(weekIndexes_[1]);
        weekIndexes_[2] = rightWeekIndex(weekIndexes_[1]);

        System.out.println("current Q/W -> " + weekIndexes_[Order.center()].first + "/" + weekIndexes_[Order.center()].second);
        System.out.println("weekShift -> " + weekShift_);

        setScreenInfo();
        buttonHeight_ = (int)(SCREEN_HEIGHT * 0.085f);
        fragmentHeight_ = SCREEN_HEIGHT - buttonHeight_ - buttonHeight_ - STATUS_BAR_HEIGHT;

        setRootLayoutStartState();

        setRealName();

        Thread stateUpdater = new Thread(new PageStateUpdaterR());
        stateUpdater.start();
    }

    @Override
    public void onBackPressed() {
        if (!journalClosed_) {
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 1f);
            rootLayoutSet_.applyTo(ROOT_LAYOUT);
            journalClosed_ = !journalClosed_;
        }
    }
    public void logOutButtonOnClick(View view) {
        cleanUserData();
        Intent logInActivity = new Intent(CONTEXT, LogInActivity.class);
        startActivity(logInActivity);
    }
    public void journalButtonOnClick(View view) {
        if (journalClosed_) {
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 0f);
        } else {
            rootLayoutSet_.setVerticalBias(R.id.JournalButton, 1f);
        }
        rootLayoutSet_.applyTo(ROOT_LAYOUT);
        journalClosed_ = !journalClosed_;
    }
    public void leftArrowOnClick(View view) {
        scrollLeft();
    }
    public void rightArrowOnClick(View view) {
        scrollRight();
    }
    public void updateOnClick(View view) {
        PageLoadState state = weekManager_.getWeekState(weekIndexes_[Order.center()].first, weekIndexes_[Order.center()].second);
        switch (state) {
            case DOWNLOADING_ERROR:
            case GATHERING_ERROR:
            case ACTIVE:
                weekManager_.weeks.get(weekIndexes_[Order.center()].first - 1).get(weekIndexes_[Order.center()].second - 1).clear();
                weekManager_.setWeekState(weekIndexes_[Order.center()].first, weekIndexes_[Order.center()].second, PageLoadState.DOWNLOADING);
                break;
            case DOWNLOADING:
            case GATHERING:
            case BUILDING:
            case INACTIVE:
                break;
        }
    }

    private void setUserName() {
        String buffer = "";
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/username.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = findViewById(R.id.UserNameTextView);
        userNameTextView.setText(buffer);
    }
    private void setRealName() {
        String buffer = "";
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/realName.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = findViewById(R.id.UserNameTextView);
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
        SCREEN_WIDTH = CONTEXT.getResources().getDisplayMetrics().widthPixels;
        SCREEN_HEIGHT = CONTEXT.getResources().getDisplayMetrics().heightPixels;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            STATUS_BAR_HEIGHT = getResources().getDimensionPixelSize(resourceId);
        }
        if (STATUS_BAR_HEIGHT == 0) {
            STATUS_BAR_HEIGHT = Calculation.dpToPx(24, CONTEXT);
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
        rootLayoutSet_.clear(FRAGMENTS.get(Order.left()).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(Order.left()).getId(), ConstraintSet.RIGHT);
        rootLayoutSet_.clear(FRAGMENTS.get(Order.center()).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(Order.center()).getId(), ConstraintSet.RIGHT);
        rootLayoutSet_.clear(FRAGMENTS.get(Order.right()).getId(), ConstraintSet.LEFT);
        rootLayoutSet_.clear(FRAGMENTS.get(Order.right()).getId(), ConstraintSet.RIGHT);

        rootLayoutSet_.connect(FRAGMENTS.get(Order.center()).getId(), ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.center()).getId(), ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(Order.center()).getId(), 0.5f);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.left()).getId(), ConstraintSet.RIGHT, FRAGMENTS.get(Order.center()).getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.left()).getId(), ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(Order.left()).getId(), 1f);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.right()).getId(), ConstraintSet.LEFT, FRAGMENTS.get(Order.center()).getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.right()).getId(), ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet_.setHorizontalBias(FRAGMENTS.get(Order.right()).getId(), 0f);

        rootLayoutSet_.applyTo(ROOT_LAYOUT);
    }
    private Pair<Integer, Integer> leftWeekIndex(Pair<Integer, Integer> p) {
        int quarter = p.first;
        int week = p.second - 1;
        if (week < 1) {
            quarter--;
            if (quarter < 1) {
                quarter = -1;
                week = -1;
            } else {
                week = YearData.getAmountOfWeeks(quarter);
            }
        }
        return new Pair<>(quarter, week);
    }
    private Pair<Integer, Integer> rightWeekIndex(Pair<Integer, Integer> p) {
        int quarter = p.first;
        int week = p.second + 1;
        if (week > YearData.getAmountOfWeeks(quarter)) {
            quarter++;
            if (quarter > 4) {
                quarter = -1;
                week = -1;
            } else {
                week = 1;
            }
        }
        return new Pair<>(quarter, week);
    }
    private void scrollLeft() {
        weekShift_--;
    }
    private void scrollRight() {
        weekShift_++;
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

        rootLayoutSet_.connect(FRAGMENTS.get(Order.left()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.center()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet_.connect(FRAGMENTS.get(Order.right()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);

        rootLayoutSet_.constrainHeight(R.id.JournalButton, buttonHeight_);
        rootLayoutSet_.constrainHeight(R.id.ControlPanel, buttonHeight_);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, fragmentHeight_);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.LEFT, ROOT_LAYOUT.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet_.connect(R.id.ControlPanel, ConstraintSet.RIGHT, ROOT_LAYOUT.getId(), ConstraintSet.RIGHT, 0);

        alignFragments();
    }
}