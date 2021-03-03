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
    private String ROOT_DIRECTORY;
    private String USER_AGENT = "Mozilla/5.0";

    private String csrftoken_;

    public PageDownloader(String rootDirectory) {
        ROOT_DIRECTORY = rootDirectory;
    }

    private String getPageCode(String url) {
        try {
            if (csrftoken_ == null) {
                takeCsrftoken();
                if (csrftoken_ == null) {
                    takeCsrftoken();
                    if (csrftoken_ == null) {
                        return "FE";
                    }
                }
            }
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setInstanceFollowRedirects(false);
            con.setUseCaches(false);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("cookie", ("csrftoken=" + csrftoken_ + "; sessionid=" + YearData.getSessionid())); // check

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append("\n");
            }
            in.close();
            con.disconnect();

            return response.toString();
        } catch (Exception e) {
            return "-";
        }
    }

    public boolean downloadPage(int quarterNumber, int weekNumber, String link) { // from one
        try {
            String pageCode = "-";
            int counter = 5;
            while (counter > 0 && pageCode.equals("-")) {
                pageCode = getPageCode(link);
                if (pageCode.equals("FE")) {
                    System.out.println("FATAL ERROR");
                    return false;
                } else {
                    System.out.println("NETWORK ERROR");
                    counter--;
                }
            }
            if (pageCode.equals("-")) {
                System.out.println("NETWORK ERROR (FINAL)");
                return false;
            }
            FileWriter fout = new FileWriter(
                    ROOT_DIRECTORY + "/p" + quarterNumber + "q/w" + weekNumber + ".html"
            );
            fout.write(pageCode);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            System.out.println("EXCEPTION -> " + e);
            return false;
        }
        return true;
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
            Map<String,List<String>> hf = con.getHeaderFields();
            con.disconnect();

            // find csrftoken
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

            System.out.println("csrftoken not found");
        } catch (Exception e) {
            System.out.println("csrftoken getting error, " + e);
            csrftoken_ = null;
            return;
        }
    }

}
