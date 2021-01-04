package com.example.journal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class NewUserLoginPage extends AppCompatActivity {
    private String ROOT_DIRECTORY;
    private Context THIS_CONTEXT;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private Intent mainMenuActivity;

    private LogInManager logInManager;

    private boolean breakLoginAttempt;
    private LoginState loginState;

    Thread loginThread;
    Thread loginAwaitThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        THIS_CONTEXT = this;
        usernameField = findViewById(R.id.UsernameInput);
        passwordField = findViewById((R.id.PasswordInput));

        logInManager = new LogInManager(ROOT_DIRECTORY);

        mainMenuActivity = new Intent(this, MainMenu.class);

        breakLoginAttempt = false;
    }

    @Override
    public void onBackPressed() {}

    public void loginButtonOnClick(View view) {
        try {
            loginThread = new Thread(new LoginRunnable());
            loginAwaitThread = new Thread(new LoginAwaitRunnable());
            loginThread.start();


        } catch (Exception e) {
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
                while (loginState == LoginState.DEFAULT) {}
            } catch (Exception e) {
                System.out.println(e);
            }
            breakLoginAttempt = true;
        }
    }

    class LoginAwaitRunnable implements Runnable {
        @Override
        public void run() {
//            Looper.prepare();
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
            switch (loginState) {
                case LOGGED_IN:
                    logInManager.writeLoginDataToFiles(usernameField.getText().toString());

//                    logInManager.takeCsrftoken();
                    mainMenuActivity.putExtra("csrftoken", logInManager.getCsrftoken());
//                    mainMenuActivity.putExtra("sessionid", logInManager.getSessionid());
//                    mainMenuActivity.putExtra("pupilUrl", logInManager.getPupilUrl());
                    startActivity(mainMenuActivity);
                    break;
                case WRONG_PASSWORD:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "Wrong login or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case ERROR_OCCURED:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "Error occured", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case DEFAULT:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "WTF", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }
    }

    class LoginBreakTimerTask extends TimerTask {
        @Override
        public void run() {
            breakLoginAttempt = true;
        }

    }

}