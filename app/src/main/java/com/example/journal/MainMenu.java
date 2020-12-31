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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ROOT_DIRECTORY = String.valueOf(getFilesDir());

        setUserName();
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
}