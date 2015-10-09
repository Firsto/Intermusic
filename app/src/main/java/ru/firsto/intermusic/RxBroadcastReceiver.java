package ru.firsto.intermusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * RxBroadcastReceiver.java https://gist.github.com/pwittchen/2ecb66aca44e80a5e0eb
 */

public final class RxBroadcastReceiver implements OnSubscribe<IntentAggregator> {
    private Context context;
    private IntentFilter filter;
    private BroadcastReceiver receiver;

    public RxBroadcastReceiver(Context context, IntentFilter filter) {
        this.context = context;
        this.filter = filter;
    }

    @Override
    public void call(final Subscriber<? super IntentAggregator> subscriber) {
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                subscriber.onNext(new IntentAggregator(context, intent));
            }

        };
        context.registerReceiver(receiver, filter);
    }

    public void unregister() {
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
    }
}