package ru.firsto.intermusic;

import android.content.Context;
import android.content.Intent;

/**
 * https://gist.github.com/kolipass/26577777fff9910ccb05
 */
public class IntentAggregator {
    private Context context;
    private Intent intent;

    public IntentAggregator(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    public Context getContext() {
        return context;
    }

    public Intent getIntent() {
        return intent;
    }
}