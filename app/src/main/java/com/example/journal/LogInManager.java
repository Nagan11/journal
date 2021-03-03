package com.example.journal;

import android.os.Build;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LogInManager {
    // constants
    private String USER_AGENT = "Mozilla/5.0";
    private String ROOT_DIRECTORY;

    // login data
    private String sessionid_ = "";
    private String csrftoken_ = "";

    // URL data
    private String pupilUrl_;
    private String postParameters_;

    public LogInManager(String rtDir) {
        ROOT_DIRECTORY = rtDir;
    }

    private void setPostParameters(String un, String pw) {
        postParameters_ = "csrfmiddlewaretoken=";
        postParameters_ += csrftoken_;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            postParameters_ += "&username=";
            try {
                postParameters_ += URLEncoder.encode(un, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                postParameters_ += URLEncoder.encode(un);
            }

            postParameters_ += "&password=";
            try {
                postParameters_ += URLEncoder.encode(pw, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                postParameters_ += URLEncoder.encode(pw);
            }
        }
        else {
            postParameters_ += "&username=";
            postParameters_ += URLEncoder.encode(un);
            postParameters_ += "&password=";
            postParameters_ += URLEncoder.encode(pw);
        }
    }

    public void writeLoginDataToFiles(String username, String realName) {
        try {
            FileWriter foutUsername = new FileWriter(ROOT_DIRECTORY + "/UserData/username.txt", false);
            foutUsername.write(username);
            foutUsername.flush();
            foutUsername.close();
        } catch (Exception e) {
            System.out.println("foutUsername error");
        }

        try {
            FileWriter foutSessionid = new FileWriter(ROOT_DIRECTORY + "/UserData/sessionid.txt", false);
            foutSessionid.write(sessionid_);
            foutSessionid.flush();
            foutSessionid.close();
        } catch (Exception e) {
            System.out.println("foutSessionid error");
        }

        try {
            FileWriter foutUrl = new FileWriter(ROOT_DIRECTORY + "/UserData/pupilUrl.txt", false);
            foutUrl.write(pupilUrl_);
            foutUrl.flush();
            foutUrl.close();
        } catch (Exception e) {
            System.out.println("foutUrl error");
        }

        try {
            FileWriter foutRealName = new FileWriter(ROOT_DIRECTORY + "/UserData/realName.txt", false);
            foutRealName.write(realName);
            foutRealName.flush();
            foutRealName.close();
        } catch (Exception e) {
            System.out.println("foutRealName error");
        }

        try {
            FileWriter foutStatus = new FileWriter(ROOT_DIRECTORY + "/UserData/status.txt", false);
            foutStatus.write("YES");
            foutStatus.flush();
            foutStatus.close();
        } catch (Exception e) {
            System.out.println("foutStatus error");
        }
    }

    public void takeCsrftoken() throws Exception {
        // open connection
        URL connectionUrl = new URL("https://schools.by/login");
        HttpURLConnection con = (HttpURLConnection)connectionUrl.openConnection();

        // set connection args
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        Map<String,List<String>> hf = con.getHeaderFields();
        for (Map.Entry<String, List<String>> ent : hf.entrySet()) {
            try {
                if (ent.getKey().equals("Set-Cookie")) {
                    List<HttpCookie> cookies = HttpCookie.parse(ent.getValue().get(0));
                    for (HttpCookie cookie : cookies) {
                        if (cookie.getName().equals("csrftoken")) {
                            csrftoken_ = cookie.getValue();
                            return;
                        }
                    }
                }
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public LoginState loggedIn(String username, String password) {
        try {
            takeCsrftoken();
        } catch (Exception e1) {
            try {
                takeCsrftoken();
            } catch (Exception e2) {
                return LoginState.ERROR_OCCURED;
            }
        }

        setPostParameters(username, password);

        URL connectionUrl;
        HttpURLConnection con;
        try {
            // open connection
            connectionUrl = new URL("https://schools.by/login");
            con = (HttpURLConnection) connectionUrl.openConnection();

            // configure connection
            con.setInstanceFollowRedirects(false);
            con.setUseCaches(false);

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.setRequestProperty("accept-encoding", "gzip, deflate, br");
            con.setRequestProperty("accept-language", "en-gb");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("content-length", Integer.toString(postParameters_.length()));
            con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            con.setRequestProperty("cookie", "csrftoken=" + csrftoken_ + ";"); //cookie
            con.setRequestProperty("origin", "https://schools.by");
            con.setRequestProperty("referer", "https://schools.by/login");
            con.setRequestProperty("user-agent", USER_AGENT);

            con.setDoOutput(true);
            con.setDoInput(true);
        } catch (Exception e) {
            return LoginState.ERROR_OCCURED;
        }

        // send data
        try {
            DataOutputStream stream = new DataOutputStream(con.getOutputStream());
            stream.writeBytes(postParameters_);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            return LoginState.ERROR_OCCURED;
        }

        try {
            con.getContent();
        } catch (Exception e) {
            return LoginState.ERROR_OCCURED;
        }


        // login check
        int responseCode = -1;
        try {
            responseCode = con.getResponseCode();
        } catch (Exception e) {
            return LoginState.ERROR_OCCURED;
        }
        while (responseCode == -1) {
            try {
                Thread.sleep(25);
            } catch (Exception e) {
                return LoginState.ERROR_OCCURED;
            }
        }
        if (responseCode == 200) {
            return LoginState.WRONG_PASSWORD;
        }

        // get csrftoken, sessionid, pupilUrl
        Map<String,List<String>> hf = con.getHeaderFields();
        for (Map.Entry<String, List<String>> ent : hf.entrySet()) {
            try {
                if (ent.getKey().equals("Set-Cookie")) {
                    for (String s : ent.getValue()) {
                        List<HttpCookie> cookies = HttpCookie.parse(s);
                        for (HttpCookie cookie : cookies) {
                            if (cookie.getName().equals("csrftoken")) {
                                csrftoken_ = cookie.getValue();
                            }
                            if (cookie.getName().equals("sessionid")) {
                                sessionid_ = cookie.getValue();
                            }
                        }
                    }
                }
                if (ent.getKey().equals("Location")) {
                    pupilUrl_ = ent.getValue().get(0);
                }
            } catch (NullPointerException npe) {
                continue;
            }
        }

        return LoginState.LOGGED_IN;
    }

    public String getSessionid() {
        return sessionid_;
    }
    public String getPupilUrl() {
        return pupilUrl_;
    }
}
