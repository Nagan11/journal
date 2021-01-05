package com.example.journal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RealNameParser {
    private String USER_AGENT = "Mozilla/5.0";
    private String ROOT_DIRECTORY;

    private String csrftoken_;
    private String sessionid_;
    private String pupilUrl_;

    private CookieManager cookieManager = new CookieManager();

    RealNameParser(String rootDirectory, String sessionid, String pupilUrl) {
        ROOT_DIRECTORY = rootDirectory;

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        sessionid_ = sessionid;
        pupilUrl_ = pupilUrl;

        System.out.println("URL real name -> " + pupilUrl_);
        System.out.println("sessionid -> " + sessionid_);
    }

    public String getRealName() {
        String mainPage = getPageCode(pupilUrl_);
        System.out.println("out URL -> " + pupilUrl_);
        String buffer = new String("");
        if (mainPage.equals("-")) {
            return "-";
        }

        for (int i = 0; i < mainPage.length(); i++) {
            if (mainPage.charAt(i) == '<') {
                buffer += mainPage.charAt(i);
                i++;
                while (mainPage.charAt(i) != '>') {
                    buffer += mainPage.charAt(i);
                    i++;
                }
                buffer += mainPage.charAt(i);
                i++;

                if (buffer.equals("<title>")) {
                    while (mainPage.charAt(i) == ' ' || mainPage.charAt(i) == '\t' || mainPage.charAt(i) == '\n') {
//                        System.out.println(mainPage.charAt(i) + " - space");
                        i++;
                    }
                    String realName = new String("");
                    while (mainPage.charAt(i) != '.') {
//                        System.out.println(mainPage.charAt(i) + " - letter");
                        realName += mainPage.charAt(i);
                        i++;
                    }
                    return realName;
                }
            }
//            System.out.println(buffer);
            buffer = "";
        }
        return "-";
    }

    private String getPageCode(String url) {
        try {
            if (csrftoken_ == null) {
                takeCsrftoken();
                while (csrftoken_ == null) { Thread.sleep(25); }
                System.out.println("CSRF real -> " + csrftoken_);
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
        } catch (Exception e) {
            System.out.println(e);
            return "-";
        }
    }

    private void takeCsrftoken() throws Exception {
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
                con.disconnect();
                cookieJar.removeAll();
                break;
            }
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
}
