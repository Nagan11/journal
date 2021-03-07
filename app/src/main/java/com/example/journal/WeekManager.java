package com.example.journal;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class WeekManager {
    private String ROOT_DIRECTORY;

    private String sessionid_;
    private String pupilUrl_;

    private ArrayList<String> links_ = new ArrayList<>();

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

    WeekManager(String rootDirectory) {
        ROOT_DIRECTORY = rootDirectory;
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
    }             // 1
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

    private void cleanPagesFolder(int quarterNumber) {
        File quarterFolder = new File(ROOT_DIRECTORY + "p" + quarterNumber + "q");
        if (quarterFolder.exists()) {
            File[] pages = quarterFolder.listFiles();
            for (File f : pages) {
                f.delete();
            }
        } else {
            quarterFolder.mkdir();
        }
    } // 1

    public void setLinks() {
        links_.clear();
        int quarterIdCounter = 1;
        String currentLink;
        int[] currentDate;

        for (int i = 0; i < 4; i++) {
            currentDate = YearData.getFirstMonday(i + 1).clone();
            for (int week = 0; week < YearData.getAmountOfWeeks(i + 1); week++) {
                currentLink = pupilUrl_;

                currentLink += "quarter/";
                currentLink += Integer.toString(YearData.getQuarterId(quarterIdCounter));

                currentLink += "/week/";
                currentLink += Integer.toString(currentDate[0]);
                currentLink += "-";
                if (currentDate[1] < 10) {
                    currentLink += "0";
                }
                currentLink += Integer.toString(currentDate[1]);
                currentLink += "-";
                if(currentDate[2] < 10) {
                    currentLink += "0";
                }
                currentLink += Integer.toString(currentDate[2]);

                links_.add(currentLink);

                currentDate[2] += 7;
                if (currentDate[2] > YearData.getDaysInMonth(currentDate[1])) {
                    currentDate[2] -= YearData.getDaysInMonth(currentDate[1]);
                    currentDate[1]++;
                }
            }
            quarterIdCounter++;
        }

        String lp = pupilUrl_;
        lp += "last-page";
        links_.add(lp);
    }
    public String getLink(int quarterNumber, int weekNumber) {
        int linkIndex = weekNumber - 1;
        for (int i = 1; i < quarterNumber; i++) {
            linkIndex += YearData.getAmountOfWeeks(i);
        }
        return links_.get(linkIndex);
    }

    public void readData() throws Exception {
        int c;
        String buffer = "";

        FileReader inputSessionid = new FileReader(ROOT_DIRECTORY + "/UserData/sessionid.txt");
        while ((c = inputSessionid.read()) != -1) {
            buffer += (char)c;
        }
        inputSessionid.close();
        sessionid_ = buffer;
        buffer = "";

        FileReader inputPupilUrl = new FileReader(ROOT_DIRECTORY + "/UserData/pupilUrl.txt");
        while ((c = inputPupilUrl.read()) != -1) {
            buffer += (char)c;
        }
        inputPupilUrl.close();
        pupilUrl_ = buffer;
        pupilUrl_ += "/dnevnik/";
    }
    public String getSessionid() {
        return sessionid_;
    }
}