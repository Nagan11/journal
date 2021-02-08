package com.example.journal;

import java.io.File;

public class PageParser {
    private final String ROOT_DIRECTORY;

    static {
        System.loadLibrary("JniParser");
    }

    PageParser(String rt_dir) {
        ROOT_DIRECTORY = rt_dir;
        checkFolders();
    }

    public native void parsePage(String pagePath, String dataPath);

    public void checkFolders() {
        File d1qFolder = new File(ROOT_DIRECTORY, "d1q");
        File d2qFolder = new File(ROOT_DIRECTORY, "d2q");
        File d3qFolder = new File(ROOT_DIRECTORY, "d3q");
        File d4qFolder = new File(ROOT_DIRECTORY, "d4q");

        if (!d1qFolder.exists()) {
            d1qFolder.mkdir();
        }

        if (!d2qFolder.exists()) {
            d2qFolder.mkdir();
        }

        if (!d3qFolder.exists()) {
            d3qFolder.mkdir();
        }

        if (!d4qFolder.exists()) {
            d4qFolder.mkdir();
        }
    }
}