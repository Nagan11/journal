package com.example.journal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class WeekManager {
    private String ROOT_DIRECTORY;

    private String sessionid_;
    private String pupilUrl_;

    private ArrayList<String> links_ = new ArrayList<>();
    private int[] quarterIds_ = new int[] {40, 42, 43, 44};
    private int[] amountsOfWeeks_ = new int[] {9, 7, 11, 9};
    private int[][] firstMondays_ = new int[][] { // year, month, date
            {2020, 8, 31},
            {2020, 11, 9},
            {2021, 1, 11},
            {2021, 4, 5}
    };

    private GregorianCalendar calendar_ = new GregorianCalendar();
    private int currentQuarter_ = -1;
    private int currentWeek_ = -1;

    private ArrayList< ArrayList< ArrayList<String> > > dates_ = new ArrayList<>(4);
    private String[] monthNames_ = new String[] {
            "января",
            "февраля",
            "марта",
            "апреля",
            "мая",
            "июня",
            "июля",
            "августа",
            "сентября",
            "октября",
            "ноября",
            "декабря"
    };
    private String[] weekDayNames_ = new String[] {
            "Вс",
            "Пн",
            "Вт",
            "Ср",
            "Чт",
            "Пт",
            "Сб"
    };
    private ArrayList< ArrayList< ArrayList<String> > > lessonNames_ = new ArrayList<>(4);
    private ArrayList< ArrayList< ArrayList<String> > > marks_ = new ArrayList<>(4);
    private ArrayList< ArrayList< ArrayList<String> > > hometasks_ = new ArrayList<>(4);
    private Integer[][] maxLessons_ = new Integer[][] {
            new Integer[amountsOfWeeks_[0]],
            new Integer[amountsOfWeeks_[1]],
            new Integer[amountsOfWeeks_[2]],
            new Integer[amountsOfWeeks_[3]]
    };

    private int[] daysInMonth_ = new int[13];

    private enum ReadStage {
        LESSON,
        MARK,
        HOMETASK
    }

    WeekManager(String rtDir, String csrftoken) {
        ROOT_DIRECTORY = rtDir;
        calendar_.set(calendar_.get(Calendar.YEAR), calendar_.get(Calendar.MONTH), calendar_.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        defineQuarterAndWeek();
        fillDaysInMonth();
        initializeArrayLists();
        fillDates();
        checkFolders();

        try {
            readData();
        } catch (Exception e) {
            System.out.println(e);
        }
        pupilUrl_ += "/dnevnik/";
        setLinks();
    }

    public String getLink(int quarterNumber, int weekNumber) {
        int linkIndex = weekNumber - 1;
        for (int i = 1; i < quarterNumber; i++) {
            linkIndex += amountsOfWeeks_[i - 1];
        }
        return links_.get(linkIndex);
    }
    public void readWeek(String weekPath, int quarterNumber, int weekNumber) throws Exception { // from one
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
    }

    public String            getSessionid() {
        return sessionid_;
    }
    public String            getPupilUrl() {
        return pupilUrl_;
    }
    public ArrayList<String> getDates(int quarterNumber, int weekNumber) {
        return dates_.get(quarterNumber - 1).get(weekNumber - 1);
    }
    public ArrayList<String> getLessonNames(int quarterNumber, int weekNumber) {
        return lessonNames_.get(quarterNumber - 1).get(weekNumber - 1);
    }
    public ArrayList<String> getMarks(int quarterNumber, int weekNumber) {
        return marks_.get(quarterNumber - 1).get(weekNumber - 1);
    }
    public ArrayList<String> getHometasks(int quarterNumber, int weekNumber) {
        return hometasks_.get(quarterNumber - 1).get(weekNumber - 1);
    }
    public int               getMaxLessons(int quarterNumber, int weekNumber) {
        if (maxLessons_[quarterNumber - 1][weekNumber - 1] != null) {
            return maxLessons_[quarterNumber - 1][weekNumber - 1];
        }
        return -1;
    }
    public int               getAmountOfWeeks(int quarterNumber) {
        return amountsOfWeeks_[quarterNumber - 1];
    }

    public int getCurrentQuarter() {
        return currentQuarter_;
    }
    public int getCurrentWeek() {
        return currentWeek_;
    }

    private void defineQuarterAndWeek() {
        GregorianCalendar tempCalendar = new GregorianCalendar();
        tempCalendar.set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (tempCalendar.get(Calendar.YEAR) < firstMondays_[0][0] || tempCalendar.get(Calendar.YEAR) > firstMondays_[3][0]) {
            currentQuarter_ = -1;
            currentWeek_ = -1;
        } else if (tempCalendar.get(Calendar.YEAR) == firstMondays_[0][0]) {
            if (tempCalendar.get(Calendar.MONTH) < firstMondays_[0][1] - 1) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[0][1] - 1) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > firstMondays_[0][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[0][1] - 1 && tempCalendar.get(Calendar.MONTH) < firstMondays_[1][1] - 1 ) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[0]) {
                    currentQuarter_ = 2;
                    currentWeek_ = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[1][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < firstMondays_[1][2] - 1) {
                    currentQuarter_ = 1;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                    if (currentWeek_ > amountsOfWeeks_[0]) {
                        currentQuarter_ = 2;
                        currentWeek_ = 1;
                    }
                } else {
                    currentQuarter_ = 2;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[1][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[1][1] - 1) {
                currentQuarter_ = 2;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[1][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[1]) {
                    currentQuarter_ = 3;
                    currentWeek_ = 1;
                }
            }
        } else {
            if (tempCalendar.get(Calendar.MONTH) < firstMondays_[2][1] - 1) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[2][1] - 1) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > firstMondays_[2][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[2][1] - 1 && tempCalendar.get(Calendar.MONTH) < firstMondays_[3][1] - 1 ) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[2]) {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[3][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < firstMondays_[3][2] - 1) {
                    currentQuarter_ = 1;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                    if (currentWeek_ > amountsOfWeeks_[2]) {
                        currentQuarter_ = 4;
                        currentWeek_ = 1;
                    }
                } else {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[3][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[3][1] - 1) {
                currentQuarter_ = 4;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[3][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[3]) {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                }
            }
        }
    }
    private void fillDaysInMonth() {
        int secondYear = Calendar.getInstance().get(Calendar.YEAR);
        if (Calendar.getInstance().get(Calendar.MONTH) > 7) {
            secondYear++;
        }

        daysInMonth_[1] = 31;
        if (isLeap(secondYear)) {
            daysInMonth_[2] = 29;
        }
        else {
            daysInMonth_[2] = 28;
        }
        daysInMonth_[3] = 31;
        daysInMonth_[4] = 30;
        daysInMonth_[5] = 31;
        daysInMonth_[6] = 30;
        daysInMonth_[7] = 31;
        daysInMonth_[8] = 31;
        daysInMonth_[9] = 30;
        daysInMonth_[10] = 31;
        daysInMonth_[11] = 30;
        daysInMonth_[12] = 31;
    }
    private static boolean isLeap(int a) {
        if (a % 400 == 0) {
            return true;
        }
        else if (a % 400 != 0 && a % 100 == 0) {
            return false;
        }
        else if (a % 400 != 0 && a % 100 != 0 && a % 4 == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    private void initializeArrayLists() {
        for (int i = 0; i < 4; i++) {
            lessonNames_.add(new ArrayList< ArrayList<String> >());
            marks_.add(new ArrayList< ArrayList<String> >());
            hometasks_.add(new ArrayList< ArrayList<String> >());
            for (int j = 0; j < amountsOfWeeks_[i]; j++) {
                lessonNames_.get(i).add(new ArrayList<String>());
                marks_.get(i).add(new ArrayList<String>());
                hometasks_.get(i).add(new ArrayList<String>());
            }
        }
    }
    private void fillDates() {
        GregorianCalendar cal;
        for (int i = 0; i < 4; i++) {
            cal = new GregorianCalendar(firstMondays_[i][0], firstMondays_[i][1] - 1, firstMondays_[i][2]);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            dates_.add(new ArrayList<ArrayList<String>>());
            for (int j = 0; j < amountsOfWeeks_[i]; j++) {
                dates_.get(i).add(new ArrayList<String>());
                for (int k = 0; k < 7; k++) {
                    String currentDate = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    currentDate += " ";
                    currentDate += monthNames_[cal.get(Calendar.MONTH)];
                    currentDate += ", ";
                    currentDate += Integer.toString(cal.get(Calendar.YEAR));
                    currentDate += " (";
                    currentDate += weekDayNames_[cal.get(Calendar.DAY_OF_WEEK) - 1];
                    currentDate += ")";
                    dates_.get(i).get(j).add(currentDate);
                    cal.roll(Calendar.DAY_OF_WEEK, true);
                }
                cal.roll(Calendar.WEEK_OF_YEAR, true);
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
    private void setLinks() {
        links_.clear();
        int quarterIdCounter = 0;
        String currentLink;
        int[] currentDate = new int[3];

        for (int i = 0; i < 4; i++) {
            currentDate[0] = firstMondays_[i][0];
            currentDate[1] = firstMondays_[i][1];
            currentDate[2] = firstMondays_[i][2];
            for (int week = 0; week < amountsOfWeeks_[i]; week++) {
                currentLink = pupilUrl_;

                currentLink += "quarter/";
                currentLink += Integer.toString(quarterIds_[quarterIdCounter]);

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
                if (currentDate[2] > daysInMonth_[currentDate[1]]) {
                    currentDate[2] -= daysInMonth_[currentDate[1]];
                    currentDate[1]++;
                }
            }
            quarterIdCounter++;
        }

        String lp = pupilUrl_;
        lp += "last-page";
        links_.add(lp);
    }

    private void cleanPagesFolder(int quarterNumber) { // from one
        File quarterFolder = new File(ROOT_DIRECTORY + "p" + Integer.toString(quarterNumber) + "q");
        if (quarterFolder.exists()) {
            File[] pages = quarterFolder.listFiles();
            for (File f : pages) {
                f.delete();
            }
        } else {
            quarterFolder.mkdir();
        }
    }
    private void readData() throws Exception {
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
    }
}