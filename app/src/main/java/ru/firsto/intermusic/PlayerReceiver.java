package ru.firsto.intermusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by razor on 06.09.15.
 */
public class PlayerReceiver extends ResultReceiver {
    public static final String ACTION = PlayerReceiver.class.getSimpleName() + ".broadcast";

    private static final int NOTIFY_ID = 3;

    private Context mContext;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Notification notification;

    private Intent notificationIntent;
    private PendingIntent notificationPendingIntent;

    public PlayerReceiver(Handler handler, Context context) {
        super(handler);
        mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        builder = new NotificationCompat.Builder(mContext);

        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(BitmapFactory.decodeResource(res, android.R.drawable.ic_media_play))
                .setTicker("Сейчас играет...")
                .setAutoCancel(true);

        notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.putExtra("notificationId", NOTIFY_ID);
        notificationPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notificationPendingIntent);

//        android.support.v4.app.NotificationCompat.Action action = new android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", notificationPendingIntent);
//        builder.addAction(action);
//        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", notificationPendingIntent);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == PlayerService.UPDATE_PROGRESS) {
            String title = resultData.getString("title");
            String artist = resultData.getString("artist");
            int duration = resultData.getInt("duration");
            int progress = resultData.getInt("progress");

            builder.setContentTitle(title);
            builder.setContentText(artist);
            Log.d("TAG", "playerreceiver onRecieveResult progress = " + progress);

            if (progress < 1) {
                builder.setProgress(0, 0, true);
            } else {
                builder.setProgress(duration, progress, false);
            }

//            notification = builder.build();

            Notification notification = new Notification(android.R.drawable.ic_media_play, null, System.currentTimeMillis());

            RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view);
            notificationView.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_pause);
            notificationView.setTextViewText(R.id.textView1, title);

            notification.contentView = notificationView;
            notification.contentIntent = notificationPendingIntent;

            Intent switchIntent = new Intent("ACTION_PLAY");
            switchIntent.putExtra("not", notification);
            PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(mContext, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationView.setOnClickPendingIntent(R.id.play_pause, pendingSwitchIntent);

            notificationManager.notify(NOTIFY_ID, notification);
            if (progress == duration) {
                Log.d("TAG", "progress ends");
                notificationManager.cancel(NOTIFY_ID);
            }
        }
    }

    public static class RemoteControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.notification_view);

            if(intent.getAction().equalsIgnoreCase("ACTION_PLAY")){
                if(AudioPlayer.get().isPlaying() && AudioPlayer.get().getPosition() > 10000){
                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_play);
                }
                else {
                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_pause);
                }
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ID, (Notification) intent.getParcelableExtra("not"));
            }
        }
    }
}
