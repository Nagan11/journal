package com.example.journal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LogInActivity extends AppCompatActivity {
    private final Context CONTEXT = this;
    private String ROOT_DIRECTORY;

    private TextInputEditText usernameField;
    private TextInputEditText passwordField;

    private LogInManager logInManager;

    private boolean breakLoginAttempt = false;
    private LoginState loginState;

    private Thread loginThread;
    private Thread loginAwaitThread;

    private RealNameParser realNameParser;
    private String realName;

    private Button logInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        usernameField = findViewById(R.id.UsernameInput);
        passwordField = findViewById((R.id.PasswordInput));

        logInManager = new LogInManager(ROOT_DIRECTORY);

        breakLoginAttempt = false;

        logInButton = findViewById(R.id.LogInButton);
        enableLogInButton();
    }

    @Override
    public void onBackPressed() {}

    public void loginButtonOnClick(View view) {
        try {
            disableLogInButton();
            loginThread = new Thread(new LoginRunnable());
            loginAwaitThread = new Thread(new LoginAwaitRunnable());
            loginThread.start();
        } catch (Exception e) {
            enableLogInButton();
            System.out.println(e);
        }
    }

    class LoginRunnable implements Runnable {
        @Override
        public void run() {
            breakLoginAttempt = false;
            try {
                loginState = LoginState.DEFAULT;
                loginAwaitThread.start();
                loginState = logInManager.loggedIn(usernameField.getText().toString(), passwordField.getText().toString());
                while (loginState == LoginState.DEFAULT) { Thread.sleep(25); }
            } catch (Exception e) {
                System.out.println(e);
            }
            breakLoginAttempt = true;
        }
    }

    class LoginAwaitRunnable implements Runnable {
        @Override
        public void run() {
            Timer breakTimer = new Timer();
            LoginBreakTimerTask task = new LoginBreakTimerTask();
            breakTimer.schedule(task, 10000); // break while-cycle with 10s timeout
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (breakLoginAttempt) {
                    break;
                }
            }
            breakLoginAttempt = false;

            switch (loginState) {
                case LOGGED_IN:
                    realNameParser = new RealNameParser(logInManager.getSessionid(), logInManager.getPupilUrl());
                    realName = realNameParser.getRealName();
                    while (realName == "*") {
                        try {
                            Thread.sleep(25);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    logInManager.writeLoginDataToFiles(usernameField.getText().toString(), realName);
                    startActivity(new Intent(CONTEXT, MainMenuActivity.class));
                    break;
                case WRONG_PASSWORD:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CONTEXT, "Wrong login or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLogInButton();
                    break;
                case ERROR_OCCURED:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CONTEXT, "Error occured", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLogInButton();
                    break;
                case DEFAULT:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CONTEXT, "WTF", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLogInButton();
                    break;
            }
        }
    }

    class LoginBreakTimerTask extends TimerTask {
        @Override
        public void run() {
            loginState = LoginState.ERROR_OCCURED;
            breakLoginAttempt = true;
        }

    }

    private void enableLogInButton() {
        runOnUiThread(new Runnable() {
            public void run() {
                logInButton.setEnabled(true);
            }
        });
    }
    private void disableLogInButton() {
        runOnUiThread(new Runnable() {
            public void run() {
                logInButton.setEnabled(false);
            }
        });
    }

}