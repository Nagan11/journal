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
    private String ROOT_DIRECTORY;
    private Context THIS_CONTEXT;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private Intent mainMenuActivity;

    private LogInManager logInManager;

    private boolean breakLoginAttempt = false;
    private LoginState loginState;

    private Thread loginThread;
    private Thread loginAwaitThread;

    private RealNameParser realNameParser_;
    private String realName_;

    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        THIS_CONTEXT = this;
        usernameField = findViewById(R.id.UsernameInput);
        passwordField = findViewById((R.id.PasswordInput));

        logInManager = new LogInManager(ROOT_DIRECTORY);

        mainMenuActivity = new Intent(this, MainMenuActivity.class);

        breakLoginAttempt = false;

        loginButton = findViewById(R.id.LoginButton);
    }

    @Override
    public void onBackPressed() {}

    public void loginButtonOnClick(View view) {
        try {
            disableLoginButton();
            loginThread = new Thread(new LoginRunnable());
            loginAwaitThread = new Thread(new LoginAwaitRunnable());
            loginThread.start();


        } catch (Exception e) {
            enableLoginButton();
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
                    realNameParser_ = new RealNameParser(ROOT_DIRECTORY, logInManager.getSessionid(), logInManager.getPupilUrl());
                    realName_ = realNameParser_.getRealName();
                    while (realName_ == "*") {
                        try {
                            Thread.sleep(25);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    logInManager.writeLoginDataToFiles(usernameField.getText().toString(), realName_);

                    enableLoginButton();

                    mainMenuActivity.putExtra("csrftoken", logInManager.getCsrftoken());
                    startActivity(mainMenuActivity);
                    break;
                case WRONG_PASSWORD:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "Wrong login or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLoginButton();
                    break;
                case ERROR_OCCURED:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "Error occured", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLoginButton();
                    break;
                case DEFAULT:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(THIS_CONTEXT, "WTF", Toast.LENGTH_SHORT).show();
                        }
                    });
                    enableLoginButton();
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

    private void enableLoginButton() {
        runOnUiThread(new Runnable() {
            public void run() {
                loginButton.setEnabled(true);
            }
        });

        // graphics
    }

    private void disableLoginButton() {
        runOnUiThread(new Runnable() {
            public void run() {
                loginButton.setEnabled(false);
            }
        });

        // graphics
    }

}