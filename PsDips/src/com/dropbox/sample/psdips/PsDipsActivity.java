package com.dropbox.sample.psdips;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.sample.psdips.R;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class PsDipsActivity extends Activity {

    private static final String appKey = "d459xjjuo51dqhc";
    private static final String appSecret = "2te8g6uqslbjmva";

    private static final int REQUEST_LINK_TO_DBX = 0;

    private TextView mTestOutput;
    private Button mLinkButton;
    private Button mSyncButton;
    private static DbxAccountManager mDbxAcctMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ps_dips);
        mTestOutput = (TextView) findViewById(R.id.test_output);
        mLinkButton = (Button) findViewById(R.id.link_button);
        mSyncButton = (Button) findViewById(R.id.sync_all);
        mLinkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLinkToDropbox();
            }
        });
        mSyncButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new UnsupportedOperationException("not implemented");
            }
        });

        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (mDbxAcctMgr.hasLinkedAccount()) {
		    showLinkedView();
		    doDropboxTest();
		} else {
			showUnlinkedView();
		}
	}

    private void showLinkedView() {
        mLinkButton.setVisibility(View.GONE);
        mTestOutput.setVisibility(View.VISIBLE);
        mSyncButton.setVisibility(View.VISIBLE);
    }

    private void showUnlinkedView() {
        mLinkButton.setVisibility(View.VISIBLE);
        mTestOutput.setVisibility(View.GONE);
        mSyncButton.setVisibility(View.GONE);
    }

    private void onClickLinkToDropbox() {
        mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                doDropboxTest();
            } else {
                mTestOutput.setText("Link to Dropbox failed or was cancelled.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("NewApi")
    private void doDropboxTest() {
        mTestOutput.setText("Dropbox Sync API Version "+DbxAccountManager.SDK_VERSION_NAME+"\n");
        try {
            final String TEST_DATA = "Hello Dropbox";
            final String TEST_FILE_NAME = "hello_dropbox.txt";
            DbxPath testPath = new DbxPath(DbxPath.ROOT, TEST_FILE_NAME);

            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());

            // Print the contents of the root folder.  This will block until we can
            // sync metadata the first time.
            List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
            mTestOutput.append("\nContents of app folder:\n");
            for (DbxFileInfo info : infos) {
                mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
            }

            // Create a test file only if it doesn't already exist.
            if (!dbxFs.exists(testPath)) {
                DbxFile testFile = dbxFs.create(testPath);
                try {
                    testFile.writeString(TEST_DATA);
                } finally {
                    testFile.close();
                }
                mTestOutput.append("\nCreated new file '" + testPath + "'.\n");
            }

            // Read and print the contents of test file.  Since we're not making
            // any attempt to wait for the latest version, this may print an
            // older cached version.  Use getSyncStatus() and/or a listener to
            // check for a new version.
            if (dbxFs.isFile(testPath)) {
                String resultData;
                DbxFile testFile = dbxFs.open(testPath);
                try {
                    resultData = testFile.readString();
                } finally {
                    testFile.close();
                }
                mTestOutput.append("\nRead file '" + testPath + "' and got data:\n    " + resultData);

                // print directory contents.
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                //FileSync.printDirectoryFiles(path);
                
                // copy img from internal memory to dropbox
                String src_path = path.getAbsoluteFile() + "/Facebook/IMG_540940567317936.jpeg";
                File src = new File(src_path);
                DbxPath dest_path = new DbxPath(DbxPath.ROOT, "test_img.jpeg");
                if (!dbxFs.exists(dest_path)) {
                    DbxFile dest = dbxFs.create(dest_path);
                    FileSync.copy(src, dest);
                    dest.close();
                }
                
                // sync test folder
                String src_folder_path = path.getAbsolutePath() + "/test-sync";
                Log.d("akshay", "syncing " + src_folder_path);
                DbxPath dest_folder_path = new DbxPath(DbxPath.ROOT, "test-sync");
                if (!dbxFs.exists(dest_folder_path)) {
                    dbxFs.createFolder(dest_folder_path);
                }
                FileSync.syncFolderMobileToDbx(src_folder_path, dest_folder_path);
            } else if (dbxFs.isFolder(testPath)) {
                mTestOutput.append("'" + testPath.toString() + "' is a folder.\n");
            }
        } catch (IOException e) {
            mTestOutput.setText("Dropbox test failed: " + e);
        }
    }
    
    public static DbxAccount getDbxLinkedAccount() {
        return mDbxAcctMgr.getLinkedAccount();
    }
}
