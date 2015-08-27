package ru.firsto.intermusic;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by razor on 27.08.15.
 */
public class Downloader extends IntentService {
    public static final String ACTION = Downloader.class.getSimpleName()+".broadcast";

    private static final int MAX_BUFFER_SIZE = 1024;

    public Downloader() {
        super("downloader");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String artist = intent.getStringExtra("artist");
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");

        Log.d("TAG", "downloader got id " + intent.getIntExtra("id", 0));
        Log.d("TAG", "downloader got id " + Environment.getExternalStorageDirectory().getPath());
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + artist + " - " + title + ".mp3");
        try {
            download(url, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendResult(id);
    }

    private void sendResult(int id) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("id", id);
        sendBroadcast(intent);
    }

    private void download(String urlString, File file) throws IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(file);

            final byte data[] = new byte[MAX_BUFFER_SIZE];
            int count;
            while ((count = in.read(data, 0, MAX_BUFFER_SIZE)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }
}
