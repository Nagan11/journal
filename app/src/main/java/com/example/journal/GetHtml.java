package com.example.journal;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class GetHtml {

    // constants
    private static String USER_AGENT = "Mozilla/5.0";


    // login data
    private static String username;
    private static String password;
    private static String sessionid = new String("");
    private static String csrftoken = new String("");


    // help data
    private static CookieManager cookieManager = new CookieManager();
    private static int responseCode;


    // URL data
    private static String pupilUrl;
    private static String postParameters;

    private static int quarterId = 33;

    private static int[] amountsOfWeeks = new int[] {8, 7, 11, 8};
    private static int[][] firstMondays = new int[][] {{2019, 9, 2}, {2019, 11, 11}, {2020, 1, 13}, {2020, 4, 6}};

    private static ArrayList<String> links;
    private static ArrayList<String> fileNames;
    private static HashMap<String, ArrayList<String> > datesForWeeks;

    private static String currentDateTime;
    private static int[] daysInMonth = new int[13];


    // assist
    private static Scanner cin = new Scanner(System.in);

    private static String forBooleanInput;

    private static boolean exceptionCaught;



    public static String getCsrfToken() throws Exception {

        // open connection
        URL obj = new URL("https://schools.by/login");
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();

        // set connection args
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // get cookies
        con.getContent();
        CookieStore cookieJar = cookieManager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();

        // find csrftoken in cookies and return
        String temp = new String("");
        for (HttpCookie cookie : cookies) {

            if (cookie.getName().equals("csrftoken")) {
                temp = cookie.getValue();
            }

        }
        return temp;
    }

    public static void loginAndGetCookies(String url) throws Exception {

        // open connection
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // configure connection
        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);

        con.setRequestMethod("POST");

        con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        con.setRequestProperty("accept-encoding", "gzip, deflate, br");
        con.setRequestProperty("accept-language", "en-US,en;q=0.9");
        con.setRequestProperty("content-length", Integer.toString(postParameters.length()));
        con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        con.setRequestProperty("cookie", "csrftoken=" + csrftoken + ";"); //cookie
        con.setRequestProperty("origin", "https://schools.by");
        con.setRequestProperty("referer", "https://schools.by/login");
        con.setRequestProperty("user-agent", USER_AGENT);

        con.setDoOutput(true);
        con.setDoInput(true);

        DataOutputStream stream = new DataOutputStream(con.getOutputStream());
        stream.writeBytes(postParameters);
        stream.flush();
        stream.close();

        con.getContent();
        CookieStore cookieJar = cookieManager.getCookieStore();
        List <HttpCookie> cookies = cookieJar.getCookies();
        for (HttpCookie cookie : cookies) {

            // System.out.println(cookie.getName() + " -> " + cookie.getValue());
            if (cookie.getName().equals("csrftoken")) {
                csrftoken = cookie.getValue();
            }
            if (cookie.getName().equals("sessionid")) {

                sessionid = cookie.getValue();

                writeStatus("YES");
                writeSessionid(sessionid);
            }
        }

        pupilUrl = con.getHeaderField("location");
        pupilUrl += "/dnevnik/";
        writePupilUrl(pupilUrl);

        responseCode = con.getResponseCode();
    }

    public static String setPostParameters(String csrftoken, String username, String password) {

        String result = new String("");

        result += "csrfmiddlewaretoken=";
        result += csrftoken;
        result += "&username=";
        result += username;
        result += "&password=";
        result += password;

        return result;
    }

    public static String getWeek(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("cookie", "csrftoken=" + csrftoken + "; sessionid=" + sessionid);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine = new String("");
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    public static void showAllHeaders(HttpURLConnection con) {

        System.out.println("\n");
        Map<String, List<String> > headersTest;
        headersTest = con.getHeaderFields();

        for (Map.Entry<String, List<String> > item : headersTest.entrySet()) {

            for (String s : item.getValue()) {
                System.out.println("-> " + item.getKey() + ": " + s);
            }
        }
    }

    public static void fillDaysInMonth() {

        int firstYear;
        int secondYear;

        int month, year;

        String yearS = new String("");
        String monthS = new String("");

        for (int i = 0; i < 4; i++) {
            yearS += currentDateTime.charAt(i);
        }
        for (int i = 5; i < 7; i++) {
            monthS += currentDateTime.charAt(i);
        }

        month = Integer.parseInt(monthS);
        year = Integer.parseInt(yearS);

        if (month >= 1 && month <= 8) {
            firstYear = (year - 1);
            secondYear = year;
        } else {
            firstYear = year;
            secondYear = (year + 1);
        }

        daysInMonth[1] = 31;
        if (isLeap(secondYear)) {
            daysInMonth[2] = 29;
        } else {
            daysInMonth[2] = 28;
        }
        daysInMonth[3] = 31;
        daysInMonth[4] = 30;
        daysInMonth[5] = 31;
        daysInMonth[6] = 30;
        daysInMonth[7] = 31;
        daysInMonth[8] = 31;
        daysInMonth[9] = 30;
        daysInMonth[10] = 31;
        daysInMonth[11] = 30;
        daysInMonth[12] = 31;
    }

    public static boolean isLeap(int a) {
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

    public static ArrayList<String> getAllLinks() {

        int counter = 0;
        ArrayList<String> links = new ArrayList<String>();
        String currentLink = new String("");
        int[] currentDate = new int[3];

        for (int i = 0; i < 4; i++) {
            currentDate[0] = firstMondays[i][0];
            currentDate[1] = firstMondays[i][1];
            currentDate[2] = firstMondays[i][2];
            for (int week = 0; week < amountsOfWeeks[i]; week++, counter++) {

                currentLink = pupilUrl;

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
                if (currentDate[2] > daysInMonth[currentDate[1]]) {
                    currentDate[2] -= daysInMonth[currentDate[1]];
                    currentDate[1]++;
                }
            }
            quarterId++;
        }

        String lp = new String(pupilUrl);
        lp += "last-page";
        links.add(counter, lp);

        return links;
    }

    public static void downloadAll(List<String> ls) throws Exception {

        File pagesFolder = new File("pages");
        if (pagesFolder.exists()) {

            String[] entries = pagesFolder.list();
            for (String s : entries) {

                File currentFile = new File(pagesFolder.getPath(), s);
                currentFile.delete();
            }
        }

        String fileName = new String();
        int i = 0;

        for (int quarterCounter = 1; quarterCounter <= 4; quarterCounter++) {
            for (int j = 0; j < amountsOfWeeks[quarterCounter - 1]; j++, i++) {

                fileName = "pages/";

                fileName += "q";
                fileName += Integer.toString(quarterCounter);
                fileName += "w";
                fileName += Integer.toString(j + 1);
                fileName += ".html";

                System.out.printf("Downloading " + fileName + "...");
                FileWriter fout = new FileWriter(fileName, false);
                fout.write(getWeek(ls.get(i)));
                fout.flush();
                fout.close();
                System.out.println("COMPLETED!");
            }
        }

        fileName = "pages/lp.html";
        System.out.printf("Downloading " + fileName + "...");
        FileWriter fout = new FileWriter(fileName, false);
        fout.write(getWeek(ls.get(i)));
        fout.flush();
        fout.close();
        System.out.println("COMPLETED!");
    }

    public static boolean alreadyLoggedIn() throws Exception {

        FileReader fin = new FileReader("userData/status.txt");
        int c;
        String s = new String("");
        while ((c = fin.read()) != -1) {
            s += (char)c;
        }

        if (s.equals("YES")) {
            return true;
        } else {
            return false;
        }
    }

    public static String getSessionidFromFile() throws Exception {
        FileReader fin = new FileReader("userData/sessionid.txt");
        int c;
        String s = new String("");
        while ((c = fin.read()) != -1) {
            s += (char)c;
        }
        return s;
    }

    public static String getPupilUrlFromFile() throws Exception {
        FileReader fin = new FileReader("userData/pupilUrl.txt");
        int c;
        String s = new String("");
        while ((c = fin.read()) != -1) {
            s += (char)c;
        }
        return s;
    }

    public static void getLoginAndPassword() {
        System.out.print("Логин: ");
        username = cin.nextLine();
        System.out.print("Пароль: ");
        password = cin.nextLine();
        System.out.print("\n");
    }

    public static String getUsernameFromFile() throws Exception {
        FileReader fin = new FileReader("userData/username.txt");
        int c;
        String s = new String("");
        while ((c = fin.read()) != -1) {
            s += (char)c;
        }
        return s;
    }

    public static void writeStatus(String s) throws Exception {
        FileWriter fout = new FileWriter("userData/status.txt", false);
        fout.write(s);
        fout.flush();
        fout.close();
    }

    public static void writeUsername(String s) throws Exception {
        FileWriter fout = new FileWriter("userData/username.txt", false);
        fout.write(s);
        fout.flush();
        fout.close();
    }

    public static void writePupilUrl(String s) throws Exception {
        FileWriter fout = new FileWriter("userData/pupilUrl.txt", false);
        fout.write(s);
        fout.flush();
        fout.close();
    }

    public static void writeSessionid(String s) throws Exception {
        FileWriter fout = new FileWriter("userData/sessionid.txt", false);
        fout.write(s);
        fout.flush();
        fout.close();
    }

    public static void checkAndFix() {
        File userDataFolder = new File("userData");
        File pagesFolder = new File("pages");


        if (userDataFolder.exists()) {
            // System.out.println("userData folder exists\n");
        } else {
            // System.out.println("userData folder does not exist");
            userDataFolder.mkdir();

            try {
                FileWriter fout = new FileWriter("userData/status.txt", false);
                fout.write("NO");
                fout.flush();
                fout.close();
            } catch (Exception e) {
                System.out.println("An error occured (checkAndFix, FileWriter)");
            }
        }

        if (pagesFolder.exists()) {
            // System.out.println("pages folder exists\n");
        } else {
            // System.out.println("pages folder does not exist");
            pagesFolder.mkdir();
        }
    }

}
