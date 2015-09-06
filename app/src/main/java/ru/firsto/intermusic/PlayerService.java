package ru.firsto.intermusic;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Created by razor on 06.09.15.
 */
public class PlayerService extends IntentService {
    public static final String ACTION = PlayerService.class.getSimpleName() + ".broadcast";
    public static final int UPDATE_PROGRESS = 2222;
    public static volatile boolean interrupted = false;

    private AudioPlayer mAudioPlayer;

    private boolean needNotify = false;

    public PlayerService() {
        super("PlayerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        interrupted = false;
        mAudioPlayer = AudioPlayer.get();

        Song song = intent.getParcelableExtra("song");
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Log.d("TAG", "player service got song: " + song.title);

        Bundle resultData = new Bundle();
        resultData.putString("title", song.title);
        resultData.putString("artist", song.artist);
        resultData.putInt("duration", song.duration);
        while (mAudioPlayer.isExist() && (mAudioPlayer.getSource().equals(song.path) || mAudioPlayer.getSource().equals(song.url))) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("TAG", "player position " + mAudioPlayer.getPosition());
            resultData.putInt("progress", mAudioPlayer.getPosition() / 1000);
            receiver.send(UPDATE_PROGRESS, resultData);
        }


        resultData.putInt("progress", song.duration);
        receiver.send(UPDATE_PROGRESS, resultData);
        Log.d("TAG", "PlayerService ends");
//        sendResult(id, file.length() != 0 ? file.getPath() : "");
    }

    private void sendResult(int id, String path) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("id", id);
        intent.putExtra("path", path);
        intent.putExtra("needNotify", needNotify);
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

            final byte data[] = new byte[1];
            int total = 0;
            int progressBuffer = 0;
            int count;
            while ((count = in.read(data, 0, 1)) != -1) {
                if (interrupted) break;
                total += count;
                progressBuffer += count;

                if (progressBuffer >= fileLength / 100 || total == fileLength) {
                    int progress = (total * 100 / fileLength);
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress", progress);
                    Log.d("TAG", "fileLength " + fileLength + " // total " + total + " // progress " + progress);
                    if (needNotify) receiver.send(UPDATE_PROGRESS, resultData);
                    progressBuffer = 0;
                }

                fout.write(data, 0, count);
            }
            if (file.length() != fileLength) interrupted = true;
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
