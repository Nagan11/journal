package com.example.journal;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class YearData {
    private static String sessionid_;
    private static String pupilUrl_;

    private static ArrayList<String> links_ = new ArrayList<>();
    private static int[] quarterIds_ = new int[] {40, 42, 43, 44};
    private static int[] amountsOfWeeks_ = new int[] {9, 7, 11, 9};
    private static int[][] firstMondays_ = new int[][] { // year, month, day
            {2020, 8, 31},
            {2020, 11, 9},
            {2021, 1, 11},
            {2021, 4, 5}
    };
    private static ArrayList< ArrayList< ArrayList<String> > > dates_ = new ArrayList<>(4);
    private static String[] monthNames_ = new String[] {
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
    private static String[] weekDayNames_ = new String[] {
            "Вс",
            "Пн",
            "Вт",
            "Ср",
            "Чт",
            "Пт",
            "Сб"
    };
    private static int[] daysInMonth_ = new int[13];

    private static int currentQuarter_ = -1;
    private static int currentWeek_ = -1;

    static {
        fillDaysInMonth();
        fillDates();
        defineQuarterAndWeek();
    }

    // getters
    public static int               getAmountOfWeeks(int quarterNumber) {
        return amountsOfWeeks_[quarterNumber - 1];
    }              // 1
    public static int               getCurrentQuarter() {
        return currentQuarter_;
    }
    public static int               getCurrentWeek() {
        return currentWeek_;
    }
    public static int               getQuarterId(int quarterNumber) {
        return quarterIds_[quarterNumber - 1];
    }                  // 1
    public static int[]             getFirstMonday(int quarterNumber) {
        return firstMondays_[quarterNumber - 1];
    }                // 1
    public static String            getSessionid() {
        return sessionid_;
    }
    public static String            getPupilUrl() {
        return pupilUrl_;
    }
    public static String            getLink(int quarterNumber, int weekNumber) {
        int linkIndex = weekNumber - 1;
        for (int i = 1; i < quarterNumber; i++) {
            linkIndex += amountsOfWeeks_[i - 1];
        }
        return links_.get(linkIndex);
    }       // 1
    public static String            getMonthName(int month) {
        return monthNames_[month];
    } // 0
    public static String            getWeekDayName(int weekDay) {
        return weekDayNames_[weekDay];
    }                      // Sun, 0
    public static ArrayList<String> getDates(int quarterNumber, int weekNumber) {
        return dates_.get(quarterNumber - 1).get(weekNumber - 1);
    }      // 1

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
    private static void fillDaysInMonth() {
        int secondYear = Calendar.getInstance().get(Calendar.YEAR);
        if (Calendar.getInstance().get(Calendar.MONTH) > 7) {
            secondYear++;
        }

        daysInMonth_[1] = 31;
        if (isLeap(secondYear)) {
            daysInMonth_[2] = 29;
        } else {
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
    private static void fillDates() {
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
    public static void setLinks() {
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
    private static void defineQuarterAndWeek() {
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
    public static void readData(String rootDirectory) throws Exception {
        int c;
        String buffer = "";

        FileReader inputSessionid = new FileReader(rootDirectory + "/UserData/sessionid.txt");
        while ((c = inputSessionid.read()) != -1) {
            buffer += (char)c;
        }
        inputSessionid.close();
        sessionid_ = buffer;
        buffer = "";

        FileReader inputPupilUrl = new FileReader(rootDirectory + "/UserData/pupilUrl.txt");
        while ((c = inputPupilUrl.read()) != -1) {
            buffer += (char)c;
        }
        inputPupilUrl.close();
        pupilUrl_ = buffer;
        pupilUrl_ += "/dnevnik/";
    }
}
