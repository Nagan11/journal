package com.example.journal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DownloadManager {
    private String USER_AGENT = "Mozilla/5.0";
    private String ROOT_DIRECTORY;

    private String csrftoken_;
    private String sessionid_;
    private String pupilUrl_;

    private CookieManager cookieManager = new CookieManager();

    private int[] quarterIds_ = new int[] {33, 34, 35, 36};
    private int[] amountsOfWeeks_ = new int[] {8, 7, 11, 8};
    private int[][] firstMondays_ = new int[][] {{2019, 9, 2}, {2019, 11, 11}, {2020, 1, 13}, {2020, 4, 6}};

    private int[] daysInMonth_ = new int[13];



    DownloadManager(String rtDir, String csrftoken) {
        ROOT_DIRECTORY = rtDir;
        fillDaysInMonth();
        checkFolders();

        csrftoken_ = csrftoken;
        try {
            readData();
        } catch (Exception e) {
            System.out.println(e);
        }

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        System.out.println("URL -> " + pupilUrl_);
        System.out.println("Sessionid -> " + sessionid_);
        System.out.println("csrftoken -> " + csrftoken_);
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
        ArrayList<String> quarterLinks = new ArrayList<>(getQuarterLinks(quarterNumber));
        cleanPagesFolder(quarterNumber);

        for (int i = 0; i < amountsOfWeeks_[quarterNumber]; i++) {
            String fileName = ROOT_DIRECTORY;
            fileName += "/p" + Integer.toString(quarterNumber + 1) + "q/w";
            fileName += Integer.toString(i + 1);
            fileName += ".html";

            FileWriter fout = new FileWriter(fileName, false);
            fout.write(getPageCode(quarterLinks.get(i)));
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

    private String getPageCode(String url) throws Exception {
        if (csrftoken_ == null) {
            takeCsrftoken();
            while (csrftoken_ == null) {}
            System.out.println("CSRF -> " + csrftoken_);
        }
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("cookie", "csrftoken=" + csrftoken_ + "; sessionid=" + sessionid_);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine = new String("");
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    public void takeCsrftoken() throws Exception {
        // open connection
        URL connectionUrl = new URL("https://schools.by/login");
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
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int secondYear = Calendar.getInstance().get(Calendar.YEAR);
        if (month > 7) {
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

    private ArrayList<String> getAllLinks(int quarterId) {
        int counter = 0;
        ArrayList<String> links = new ArrayList<String>();
        String currentLink = new String("");
        int[] currentDate = new int[3];

        for (int i = 0; i < 4; i++) {
            currentDate[0] = firstMondays_[i][0];
            currentDate[1] = firstMondays_[i][1];
            currentDate[2] = firstMondays_[i][2];
            for (int week = 0; week < amountsOfWeeks_[i]; week++, counter++) {
                currentLink = pupilUrl_;

                currentLink += "quarter/";
                currentLink += Integer.toString(quarterId);

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

                links.add(counter, currentLink);

                currentDate[2] += 7;
                if (currentDate[2] > daysInMonth_[currentDate[1]]) {
                    currentDate[2] -= daysInMonth_[currentDate[1]];
                    currentDate[1]++;
                }
            }
            quarterId++;
        }

        String lp = new String(pupilUrl_);
        lp += "last-page";
        links.add(counter, lp);

        return links;
    }

}
