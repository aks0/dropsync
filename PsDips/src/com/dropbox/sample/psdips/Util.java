package com.dropbox.sample.psdips;

import java.io.File;

import android.util.Log;

public class Util {

    public static void printDirectoryFiles(File file) {
        if (!file.isDirectory()) {
            Log.d("akshay", file.getAbsolutePath());
            return;
        }
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                printDirectoryFiles(child);
            } else {
                Log.d("akshay", child.getAbsolutePath());
            }
        }
    }
}
