package ru.firsto.intermusic;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by razor on 27.08.15.
 */
public class DownloadService extends IntentService {
    public static final String ACTION = DownloadService.class.getSimpleName() + ".broadcast";
    public static final int UPDATE_PROGRESS = 1111;
    public static volatile boolean interrupted = false;

    private static final int MAX_BUFFER_SIZE = 1024;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String artist = intent.getStringExtra("artist");
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
        ResultReceiver receiver = intent.getParcelableExtra("receiver");

        Log.d("TAG", "downloader got id " + intent.getIntExtra("id", 0));
        Log.d("TAG", "downloader got id " + Environment.getExternalStorageDirectory().getPath());
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + artist + " - " + title + ".mp3");
        try {
            download(url, file, receiver);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            Log.d("TAG", "file size = " + file.length()
//                    + " // path: " + file.getPath()
//                    + " // abs path " + file.getAbsolutePath()
//                    + " // canonical path " + file.getCanonicalPath()
//            );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        sendResult(id, file.length() != 0 ? file.getPath() : "");
    }

    private void sendResult(int id, String path) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("id", id);
        intent.putExtra("path", path);
        sendBroadcast(intent);
    }

    private void download(String urlString, File file, ResultReceiver receiver) throws IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();
            Log.d("TAG", "fileLength = " + fileLength + " // file.length() = " + file.length());
            if (file.length() == fileLength) return;

            in = new BufferedInputStream(connection.getInputStream());
            fout = new FileOutputStream(file);

            final byte data[] = new byte[MAX_BUFFER_SIZE];
            int total = 0;
            int progressBuffer = 0;
            int count;
            while ((count = in.read(data, 0, MAX_BUFFER_SIZE)) != -1) {
                if (interrupted) break;
                total += count;
                progressBuffer += count;

                if (progressBuffer >= fileLength / 100 || total == fileLength) {
                    int progress = (total * 100 / fileLength);
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress", progress);
                    Log.d("TAG", "fileLength " + fileLength + " // total " + total + " // progress " + progress);
                    receiver.send(UPDATE_PROGRESS, resultData);
                    progressBuffer = 0;
                }

                fout.write(data, 0, count);
            }
            fout.flush();
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
            if (interrupted) {
                file.delete();
            }
            interrupted = false;
        }
    }

}
