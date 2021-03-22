package com.example.journal;

public class LastPageParser {
    static {
        System.loadLibrary("JniLastPageParser");
    }

    private final String ROOT_DIRECTORY;

    LastPageParser(String rootDirectory) {
        ROOT_DIRECTORY = rootDirectory;
    }

    public native void parsePage(String pagePath, String dataPath);
}
