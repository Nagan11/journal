package com.example.journal;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class PageDownloader {
    private final String USER_AGENT = "Mozilla/5.0";
    private final String ROOT_DIRECTORY;
    private final String SESSIONID;

    private String csrftoken;

    public PageDownloader(String rootDirectory, String sessionid) {
        ROOT_DIRECTORY = rootDirectory;
        SESSIONID = sessionid;
    }

    private String getPageCode(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setConnectTimeout(2500);
        con.setReadTimeout(2500);
        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("cookie", ("csrftoken=" + csrftoken + "; sessionid=" + SESSIONID));

        String buffer;
        StringBuffer pageCode = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while ((buffer = in.readLine()) != null) {
            pageCode.append(buffer);
            pageCode.append("\n");
        }
        in.close();

        con.disconnect();

        if (pageCode.length() < 1) throw new Exception("Response missing");

        return pageCode.toString();
    }

    public boolean downloadPage(int quarterNumber, int weekNumber, String link) { // 1
        int getCodeAttempts = 5;
        while (getCodeAttempts-- > 0) {
            try {
                if (csrftoken == null) takeCsrftoken();
                String pageCode = getPageCode(link);

                FileWriter fout = new FileWriter(
                        ROOT_DIRECTORY + "/p" + quarterNumber + "q/w" + weekNumber + ".html"
                );
                fout.write(pageCode);
                fout.flush();
                fout.close();
                return true;
            } catch (Exception e) {
                System.out.println("Exception -> " + e);
                continue;
            }
        }
        return false;
    }

    private void takeCsrftoken() throws Exception {
        URL connectionUrl = new URL("https://schools.by/login");
        HttpURLConnection con = (HttpURLConnection)connectionUrl.openConnection();

        con.setConnectTimeout(2500);
        con.setReadTimeout(2500);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        con.connect();
        Map<String, List<String>> hf = con.getHeaderFields();
        con.disconnect();

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
        throw new Exception("csrftoken not found in headers");
    }

}
