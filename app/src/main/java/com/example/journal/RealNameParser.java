package com.example.journal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RealNameParser {
    private final String USER_AGENT = "Mozilla/5.0";
    private final String SESSIONID;
    private final String PUPIL_URL;

    private String csrftoken;

    RealNameParser(String sessionid, String pupilUrl) {
        SESSIONID = sessionid;
        PUPIL_URL = pupilUrl;
    }

    public String getRealName() {
        String mainPage = getPageCode(PUPIL_URL);
        String buffer = "";
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
                        i++;
                    }
                    String realName = "";
                    while (mainPage.charAt(i) != '.') {
                        realName += mainPage.charAt(i);
                        i++;
                    }
                    return realName;
                }
            }
            buffer = "";
        }
        return "-";
    }

    private String getPageCode(String url) {
        try {
            if (csrftoken == null) {
                takeCsrftoken();
                if (csrftoken == null) {
                    throw new Exception("csrftoken getting error");
                }
            }
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("cookie", "csrftoken=" + csrftoken + "; sessionid=" + SESSIONID);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
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

    private void takeCsrftoken() {
        try {
            // open connection
            URL connectionUrl = new URL("https://schools.by/login");
            HttpURLConnection con = (HttpURLConnection)connectionUrl.openConnection();

            // set connection args
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            // get cookies
            con.connect();
            Map<String, List<String>> hf = con.getHeaderFields();
            con.disconnect();

            // find csrftoken
            for (Map.Entry<String, List<String>> ent : hf.entrySet()) {
                try {
                    if (ent.getKey().equals("Set-Cookie")) {
                        List<HttpCookie> cookies = HttpCookie.parse(ent.getValue().get(0));
                        for (HttpCookie cookie : cookies) {
                            if (cookie.getName().equals("csrftoken")) {
                                csrftoken = cookie.getValue();
                                return;
                            }
                        }
                    }
                } catch (NullPointerException npe) {
                    continue;
                }
            }

            System.out.println("csrftoken not found");
        } catch (Exception e) {
            System.out.println("csrftoken getting error, " + e);
            csrftoken = null;
            return;
        }
    }
}
