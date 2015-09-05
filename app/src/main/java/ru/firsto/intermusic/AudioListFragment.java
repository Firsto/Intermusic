package ru.firsto.intermusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        if (getActivity() instanceof IAudioList) {
            mAudioList = ((IAudioList) getActivity()).getList();
        }

        mAudioPlayer = new AudioPlayer();
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
}
