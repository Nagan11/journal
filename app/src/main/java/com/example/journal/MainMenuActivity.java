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
    private final Context CONTEXT = this;
    private String ROOT_DIRECTORY;

    private int screenWidthPx;
    private int screenHeightPx;
    private int statusBarHeightPx = 0;
    private int fragmentHeightPx;
    private int buttonHeightPx;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<LinearLayout> scrollLayouts = new ArrayList<>();
    private ArrayList<TextView> statusTexts = new ArrayList<>();
    private ConstraintLayout rootLayout;
    private LinearLayout scrollLayoutYear;

    private ConstraintSet rootLayoutSet = new ConstraintSet();

    private WeekManager weekManager;
    private PageParser parser;

    private Pair<Integer, Integer>[] weekIndexes = new Pair[3]; // .first - quarter, .second - week

    private boolean journalClosed = true;
    private boolean lpClosed = true;

    private int weekShift = 0;



    private static class Order {
        private static int[] currentOrder = new int[] {0, 1, 2};

        public static int get(int index) {
            return currentOrder[index];
        }

        public static int left() {
            return currentOrder[0];
        }
        public static int center() {
            return currentOrder[1];
        }
        public static int right() {
            return currentOrder[2];
        }

        public static void rollLeft() {
            currentOrder = new int[] {currentOrder[2], currentOrder[0], currentOrder[1]};
        }
        public static void rollRight() {
            currentOrder = new int[] {currentOrder[1], currentOrder[2], currentOrder[0]};
        }

        public static void resetOrder() {
            currentOrder = new int[] {0, 1, 2};
        }
    }

    private class PageStateUpdaterR implements Runnable {
        private ArrayList< ArrayList<PageLoadState> > previousStates = new ArrayList<>(); // 1
        private ArrayList< ArrayList<PageLoadState> > currentStates = new ArrayList<>();  // 1
        private Pair<Integer, Integer>[] previousWeekIndexes;

        public PageStateUpdaterR() {
            previousStates.add(new ArrayList<PageLoadState>());
            currentStates.add(new ArrayList<PageLoadState>());
            for (int i = 1; i <= 4; i++) {
                previousStates.add(new ArrayList<PageLoadState>(YearData.getAmountOfWeeks(i)));
                currentStates.add(new ArrayList<PageLoadState>(YearData.getAmountOfWeeks(i)));
                for (int j = 0; j <= YearData.getAmountOfWeeks(i); j++) {
                    previousStates.get(i).add(null);
                    currentStates.get(i).add(null);
                }
            }
        }

        private void buildWeek(int index) {
            if (weekIndexes[index].first == -1 || weekIndexes[index].second == -1) return;
            ArrayList<ViewSets.Day> week = weekManager.weeks.get(weekIndexes[index].first - 1).get(weekIndexes[index].second - 1);
            for (int i = 0; i < week.size(); i++) {
                scrollLayouts.get(index).addView(week.get(i).getDate());
                for (ViewSets.Lesson l : week.get(i).getLessons()) {
                    scrollLayouts.get(index).addView(l.getLessonMarkContainer());
                    scrollLayouts.get(index).addView(l.getHometask());
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try { Thread.sleep(16); } catch (Exception e) { System.out.println(e); }

                previousWeekIndexes = weekIndexes.clone();
                if (weekShift != 0) {
                    while (weekShift > 0) {
                        if (!(weekIndexes[Order.center()].first == 4 && weekIndexes[Order.center()].second == YearData.getAmountOfWeeks(4))) {
                            Order.rollRight();
                            weekIndexes[Order.right()] = rightWeekIndex(weekIndexes[Order.center()]);
                        }
                        weekShift--;
                    }
                    while (weekShift < 0) {
                        if (!(weekIndexes[Order.center()].first == 1 && weekIndexes[Order.center()].second == 1)) {
                            Order.rollLeft();
                            weekIndexes[Order.left()] = leftWeekIndex(weekIndexes[Order.center()]);
                        }
                        weekShift++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alignFragments();
                        }
                    });
                }

                currentStates.get(weekIndexes[Order.center()].first)
                        .set(weekIndexes[Order.center()].second,
                                weekManager.getWeekState(weekIndexes[Order.center()].first, weekIndexes[Order.center()].second));
                if (weekIndexes[Order.left()].first > 0 && weekIndexes[Order.left()].second > 0) {
                    currentStates.get(weekIndexes[Order.left()].first)
                            .set(weekIndexes[Order.left()].second,
                                    weekManager.getWeekState(weekIndexes[Order.left()].first, weekIndexes[Order.left()].second));
                }
                if (weekIndexes[Order.right()].first > 0 && weekIndexes[Order.right()].second > 0) {
                    currentStates.get(weekIndexes[Order.right()].first)
                            .set(weekIndexes[Order.right()].second,
                                    weekManager.getWeekState(weekIndexes[Order.right()].first, weekIndexes[Order.right()].second));
                }

                for (int i = 0; i <= 2; i++) {
                    final int fI = Order.get(i);

                    if (weekIndexes[fI].first == -1 || weekIndexes[fI].second == -1) continue;

                    final boolean weekIndexesDiffer = !(weekIndexes[fI].first == previousWeekIndexes[fI].first &&
                            weekIndexes[fI].second == previousWeekIndexes[fI].second);
                    final boolean weekStatesDiffer = !currentStates.get(weekIndexes[fI].first).get(weekIndexes[fI].second)
                            .equals(previousStates.get(weekIndexes[fI].first).get(weekIndexes[fI].second));

                    if (weekIndexesDiffer || weekStatesDiffer) {
                        switch (currentStates.get(weekIndexes[fI].first).get(weekIndexes[fI].second)) {
                            case DOWNLOADING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setText("Downloading...");
                                        scrollLayouts.get(fI).removeAllViews();
                                        statusTexts.get(fI).setVisibility(View.VISIBLE);

                                        if (weekStatesDiffer) {
                                            Thread updater = new Thread(new UpdateWeekR(CONTEXT, ROOT_DIRECTORY,
                                                    weekIndexes[fI].first, weekIndexes[fI].second));
                                            updater.start();
                                        }
                                    }
                                });
                                break;
                            case DOWNLOADING_ERROR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setText("Downloading error");
                                        scrollLayouts.get(fI).removeAllViews();
                                        statusTexts.get(fI).setVisibility(View.VISIBLE);
                                    }
                                });
                                break;
                            case GATHERING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setText("Gathering info...");
                                        scrollLayouts.get(fI).removeAllViews();
                                        statusTexts.get(fI).setVisibility(View.VISIBLE);
                                    }
                                });
                                break;
                            case GATHERING_ERROR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setText("Gathering info error");
                                        scrollLayouts.get(fI).removeAllViews();
                                        statusTexts.get(fI).setVisibility(View.VISIBLE);
                                    }
                                });
                                break;
                            case BUILDING:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setText("Loading...");
                                        scrollLayouts.get(fI).removeAllViews();
                                        statusTexts.get(fI).setVisibility(View.VISIBLE);
                                    }
                                });
                                break;
                            case READY:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusTexts.get(fI).setVisibility(View.INVISIBLE);
                                        scrollLayouts.get(fI).removeAllViews();
                                        buildWeek(fI);
                                    }
                                });
                                break;
                        }
                        previousStates.get(weekIndexes[fI].first).set(weekIndexes[fI].second,
                                currentStates.get(weekIndexes[fI].first).get(weekIndexes[fI].second));
                    }
                }
            }
        }
    }

    public class UpdateWeekR implements Runnable {
        private final Context CONTEXT;
        private final String ROOT_DIRECTORY;

        private PageDownloader downloader;

        private int quarterNumber, weekNumber;
        private String pagePath;
        private String dataPath;

        UpdateWeekR(Context context, String rtDir, int quarter, int week) {
            CONTEXT = context;
            ROOT_DIRECTORY = rtDir;
            quarterNumber = quarter;
            weekNumber = week;

            downloader = new PageDownloader(ROOT_DIRECTORY, weekManager.getSessionid());

            pagePath = (ROOT_DIRECTORY + "/p" + quarterNumber + "q/w" + weekNumber + ".html");
            dataPath = (ROOT_DIRECTORY + "/d" + quarterNumber + "q/w" + weekNumber + ".txt");
        }

        @Override
        public void run() {
            if (quarterNumber == -1 || weekNumber == -1) return;

            if (downloader.downloadPage(quarterNumber, weekNumber, weekManager.getLink(quarterNumber, weekNumber))) {
                weekManager.setWeekState(quarterNumber, weekNumber, PageLoadState.GATHERING);
            } else {
                weekManager.setWeekState(quarterNumber, weekNumber, PageLoadState.DOWNLOADING_ERROR);
                return;
            }

            synchronized (parser) {
                parser.parsePage(pagePath, dataPath);
            }

            try {
                weekManager.readWeek(dataPath, quarterNumber, weekNumber);
            } catch (Exception e) {
                weekManager.setWeekState(quarterNumber, weekNumber, PageLoadState.GATHERING_ERROR);
                return;
            }
            weekManager.setWeekState(quarterNumber, weekNumber, PageLoadState.BUILDING);

            int startIndex = 0;
            weekManager.weeks.get(quarterNumber - 1).get(weekNumber - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager.getMaxLessons(quarterNumber, weekNumber) - 1,
                    YearData.getDates(quarterNumber, weekNumber).get(0),
                    weekManager.getLessonNames(quarterNumber, weekNumber),
                    weekManager.getMarks(quarterNumber, weekNumber),
                    weekManager.getHometasks(quarterNumber, weekNumber), false)
            );
            startIndex += weekManager.getMaxLessons(quarterNumber, weekNumber);
            weekManager.weeks.get(quarterNumber - 1).get(weekNumber - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager.getMaxLessons(quarterNumber, weekNumber) - 1,
                    YearData.getDates(quarterNumber, weekNumber).get(1),
                    weekManager.getLessonNames(quarterNumber, weekNumber),
                    weekManager.getMarks(quarterNumber, weekNumber),
                    weekManager.getHometasks(quarterNumber, weekNumber), false)
            );
            startIndex += weekManager.getMaxLessons(quarterNumber, weekNumber);
            weekManager.weeks.get(quarterNumber - 1).get(weekNumber - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager.getMaxLessons(quarterNumber, weekNumber) - 1,
                    YearData.getDates(quarterNumber, weekNumber).get(2),
                    weekManager.getLessonNames(quarterNumber, weekNumber),
                    weekManager.getMarks(quarterNumber, weekNumber),
                    weekManager.getHometasks(quarterNumber, weekNumber), false)
            );
            startIndex += weekManager.getMaxLessons(quarterNumber, weekNumber);
            weekManager.weeks.get(quarterNumber - 1).get(weekNumber - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager.getMaxLessons(quarterNumber, weekNumber) - 1,
                    YearData.getDates(quarterNumber, weekNumber).get(3),
                    weekManager.getLessonNames(quarterNumber, weekNumber),
                    weekManager.getMarks(quarterNumber, weekNumber),
                    weekManager.getHometasks(quarterNumber, weekNumber), false)
            );
            startIndex += weekManager.getMaxLessons(quarterNumber, weekNumber);
            weekManager.weeks.get(quarterNumber - 1).get(weekNumber - 1).add(new ViewSets.Day(
                    CONTEXT,
                    startIndex, startIndex + weekManager.getMaxLessons(quarterNumber, weekNumber) - 1,
                    YearData.getDates(quarterNumber, weekNumber).get(4),
                    weekManager.getLessonNames(quarterNumber, weekNumber),
                    weekManager.getMarks(quarterNumber, weekNumber),
                    weekManager.getHometasks(quarterNumber, weekNumber), true)
            );

            weekManager.setWeekState(quarterNumber, weekNumber, PageLoadState.READY);
        }
    }

    public class UpdateYearMarks implements Runnable {
        String link = weekManager.getLink(5, 0);
        String pagePath = ROOT_DIRECTORY + "/p4q/w111.html";
        String dataPath = ROOT_DIRECTORY + "/lp.txt";
        PageDownloader downloader = new PageDownloader(ROOT_DIRECTORY, weekManager.getSessionid());
        LastPageParser parser = new LastPageParser(ROOT_DIRECTORY);

        ArrayList<String> lessons = new ArrayList<>();
        ArrayList<ArrayList<String>> quarterMarks = new ArrayList<>();
        ArrayList<String> yearMarks = new ArrayList<>();
        ArrayList<ViewSets.YearLessonMarks> layouts = new ArrayList<>();

        UpdateYearMarks() {
            quarterMarks.add(new ArrayList<String>());
            quarterMarks.add(new ArrayList<String>());
            quarterMarks.add(new ArrayList<String>());
            quarterMarks.add(new ArrayList<String>());
        }

        public void run() {
            downloader.downloadPage(4, 111, link);
            parser.parsePage(pagePath, dataPath);

            try {
                FileReader fin = new FileReader(dataPath);
                int c = 0;
                String buf;

                while (c != -1) {
                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    lessons.add(buf);

                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    quarterMarks.get(0).add(buf);

                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    quarterMarks.get(1).add(buf);

                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    quarterMarks.get(2).add(buf);

                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    quarterMarks.get(3).add(buf);

                    buf = "";
                    while ((c = fin.read()) != '>' && c != -1) buf += (char)c;
                    yearMarks.add(buf);
                }
            } catch (Exception e) {}

            for (int i = 0; i < lessons.size() - 1; i++) {
                layouts.add(new ViewSets.YearLessonMarks(CONTEXT, lessons.get(i), yearMarks.get(i),
                        quarterMarks.get(0).get(i), quarterMarks.get(1).get(i),
                        quarterMarks.get(2).get(i), quarterMarks.get(3).get(i)));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < lessons.size() - 1; i++) {
                        scrollLayoutYear.addView(layouts.get(i).getLessonMark());
                        scrollLayoutYear.addView(layouts.get(i).getMarks());
                    }
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        rootLayout = findViewById(R.id.RootLayout);
        scrollLayoutYear = findViewById(R.id.ScrollLayoutYear);
        weekManager = new WeekManager(ROOT_DIRECTORY);
        parser = new PageParser(ROOT_DIRECTORY);
        fillFragmentArrays();
        Order.resetOrder();

        try {
            weekManager.readData();
        } catch (Exception e) {
            System.out.println("weekManager initialization failed, " + e);
        }
        weekManager.setLinks();

        weekIndexes[1] = new Pair<>(YearData.getCurrentQuarter(), YearData.getCurrentWeek());
        weekIndexes[0] = leftWeekIndex(weekIndexes[1]);
        weekIndexes[2] = rightWeekIndex(weekIndexes[1]);

        setScreenInfo();
        buttonHeightPx = (int)(screenHeightPx * 0.085f);
        fragmentHeightPx = screenHeightPx - buttonHeightPx - buttonHeightPx - statusBarHeightPx;

        setRootLayoutStartState();

        setRealName();

        Thread stateUpdater = new Thread(new PageStateUpdaterR());
        stateUpdater.start();

        Thread updateYearMarks = new Thread(new UpdateYearMarks());
        updateYearMarks.start();
    }

    @Override
    public void onBackPressed() {
        if (!journalClosed) {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 1f);
            rootLayoutSet.applyTo(rootLayout);
            journalClosed = true;
        }
        if (!lpClosed) {
            rootLayoutSet.setHorizontalBias(R.id.LastPageFragment, 0f);
            rootLayoutSet.applyTo(rootLayout);
            lpClosed = true;
        }
    }
    public void logOutButtonOnClick(View view) {
        cleanUserData();
        Intent logInActivity = new Intent(CONTEXT, LogInActivity.class);
        startActivity(logInActivity);
    }
    public void journalOnClick(View view) {
        if (journalClosed) {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 0f);
        } else {
            rootLayoutSet.setVerticalBias(R.id.JournalButton, 1f);
        }
        rootLayoutSet.applyTo(rootLayout);
        journalClosed = !journalClosed;
    }
    public void lpOnClick(View view) {
        rootLayoutSet.setHorizontalBias(R.id.LastPageFragment, 1f);
        rootLayoutSet.applyTo(rootLayout);
        lpClosed = false;
    }
    public void weekBackOnClick(View view) {
        scrollLeft();
    }
    public void weekForwardOnClick(View view) {
        scrollRight();
    }
    public void updateOnClick(View view) {
        PageLoadState state = weekManager.getWeekState(weekIndexes[Order.center()].first, weekIndexes[Order.center()].second);
        switch (state) {
            case DOWNLOADING_ERROR:
            case GATHERING_ERROR:
            case READY:
                weekManager.weeks.get(weekIndexes[Order.center()].first - 1).get(weekIndexes[Order.center()].second - 1).clear();
                weekManager.setWeekState(weekIndexes[Order.center()].first, weekIndexes[Order.center()].second, PageLoadState.DOWNLOADING);
                break;
            case DOWNLOADING:
            case GATHERING:
            case BUILDING:
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
        screenWidthPx = CONTEXT.getResources().getDisplayMetrics().widthPixels;
        screenHeightPx = CONTEXT.getResources().getDisplayMetrics().heightPixels;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeightPx = getResources().getDimensionPixelSize(resourceId);
        }
        if (statusBarHeightPx == 0) {
            statusBarHeightPx = Calculation.dpToPx(24, CONTEXT);
        }
    }
    private void fillFragmentArrays() {
        fragments.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment0));
        fragments.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment1));
        fragments.add(getSupportFragmentManager().findFragmentById(R.id.JournalFragment2));

        scrollLayouts.add((LinearLayout)findViewById(R.id.ScrollLayout0));
        scrollLayouts.add((LinearLayout)findViewById(R.id.ScrollLayout1));
        scrollLayouts.add((LinearLayout)findViewById(R.id.ScrollLayout2));

        statusTexts.add((TextView)findViewById(R.id.StatusText0));
        statusTexts.add((TextView)findViewById(R.id.StatusText1));
        statusTexts.add((TextView)findViewById(R.id.StatusText2));
    }

    private void alignFragments() {
        rootLayoutSet.clear(fragments.get(Order.left()).getId(), ConstraintSet.LEFT);
        rootLayoutSet.clear(fragments.get(Order.left()).getId(), ConstraintSet.RIGHT);
        rootLayoutSet.clear(fragments.get(Order.center()).getId(), ConstraintSet.LEFT);
        rootLayoutSet.clear(fragments.get(Order.center()).getId(), ConstraintSet.RIGHT);
        rootLayoutSet.clear(fragments.get(Order.right()).getId(), ConstraintSet.LEFT);
        rootLayoutSet.clear(fragments.get(Order.right()).getId(), ConstraintSet.RIGHT);

        rootLayoutSet.connect(fragments.get(Order.center()).getId(), ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet.connect(fragments.get(Order.center()).getId(), ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet.setHorizontalBias(fragments.get(Order.center()).getId(), 0.5f);
        rootLayoutSet.connect(fragments.get(Order.left()).getId(), ConstraintSet.RIGHT, fragments.get(Order.center()).getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet.connect(fragments.get(Order.left()).getId(), ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet.setHorizontalBias(fragments.get(Order.left()).getId(), 1f);
        rootLayoutSet.connect(fragments.get(Order.right()).getId(), ConstraintSet.LEFT, fragments.get(Order.center()).getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet.connect(fragments.get(Order.right()).getId(), ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
        rootLayoutSet.setHorizontalBias(fragments.get(Order.right()).getId(), 0f);

        rootLayoutSet.applyTo(rootLayout);
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
        weekShift--;
    }
    private void scrollRight() {
        weekShift++;
    }
    private void setRootLayoutStartState() {
        rootLayoutSet.clone(rootLayout);
        rootLayoutSet.clear(R.id.JournalFragment0);
        rootLayoutSet.clear(R.id.JournalFragment1);
        rootLayoutSet.clear(R.id.JournalFragment2);

        rootLayoutSet.setTranslationZ(rootLayout.getId(), 0f);
        rootLayoutSet.setTranslationZ(R.id.JournalButton, 100f);
        rootLayoutSet.setTranslationZ(R.id.ControlPanel, 100f);
        rootLayoutSet.setTranslationZ(R.id.JournalFragment0, 100f);
        rootLayoutSet.setTranslationZ(R.id.JournalFragment1, 100f);
        rootLayoutSet.setTranslationZ(R.id.JournalFragment2, 100f);
        rootLayoutSet.setTranslationZ(R.id.LastPageFragment, 150f);

        rootLayoutSet.constrainHeight(R.id.JournalFragment0, fragmentHeightPx);
        rootLayoutSet.constrainWidth(R.id.JournalFragment0, screenWidthPx);
        rootLayoutSet.constrainHeight(R.id.JournalFragment1, fragmentHeightPx);
        rootLayoutSet.constrainWidth(R.id.JournalFragment1, screenWidthPx);
        rootLayoutSet.constrainHeight(R.id.JournalFragment2, fragmentHeightPx);
        rootLayoutSet.constrainWidth(R.id.JournalFragment2, screenWidthPx);

        rootLayoutSet.constrainWidth(R.id.LastPageFragment, screenWidthPx);
        rootLayoutSet.connect(R.id.LastPageFragment, ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, screenWidthPx);
        rootLayoutSet.connect(R.id.LastPageFragment, ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP);
        rootLayoutSet.connect(R.id.LastPageFragment, ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT);
        rootLayoutSet.connect(R.id.LastPageFragment, ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM);
        rootLayoutSet.setHorizontalBias(R.id.LastPageFragment, 0f);

        rootLayoutSet.connect(fragments.get(Order.left()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet.connect(fragments.get(Order.center()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);
        rootLayoutSet.connect(fragments.get(Order.right()).getId(), ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, 0);

        rootLayoutSet.constrainHeight(R.id.JournalButton, buttonHeightPx);
        rootLayoutSet.constrainHeight(R.id.ControlPanel, buttonHeightPx);
        rootLayoutSet.connect(R.id.ControlPanel, ConstraintSet.TOP, R.id.JournalButton, ConstraintSet.BOTTOM, fragmentHeightPx);
        rootLayoutSet.connect(R.id.ControlPanel, ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
        rootLayoutSet.connect(R.id.ControlPanel, ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);

        alignFragments();
    }
}