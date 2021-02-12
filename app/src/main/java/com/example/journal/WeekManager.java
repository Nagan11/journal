package com.example.journal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeekManager {
    private String USER_AGENT = "Mozilla/5.0";
    private String ROOT_DIRECTORY;

    private String csrftoken_;
    private String sessionid_;
    private String pupilUrl_;

    private CookieManager cookieManager = new CookieManager();

    private ArrayList<String> links_ = new ArrayList<>();
    private int[] quarterIds_ = new int[] {40, 42, 43, 44};
    private int[] amountsOfWeeks_ = new int[] {9, 7, 11, 9};
    private int[][] firstMondays_ = new int[][] {
            {2020, 8, 31},
            {2020, 11, 9},
            {2021, 1, 11},
            {2021, 4, 5}
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

    public PageParser pageParser;
    private enum ReadStage {
        LESSON,
        MARK,
        HOMETASK
    }

    WeekManager(String rtDir, String csrftoken) {
        ROOT_DIRECTORY = rtDir;

        fillDaysInMonth();
        initializeArrayLists();
        checkFolders();

        csrftoken_ = csrftoken;
        try {
            readData();
        } catch (Exception e) {
            System.out.println(e);
        }
        pupilUrl_ += "/dnevnik/";
        setLinks();

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        pageParser = new PageParser(ROOT_DIRECTORY);
    }

    private String getPageCode(String url) {
        try {
            if (csrftoken_ == null) {
                takeCsrftoken();
                while (csrftoken_ == null) {
                    Thread.sleep(25);
                }
                System.out.println("CSRF -> " + csrftoken_);
            }
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setInstanceFollowRedirects(false);
            con.setUseCaches(false);

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            String cookies = "csrftoken=" + csrftoken_ + "; sessionid=" + sessionid_;
            System.out.println("cookies -> " + cookies);
            con.setRequestProperty("cookie", cookies);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append("\n");
            }

            in.close();

            return response.toString();
        } catch (Exception e) {
            return "-";
        }
    }

    public boolean downloadPage(int quarterNumber, int weekNumber) { // from one
        int linkIndex = weekNumber - 1;
        for (int i = 1; i < quarterNumber; i++) {
            linkIndex += amountsOfWeeks_[i];
        }

        try {
            String pageCode = "-";
            int counter = 5;
            while (counter > 0 && pageCode.equals("-")) {
                pageCode = getPageCode(links_.get(linkIndex));
                counter--;
                System.out.println("NETWORK ERROR");
            }
            if (pageCode.equals("-")) {
                System.out.println("NETWORK ERROR (FINAL)");
                return false;
            }
            FileWriter fout = new FileWriter(
                    ROOT_DIRECTORY + "/p" + Integer.toString(quarterNumber) +
                    "q/w" + Integer.toString(weekNumber) + ".html"
            );
            fout.write(pageCode);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            System.out.print("EXCEPTION -> ");
            System.out.println(e);
            return false;
        }
        return true;
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
                    buf += (char)currentChar;
                }
            }
        }
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
    private void readData() throws Exception {
        String buffer = "";
        int c;

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
    private void setLinks() {
        links_.clear();
        int counter = 0;
        int quarterIdCounter = 0;
        String currentLink;
        int[] currentDate = new int[3];

        for (int i = 0; i < 4; i++) {
            currentDate[0] = firstMondays_[i][0];
            currentDate[1] = firstMondays_[i][1];
            currentDate[2] = firstMondays_[i][2];
            for (int week = 0; week < amountsOfWeeks_[i]; week++, counter++) {
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

                links_.add(counter, currentLink);

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
        links_.add(counter, lp);

        int temp = 0;
        for (int i = 0; i < 4; i++) {
            System.out.println(Integer.toString(i + 1) + " quarter");
            for (int j = 0; j < amountsOfWeeks_[i]; j++) {
                System.out.println(links_.get(temp + j));
            }
            System.out.print("\n");
            temp += amountsOfWeeks_[i];
        }
    }

    public void takeCsrftoken() throws Exception {
        // open connection
        URL connectionUrl = new URL(pupilUrl_);
        HttpURLConnection con = (HttpURLConnection)connectionUrl.openConnection();

        // set connection args
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // get cookies
        con.getContent();
        CookieStore cookieJar = cookieManager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();

        // find csrftoken in cookies and set it
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals("csrftoken")) {
                csrftoken_ = cookie.getValue();
                con.disconnect();
                cookieJar.removeAll();
                break;
            }
        }
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
}