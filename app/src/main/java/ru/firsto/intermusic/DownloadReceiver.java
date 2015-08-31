package ru.firsto.intermusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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

    private NotificationCompat.Builder builder;
    private Context mContext;

    public DownloadReceiver(Handler handler, Context context) {
        super(handler);
        mContext = context;

        Resources res = context.getResources();
        builder = new NotificationCompat.Builder(context);

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

    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            int progress = resultData.getInt("progress");

            Log.d("TAG", "onRecieveResult progress = " + progress);

            builder.setProgress(100, progress, false);

            Notification notification = builder.build();

            NotificationManager notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notification);
//                mProgressDialog.setProgress(progress);
            if (progress == 100) {
                Log.d("TAG", "progress 100");
                notificationManager.cancel(2);
//                    mProgressDialog.dismiss();
            }
        }
    }
}
