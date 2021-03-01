package com.example.journal;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class WeekManager {
    private String ROOT_DIRECTORY;

    private ArrayList< ArrayList< ArrayList<String> > > lessonNames_ = new ArrayList<>(4);
    private ArrayList< ArrayList< ArrayList<String> > > marks_ = new ArrayList<>(4);
    private ArrayList< ArrayList< ArrayList<String> > > hometasks_ = new ArrayList<>(4);
    private ArrayList< ArrayList <PageLoadState> > weekStates_ = new ArrayList<>(4);
    private Integer[][] maxLessons_ = new Integer[][] {
            new Integer[YearData.getAmountOfWeeks(1)],
            new Integer[YearData.getAmountOfWeeks(2)],
            new Integer[YearData.getAmountOfWeeks(3)],
            new Integer[YearData.getAmountOfWeeks(4)]
    };

    public ArrayList< ArrayList < ArrayList<ViewSets.Day> > > weeks = new ArrayList<>(4);

    private enum ReadStage {
        LESSON,
        MARK,
        HOMETASK
    }

    WeekManager(String rtDir) {
        ROOT_DIRECTORY = rtDir;
        initializeArrayLists();
        checkFolders();
    }

    public void readWeek(String weekPath, int quarterNumber, int weekNumber) throws Exception {
        ReadStage stage = ReadStage.LESSON;
        FileReader fin = new FileReader(weekPath);
        int currentChar;
        String buf = "";

        if (maxLessons_[quarterNumber - 1][weekNumber - 1] == null) {
            int stagesCounter = 0;
            while ((currentChar = fin.read()) != -1) {
                if (currentChar == '>') {
                    stagesCounter++;
                    switch (stage) {
                        case LESSON:
                            stage = ReadStage.MARK;
                            lessonNames_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                        case MARK:
                            stage = ReadStage.HOMETASK;
                            marks_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                        case HOMETASK:
                            stage = ReadStage.LESSON;
                            hometasks_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                    }
                    buf = "";
                } else {
                    buf += (char)currentChar;
                }
            }
            maxLessons_[quarterNumber - 1][weekNumber - 1] = stagesCounter / 18;
        } else {
            while ((currentChar = fin.read()) != -1) {
                if (currentChar == '>') {
                    switch (stage) {
                        case LESSON:
                            stage = ReadStage.MARK;
                            lessonNames_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                        case MARK:
                            stage = ReadStage.HOMETASK;
                            marks_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                        case HOMETASK:
                            stage = ReadStage.LESSON;
                            hometasks_.get(quarterNumber - 1).get(weekNumber - 1).add(buf);
                            break;
                    }
                    buf = "";
                } else {
                    buf += (char) currentChar;
                }
            }
        }
        fin.close();
    } // 1

    public ArrayList<String> getLessonNames(int quarterNumber, int weekNumber) {
        return lessonNames_.get(quarterNumber - 1).get(weekNumber - 1);
    } // 1
    public ArrayList<String> getMarks(int quarterNumber, int weekNumber) {
        return marks_.get(quarterNumber - 1).get(weekNumber - 1);
    }       // 1
    public ArrayList<String> getHometasks(int quarterNumber, int weekNumber) {
        return hometasks_.get(quarterNumber - 1).get(weekNumber - 1);
    }   // 1
    public int               getMaxLessons(int quarterNumber, int weekNumber) {
        if (maxLessons_[quarterNumber - 1][weekNumber - 1] != null) {
            return maxLessons_[quarterNumber - 1][weekNumber - 1];
        }
        return -1;
    }  // 1

    public PageLoadState getWeekState(int quarterNumber, int weekNumber) {
        return weekStates_.get(quarterNumber - 1).get(weekNumber - 1);
    }
    public void setWeekState(int quarterNumber, int weekNumber, PageLoadState state) {
        weekStates_.get(quarterNumber - 1).set(weekNumber - 1, state);
    } // 1

    private void initializeArrayLists() {
        for (int i = 0; i < 4; i++) {
            lessonNames_.add(new ArrayList< ArrayList<String> >());
            marks_.add(new ArrayList< ArrayList<String> >());
            hometasks_.add(new ArrayList< ArrayList<String> >());
            weeks.add(new ArrayList<ArrayList<ViewSets.Day>>());
            weekStates_.add(new ArrayList<PageLoadState>());
            for (int j = 0; j < YearData.getAmountOfWeeks(i + 1); j++) {
                lessonNames_.get(i).add(new ArrayList<String>());
                marks_.get(i).add(new ArrayList<String>());
                hometasks_.get(i).add(new ArrayList<String>());
                weeks.get(i).add(new ArrayList<ViewSets.Day>());
                weekStates_.get(i).add(PageLoadState.DOWNLOADING);
            }
        }
    }
    private void checkFolders() {
        File p1qFolder = new File(ROOT_DIRECTORY, "p1q");
        File p2qFolder = new File(ROOT_DIRECTORY, "p2q");
        File p3qFolder = new File(ROOT_DIRECTORY, "p3q");
        File p4qFolder = new File(ROOT_DIRECTORY, "p4q");

        if (!p1qFolder.exists()) {
            p1qFolder.mkdir();
        }
        if (!p2qFolder.exists()) {
            p2qFolder.mkdir();
        }
        if (!p3qFolder.exists()) {
            p3qFolder.mkdir();
        }
        if (!p4qFolder.exists()) {
            p4qFolder.mkdir();
        }
    }

    private void cleanPagesFolder(int quarterNumber) { // from one
        File quarterFolder = new File(ROOT_DIRECTORY + "p" + quarterNumber + "q");
        if (quarterFolder.exists()) {
            File[] pages = quarterFolder.listFiles();
            for (File f : pages) {
                f.delete();
            }
        } else {
            quarterFolder.mkdir();
        }
    }
}