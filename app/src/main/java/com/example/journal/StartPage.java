package com.example.journal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;

public class StartPage extends AppCompatActivity {
    private String ROOT_DIRECTORY;

    private Intent nextActivity;

    @Override
    public void onBackPressed() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        ROOT_DIRECTORY = String.valueOf(getFilesDir());

        checkAndFix();
        if (checkStatus()) {
            nextActivity = new Intent(this, MainMenuActivity.class);
            Timer switchTimer = new Timer();
            SwitchTimerTask timerTask = new SwitchTimerTask();
            switchTimer.schedule(timerTask, 0);
        } else {
            nextActivity = new Intent(this, LoginActivity.class);
            Timer switchTimer = new Timer();
            SwitchTimerTask timerTask = new SwitchTimerTask();
            switchTimer.schedule(timerTask, 0);
        }
    }

    private boolean checkStatus() {
        try {
            FileReader fin = new FileReader(ROOT_DIRECTORY + "/UserData/status.txt");
            int c;
            String status = "";

            while ((c = fin.read()) != -1) {
                status += (char)c;
            }

            if (status.equals("YES")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Reading error");
            return false;
        }
    }

    class SwitchTimerTask extends TimerTask {
        @Override
        public void run() {
            startActivity(nextActivity);
        }
    }

    public void checkAndFix() {
        File userDataFolder = new File(ROOT_DIRECTORY, "UserData");
        File statusText = new File(ROOT_DIRECTORY + "/UserData/status.txt");

        if (userDataFolder.exists()) {
            if (!statusText.exists()) {
                try {
                    FileWriter fout = new FileWriter(ROOT_DIRECTORY + "/UserData/status.txt", false);
                    fout.write("NO");
                    fout.flush();
                    fout.close();
                } catch (Exception e) {
                    Toast.makeText(this, "writing error", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            userDataFolder.mkdir();

            try {
                FileWriter fout = new FileWriter(ROOT_DIRECTORY + "/UserData/status.txt", false);
                fout.write("NO");
                fout.flush();
                fout.close();
            } catch (Exception e) {
                Toast.makeText(this, "writing error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}