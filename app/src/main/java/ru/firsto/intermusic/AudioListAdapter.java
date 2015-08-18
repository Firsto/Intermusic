package ru.firsto.intermusic;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;

/**
 * Created by razor on 15.08.15.
 **/
public class AudioListAdapter extends ArrayAdapter<VKApiAudio> {

    private List<VKApiAudio> mAudioList;
    private Activity mContext;
    private AudioPlayer mAudioPlayer;

    private final Handler handler = new Handler();

    private int playingId = -1;
    private int nextId = -1;
    private int bufferedProgress = 0;
    private boolean played = false;
    private boolean interrupted = false;

    public AudioListAdapter(Activity context, List<VKApiAudio> audioList, AudioPlayer audioPlayer) {
        super(context, R.layout.item_song, audioList);
        this.mAudioList = audioList;
        this.mContext = context;
        this.mAudioPlayer = audioPlayer;
    }

    class AudioViewHolder {
        protected CheckBox mSelector;
        protected TextView mSongTitle, mAuthor, mDuration, mRemaining;
        protected ImageButton mPlayButton;
        protected SeekBar mProgressBar;
        private View mView;

        public AudioViewHolder(View view) {
            mView = view;
            mSongTitle = (TextView) view.findViewById(R.id.tvSongTitle);
            mAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            mDuration = (TextView) view.findViewById(R.id.tvDuration);
            mRemaining = (TextView) view.findViewById(R.id.tvRemaining);
            mSelector = (CheckBox) view.findViewById(R.id.chbSelector);
            mPlayButton = (ImageButton) view.findViewById(R.id.btnPlay);
            mProgressBar = (SeekBar) view.findViewById(R.id.progressBar);
        }

        public void setObj(final VKApiAudio song){
            mSongTitle.setText(song.title);
            mAuthor.setText(song.artist);
            mDuration.setText(getDurationString(song.duration));

            if (playingId == song.id) {

                if (mAudioPlayer.play(song.url)) {
                    mAudioPlayer.setListener(bufferingListener);
                }

                mProgressBar.setMax(song.duration);
//            if (!mAudioPlayer.isPlaying() && !played) {
//                Log.d("TAG", "init progress bar");
//                mProgressBar.setProgress(0);
//                mProgressBar.setSecondaryProgress(0);
//            }

                interruptUpdater();
                progressUpdater(this);

                mProgressBar.setTag(mRemaining);
                mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean byUser) {
                        int secondaryPosition = seekBar.getSecondaryProgress();
                        if (progress > secondaryPosition)
                            seekBar.setProgress(secondaryPosition);
                        if (mAudioPlayer.isPlaying() && byUser) {
                            mAudioPlayer.setPosition(seekBar.getProgress() * 1000);
                        }
                        ((TextView) seekBar.getTag()).setText("-" + getDurationString(song.duration - seekBar.getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                mPlayButton.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_media_pause));
                if (interrupted) mPlayButton.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_media_play));
                mProgressBar.setVisibility(View.VISIBLE);
                mRemaining.setText("-" + getDurationString(song.duration));
                mRemaining.setVisibility(View.VISIBLE);
                mDuration.setVisibility(View.INVISIBLE);
            } else {
                mPlayButton.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_media_play));
                mProgressBar.setVisibility(View.INVISIBLE);
                mRemaining.setVisibility(View.INVISIBLE);
                mDuration.setText(getDurationString(song.duration));
                mDuration.setVisibility(View.VISIBLE);
            }

            mPlayButton.setTag(mProgressBar);
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (playingId == song.id) {
                        Log.d("TAG", "playing : " + mAudioPlayer.isPlaying());
                        if (!mAudioPlayer.isPlaying()) {
                            if (played) mAudioPlayer.setPosition(((SeekBar) view.getTag()).getProgress() * 1000);
                            ((ImageButton) view).setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_media_pause));
                        } else {
                            played = true;
                            ((ImageButton) view).setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_media_play));
                        }
                        if (played) {
                            mAudioPlayer.pause();
                        } else {
                            interrupted = true;
                            notifyDataSetChanged();
                        }
                    } else {
                        played = false;
                        playingId = song.id;
                        bufferedProgress = 0;
                        notifyDataSetChanged();
                    }
                }
            });
        }
            
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
//        return super.getView(position, view, parent);

        AudioViewHolder viewHolder = null;
        if (view == null) {
            view = mContext.getLayoutInflater().inflate(R.layout.item_song, parent, false);
            viewHolder = new AudioViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (AudioViewHolder) view.getTag();
        }

        viewHolder.setObj(mAudioList.get(position));
        nextId = (position == getCount() - 1 ? mAudioList.get(0).id : mAudioList.get(position + 1).id);
       
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(mContext, "song: " + song.title + "\nurl: " + song.url, Toast.LENGTH_SHORT).show();
//                Log.d("TAG", "player position : " + (mAudioPlayer.isExist() ? getDurationString(mAudioPlayer.getPosition() / 1000) : "stopped"));
//            }
//        });
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN :
//                        view.setBackgroundColor(Color.GRAY);
//                        break;
//                    case MotionEvent.ACTION_UP :
//                        view.setBackgroundColor(Color.TRANSPARENT);
//                        break;
//                }
//                return false;
//            }
//        });

        return view;
    }

    private String getDurationString(int duration) {
        return duration / 60 + ":" + ((duration % 60) < 10 ? "0" + duration % 60 : duration % 60);
    }

    MediaPlayer.OnBufferingUpdateListener bufferingListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            //code to increase your secondary seekbar
            Log.d("TAG", "buffered percent : " + percent);
            bufferedProgress = percent;
            if (bufferedProgress == 100) {
                mp.setOnBufferingUpdateListener(null);
            }
        }
    };

    private void interruptUpdater() {
        Log.d("TAG", "interrupted " + interrupted + " -- played " + played + " -- isPlaying " + mAudioPlayer.isPlaying());
        if (interrupted) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
                played = true;
                interrupted = false;
            } else {
                Runnable notification = new Runnable() {
                    public void run() {
                        interruptUpdater();
                    }
                };
                handler.postDelayed(notification, 1000);
            }
        }
    }

    private void progressUpdater(final AudioViewHolder currentHolder) {

        if (mAudioPlayer.isExist() && playingId != nextId) {
            if (mAudioPlayer.isPlaying()) {
                currentHolder.mProgressBar.setProgress(mAudioPlayer.getPosition() / 1000);
            } else if (!played) {
                Log.d("TAG", "init progress bar / buffered " + bufferedProgress);
                currentHolder.mProgressBar.setProgress(0);
                currentHolder.mProgressBar.setSecondaryProgress(0);
            }
            currentHolder.mProgressBar.setSecondaryProgress(bufferedProgress * currentHolder.mProgressBar.getMax() / 100);

            Runnable notification = new Runnable() {
                public void run() {
                    progressUpdater(currentHolder);
                }
            };
            handler.postDelayed(notification, 1000);
        }
        else {
//            mAudioPlayer.pause();
            if (playingId < mAudioList.size() && playingId > -1) {
                Log.d("TAG", "INCREASING ID " + playingId + " // nextId = " + nextId);
                Log.d("TAG", "progress " + currentHolder.mProgressBar.getProgress() + " // secondary " + currentHolder.mProgressBar.getSecondaryProgress());
//                currentHolder.mProgressBar.setProgress(0);
//                currentHolder.mProgressBar.setSecondaryProgress(0);
                playingId = nextId;
            } else {
                playingId = -1;
            }
            played = false;
            bufferedProgress = 0;
            notifyDataSetChanged();
        }
    }
}
