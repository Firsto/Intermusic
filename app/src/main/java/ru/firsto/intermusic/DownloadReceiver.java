package ru.firsto.intermusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
 * Created by razor on 31.08.15.
 */
public class DownloadReceiver extends ResultReceiver {
    public static final String ACTION = DownloadReceiver.class.getSimpleName() + ".broadcast";

    private static final int NOTIFY_ID = 2;

    private Context mContext;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Notification notification;

    private Intent cancelIntent;
    private PendingIntent cancelPendingIntent;

    public DownloadReceiver(Handler handler, Context context) {
        super(handler);
        mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        builder = new NotificationCompat.Builder(mContext);

        builder.setSmallIcon(android.R.drawable.ic_menu_save)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, android.R.drawable.ic_menu_save))
                        //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Скачивание файла!")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                        //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("Скачивание файла")
                        //.setContentText(res.getString(R.string.notifytext))
                .setContentText("Скачивание файла"); // Текст уведомления

        //Create an Intent for the BroadcastReceiver
        cancelIntent = new Intent(ACTION);
        cancelIntent.putExtra("notificationId", NOTIFY_ID);
        cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, 0);

        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", cancelPendingIntent);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            int progress = resultData.getInt("progress");

            Log.d("TAG", "onRecieveResult progress = " + progress);

            if (progress < 3) {
                builder.setProgress(0, 0, true);
            } else {
                builder.setProgress(100, progress, false);
            }

            notification = builder.build();

            notificationManager.notify(NOTIFY_ID, notification);
            if (progress == 100) {
                Log.d("TAG", "progress 100");
                notificationManager.cancel(NOTIFY_ID);
            }
        }
    }
}
