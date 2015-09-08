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

/**
 * Created by razor on 06.09.15.
 */
public class PlayerReceiver extends ResultReceiver {
    public static final String ACTION_PLAYER = PlayerReceiver.class.getSimpleName() + ".broadcast";

    private static final int NOTIFY_ID = 3;

    private Context mContext;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Notification notification;

    private Intent switchIntent;
    private PendingIntent switchPengdingIntent;
    private Intent notificationIntent;
    private PendingIntent notificationPendingIntent;

    private int lastProgress = -1;

    static android.support.v4.app.NotificationCompat.Action actionPause;
    android.support.v4.app.NotificationCompat.Action actionPlay;

    public PlayerReceiver(Handler handler, Context context) {
        super(handler);
        mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        lastProgress = -1;

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

        switchIntent = new Intent(ACTION_PLAYER);
        switchPengdingIntent = PendingIntent.getBroadcast(mContext, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        actionPause = new android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", switchPengdingIntent);
        builder.addAction(actionPause);


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

//            Notification notification = new Notification(android.R.drawable.ic_media_play, null, System.currentTimeMillis());
//
//            RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view);
////            notificationView.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_pause);
//            notificationView.setTextViewText(R.id.textView1, title);
//
//            notification.contentView = notificationView;
//            notification.contentIntent = notificationPendingIntent;
//
//            Intent switchIntent = new Intent("ACTION_PLAY");
//            switchIntent.putExtra("not", notification);
//            PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(mContext, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            android.support.v4.app.NotificationCompat.Action action = new android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingSwitchIntent);
//            builder.addAction(action);

//
//            notificationView.setOnClickPendingIntent(R.id.play_pause, pendingSwitchIntent);

            notification = builder.build();
            switchIntent.putExtra("notification", notification);
            switchPengdingIntent = PendingIntent.getBroadcast(mContext, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            actionPause = new android.support.v4.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", switchPengdingIntent);
//            builder.mActions.clear();
//            builder.addAction(actionPause);

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

//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_view);

            if (intent.getAction().equals(ACTION_PLAYER)) {
                if (AudioPlayer.get().isPlaying()) {
//                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_play);
                    actionPause.icon = android.R.drawable.ic_media_play;
                    actionPause.title = "Play";
                } else {
//                    remoteViews.setImageViewResource(R.id.play_pause, android.R.drawable.ic_media_pause);
                    actionPause.icon = android.R.drawable.ic_media_pause;
                    actionPause.title = "Pause";
                }
                AudioPlayer.get().pause();
                Log.d("TAG", "(Notification) intent.getParcelableExtra(\"notification\") == null : " + (((Notification) intent.getParcelableExtra("notification")) == null));
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ID, (Notification) intent.getParcelableExtra("notification"));
            }
        }
    }
}
