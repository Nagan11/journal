package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainMenu extends AppCompatActivity {
    private String ROOT_DIRECTORY;

    private Intent journalActivity_;

    private WeekManager weekManager_;
    private Thread downloadThread_;

    private PageParser pageParser_;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ROOT_DIRECTORY = String.valueOf(getFilesDir());

        weekManager_ = new WeekManager(ROOT_DIRECTORY, getIntent().getStringExtra("csrftoken"));
        pageParser_ = new PageParser(ROOT_DIRECTORY);
        pageParser_.checkFolders();

        setRealName();

        journalActivity_ = new Intent(this, JournalActivity.class);
    }

    private void setUserName() {
        String buffer = new String("");
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/username.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = (TextView)findViewById(R.id.UserNameTextView);
        userNameTextView.setText(buffer);
    }

    private void setRealName() {
        String buffer = new String("");
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/realName.txt");

            int c;
            while ((c = fin.read()) != -1) {
                buffer += (char)c;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        TextView userNameTextView = (TextView)findViewById(R.id.UserNameTextView);
        userNameTextView.setText(buffer);
    }

    public void logOutButtonOnClick(View view) {
        cleanUserData();
        Intent logInActivity = new Intent(this, NewUserLoginPage.class);
        startActivity(logInActivity);
    }

    private void cleanUserData() {
        File userDataFolder = new File(ROOT_DIRECTORY + "/UserData");
        File statusText = new File(ROOT_DIRECTORY + "/UserData/status.txt");
        for (File f : userDataFolder.listFiles()) {
            f.delete();
        }

        try {
            FileWriter fout = new FileWriter(statusText.getPath());
            fout.write("NO");
            fout.flush();
            fout.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onBackPressed() {}

    public void downloadAllOnClick(View view) throws Exception {
//        downloadThread_ = new Thread(new DownloadAllRunnable());
//        downloadThread_.start();

//        pageParser_.parsePage(ROOT_DIRECTORY + "/p2q/w2.html", ROOT_DIRECTORY + "/test.txt");
//        try {
//            int c;
//            String buf = "";
//            FileReader fin = new FileReader(ROOT_DIRECTORY + "/test.txt");
//            while ((c = fin.read()) != -1) {
//                buf += (char)c;
//            }
//            System.out.println(buf);
//        } catch (Exception e) {
//            System.out.println("sosi");
//        }

//        weekManager_.setLinks();

        startActivity(journalActivity_);
    }

    class DownloadAllRunnable implements Runnable {
        @Override
        public void run() {
            try {
                weekManager_.updateQuarter(0);
                weekManager_.updateQuarter(1);
                weekManager_.updateQuarter(2);
                weekManager_.updateQuarter(3);
            } catch (Exception e) {
                System.out.println("Download failed (" + e + ")");
            }
        }
    }
}