package com.dropbox.sample.psdips;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

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
        dest.close();
    }
    
    /**
     * Syncs all the files in the src_folder with those in the dest_folder.
     * 'One-way' sync from Mobile to Dropbox
     * @param src_folder
     * @param dest_folder
     * @throws IOException 
     */
    public static void syncFolderMobileToDbx(String src_path,
            DbxPath dest_path) throws IOException {
        File src_folder = new File(src_path);
        DbxFileSystem dbxFs =
                DbxFileSystem.forAccount(PsDipsActivity.getDbxLinkedAccount());
        if (!src_folder.isDirectory() || !dbxFs.isFolder(dest_path)) {
            throw new IllegalArgumentException("The args must be folders.");
        }
        File[] m_files = src_folder.listFiles();
        ArrayList<DbxFileInfo> dbx_files =
               (ArrayList<DbxFileInfo>) dbxFs.listFolder(dest_path);
        for (File m_file : m_files) {
            String m_filename = m_file.getName();
            boolean isSynced = false;
            // TO DO: optimize the look up
            for (DbxFileInfo dbx_file : dbx_files) {
                String dbx_filename = dbx_file.path.getName();
                if (m_filename.equals(dbx_filename)) {
                    isSynced = true;
                    break;
                }
            }
            if (!isSynced) {
                DbxPath dbx_file_path = new DbxPath(dest_path, m_filename);
                DbxFile dbx_file = dbxFs.create(dbx_file_path);
                Log.d("akshay", String.format("copying %s ==> %s\n",
                        m_file.getName(), dbx_file.getPath().toString()));
                copy(m_file, dbx_file);
            }
        }
    }
}
