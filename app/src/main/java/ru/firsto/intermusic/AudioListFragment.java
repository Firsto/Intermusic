package ru.firsto.intermusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by razor on 14.08.15.
 */
public class AudioListFragment extends Fragment {

    public static final String TAG = "audiolist";

    private List<Song> mAudioList;
    private AudioPlayer mAudioPlayer;

    private RecyclerView mListAudio;
    private AudioListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAudioPlayer = AudioPlayer.get();
        if (getActivity() instanceof IAudioList) {
            mAudioList = ((IAudioList) getActivity()).getList();
        }
    }

    @Override
    public void onResume() {
        getActivity().registerReceiver(controlReceiver, new IntentFilter(PlayerReceiver.ACTION_PLAYER));
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(controlReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.list_song, container, false);

        mListAudio = (RecyclerView) view.findViewById(R.id.listSong);
        mListAudio.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mListAudio.setLayoutManager(mLayoutManager);

        mAdapter = new AudioListAdapter(getActivity(), mAudioList, mAudioPlayer);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d("TAG", "DataOberver says that something was changed!");
            }
        });
        mListAudio.setAdapter(mAdapter);

        return view;
    }

    private void releasePlayer() {
        if (mAudioPlayer.isExist()) {
            try {
                mAudioPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface IAudioList {
        List<Song> getList();
    }

    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_view);

            if (intent.getAction().equals(PlayerReceiver.ACTION_PLAYER)) {
                // TODO: action button update
//                if (AudioPlayer.get().isPlaying()) {
////                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_play);
//                    actionPause.icon = android.R.drawable.ic_media_play;
//                    actionPause.title = "Play";
//                } else {
////                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_pause);
//                    actionPause.icon = android.R.drawable.ic_media_pause;
//                    actionPause.title = "Pause";
//                }
                int position = intent.getIntExtra("position", -1);
                if (position >= 0) {
                    mListAudio.getAdapter().notifyItemChanged(position);
                    Log.d("TAG", " -- position " + position + " // " + mAdapter.getItemViewType(position));
                }
                AudioPlayer.get().pause();
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(PlayerReceiver.NOTIFY_ID, (Notification) intent.getParcelableExtra("notification"));
            }
        }
    };
}
