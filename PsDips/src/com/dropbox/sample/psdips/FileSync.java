package com.dropbox.sample.psdips;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dropbox.sync.android.DbxFile;

import android.util.Log;

public class FileSync {

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
    
    /**
     * Copies the src file into the dst file. Assumes that the dst file
     * exists and it overwrites it.
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copy(File src, DbxFile dest) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = dest.getWriteStream();

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
