package com.example.journal;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class NewUserLoginPage extends AppCompatActivity {
    private String ROOT_DIRECTORY;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private LogInManager logInManager;
    private Intent mainMenuActivity;
    private boolean loggedIn;
    private boolean breakLoginAttempt;
    private boolean wrongLoginData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        usernameField = (TextInputEditText) findViewById(R.id.UsernameInput);
        passwordField = (TextInputEditText) findViewById((R.id.PasswordInput));

        logInManager = new LogInManager(ROOT_DIRECTORY);

        mainMenuActivity = new Intent(this, MainMenu.class);

        loggedIn = false;
        breakLoginAttempt = false;
        wrongLoginData = false;
    }

    @Override
    public void onBackPressed() {}

    public void loginButtonOnClick(View view) {
        try {
            Thread loginThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (logInManager.loggedIn(usernameField.getText().toString(), passwordField.getText().toString())) {
                            System.out.println(logInManager.getSessionid());
                            logInManager.writeLoginDataToFiles(usernameField.getText().toString());
                            loggedIn = true;
                        } else {
                            wrongLoginData = true;
                        }
                        breakLoginAttempt = true;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });

            loginThread.start();
            Timer breakTimer = new Timer();
            LoginBreakTimerTask task = new LoginBreakTimerTask();
            breakTimer.schedule(task, 5000); // break while-cycle with 5s timeout
            while (true) {
                Thread.sleep(250);
                if (breakLoginAttempt) {
                    if (wrongLoginData) {
                        Toast.makeText(this, "Wrong login or password", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!loggedIn) {
                            Toast.makeText(this, "Login Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }

            }
            if (loggedIn) {
                startActivity(mainMenuActivity);
            }


        } catch (Exception e) {
            System.out.println(e);
        }

    }

    class LoginBreakTimerTask extends TimerTask {
        @Override
        public void run() {
            breakLoginAttempt = true;
        }

    }

}