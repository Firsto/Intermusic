package ru.firsto.intermusic;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by razor on 15.08.15.
 **/
public class AudioPlayer {

    private MediaPlayer mPlayer;
    private String source;

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void pause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            } else {
                mPlayer.start();
            }
        }
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public boolean isExist() {
        return mPlayer != null;
    }

    public void setPosition(int position) {
        mPlayer.seekTo(position);
    }

    public int getPosition() {
        if (isExist()) {
            return mPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    public int getDuration() {
        if (isExist()) {
            return mPlayer.getDuration();
        } else {
            return -1;
        }
    }

    public void setListener(MediaPlayer.OnBufferingUpdateListener listener) {
        if (mPlayer != null) mPlayer.setOnBufferingUpdateListener(listener);
    }

    public boolean play(String source) {

        if (!source.equals(this.source)) {
            stop();

            mPlayer = new MediaPlayer();
            this.source = source;

            try {
                mPlayer.setDataSource(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            mPlayer.prepareAsync();

            return true;
        }

        return false;
    }
}
