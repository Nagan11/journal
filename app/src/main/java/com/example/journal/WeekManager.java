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
    private int[][] firstMondays_ = new int[][] {{2020, 8, 31}, {2020, 11, 9}, {2021, 1, 11}, {2021, 4, 5}};

    private int[] daysInMonth_ = new int[13];

    private PageParser pageParser_;

    WeekManager(String rtDir, String csrftoken) {
        ROOT_DIRECTORY = rtDir;
        fillDaysInMonth();
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

        pageParser_ = new PageParser(ROOT_DIRECTORY);

//        System.out.println("URL -> " + pupilUrl_);
//        System.out.println("Sessionid -> " + sessionid_);
//        System.out.println("csrftoken -> " + csrftoken_);
    }

    class UpdatePage implements Runnable {
        private int quarterNumber_;
        private int weekInQuarter_;
        private int weekInYear_;

        private String pagePath_;
        private String dataPath_;

        UpdatePage(int quarterNumber, int weekInQuarter) {
            quarterNumber_ = quarterNumber;
            weekInQuarter_ = weekInQuarter;

            setWeekInYear();
            setPaths();
        }

        UpdatePage(int quarterNumber, int weekInQuarter, int weekInYear) {
            quarterNumber_ = quarterNumber;
            weekInQuarter_ = weekInQuarter;
            weekInYear_ = weekInYear;

            setPaths();
        }

        @Override
        public void run() {
            try {
                FileWriter fout = new FileWriter(pagePath_, false);
                fout.write(getPageCode(links_.get(weekInYear_)));
                fout.flush();
                fout.close();
            } catch (Exception e) {
                // I'll definitely handle it later
            }

            pageParser_.parsePage(pagePath_, dataPath_);
        }

        private void setWeekInYear() {
            weekInYear_ = 0;
            for (int i = 0; i <= quarterNumber_; i++) {
                weekInYear_ += amountsOfWeeks_[i];
            }
            weekInYear_ += weekInQuarter_;
        }

        private void setPaths() {
            pagePath_ = ROOT_DIRECTORY;
            pagePath_ += "/p" + Integer.toString(quarterNumber_ + 1) + "q/w";
            pagePath_ += Integer.toString(weekInQuarter_ + 1);
            pagePath_ += ".html";

            dataPath_ = ROOT_DIRECTORY;
            dataPath_ += "/d" + Integer.toString(quarterNumber_ + 1) + "q/w";
            dataPath_ += Integer.toString(weekInQuarter_ + 1);
            dataPath_ += ".txt";
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
        String buffer = new String("");
        int c;

        FileReader inputSessionid = new FileReader(ROOT_DIRECTORY + "/UserData/sessionid.txt");
        while ((c = inputSessionid.read()) != -1) {
            buffer += (char)c;
        }
        inputSessionid.close();
        sessionid_ = new String(buffer);
        buffer = "";

        FileReader inputPupilUrl = new FileReader(ROOT_DIRECTORY + "/UserData/pupilUrl.txt");
        while ((c = inputPupilUrl.read()) != -1) {
            buffer += (char)c;
        }
        inputPupilUrl.close();
        pupilUrl_ = new String(buffer);
        buffer = "";
    }

    public void updateQuarter(int quarterNumber) throws Exception { // from zero
        System.out.println(quarterNumber);
        cleanPagesFolder(quarterNumber);
        int linkSum = 0;
        linkSum = 0;
        for (int i = 0; i <= quarterNumber; i++) {
            linkSum += amountsOfWeeks_[i];
        }

        for (int i = 0; i < amountsOfWeeks_[quarterNumber]; i++) {
            String fileName = ROOT_DIRECTORY;
            fileName += "/p" + Integer.toString(quarterNumber + 1) + "q/w";
            fileName += Integer.toString(i + 1);
            fileName += ".html";

            FileWriter fout = new FileWriter(fileName, false);
            fout.write(getPageCode(links_.get(linkSum + i)));
            fout.flush();
            fout.close();
            System.out.println(fileName);
        }
    }

    private void cleanPagesFolder(int quarterNumber) {
        File quarterFolder = new File(ROOT_DIRECTORY + "p" + Integer.toString(quarterNumber + 1) + "q");
        if (quarterFolder.exists()) {
            File[] pages = quarterFolder.listFiles();
            for (File f : pages) {
                f.delete();
            }
        } else {
            quarterFolder.mkdir();
        }
    }

    private ArrayList<String> getQuarterLinks(int quarterNumber) {
        ArrayList<String> links = new ArrayList<>();
        String currentLink = new String("");
        int[] currentDate = new int[3];

        currentDate[0] = firstMondays_[quarterNumber][0];
        currentDate[1] = firstMondays_[quarterNumber][1];
        currentDate[2] = firstMondays_[quarterNumber][2];

        for (int week = 0; week < amountsOfWeeks_[quarterNumber]; week++) {
            currentLink = pupilUrl_;

            currentLink += "quarter/";
            currentLink += Integer.toString(quarterIds_[quarterNumber]);

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

            System.out.println(currentLink);
            links.add(week, currentLink);

            currentDate[2] += 7;
            if (currentDate[2] > daysInMonth_[currentDate[1]]) {
                currentDate[2] -= daysInMonth_[currentDate[1]];
                currentDate[1]++;
            }
        }

        return links;
    }

    public void setLinks() {
        links_.clear();
        int counter = 0;
        int quarterIdCounter = 0;
        String currentLink = new String("");
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

        String lp = new String(pupilUrl_);
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

    private String getPageCode(String url) throws Exception {
        if (csrftoken_ == null) {
            takeCsrftoken();
            while (csrftoken_ == null) { Thread.sleep(25); }
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

        String inputLine = new String("");
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }

        in.close();

        System.out.println(con.getResponseCode());
        System.out.println(response.toString());
        return response.toString();
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
}
