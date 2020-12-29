package com.example.journal;

import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class NewUserLoginPage extends AppCompatActivity {
    private String ROOT_DIRECTORY;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private LogInManager logInManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user_login);

        ROOT_DIRECTORY = String.valueOf(getFilesDir());
        usernameField = (TextInputEditText) findViewById(R.id.UsernameInput);
        passwordField = (TextInputEditText) findViewById((R.id.PasswordInput));

        logInManager = new LogInManager(ROOT_DIRECTORY);
    }

    public void loginButtonOnClick(View view) {
        try {
            Thread loginThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (logInManager.loggedIn(usernameField.getText().toString(), passwordField.getText().toString())) {
                            System.out.println(logInManager.getSessionid());
                            logInManager.writeLoginDataToFiles(usernameField.getText().toString());
                        } else {
                            // output that login or password are incorrect
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });

            loginThread.start();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}