package com.flipkart.springyheads.demo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendNotification(context);
            }
        }, TimeUnit.SECONDS.toMillis(20));
    }

    public void sendNotification(Context context) {

        Intent intent = new Intent(context, FloatingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.chathead)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.chathead))
                        .setContentTitle("Szia!")
                        .setContentText("Van számodra egy szuper ajánlatom!")
                        .setAutoCancel(true)
                        //.setDeleteIntent(PendingIntent.getBroadcast(context, 0, new Intent(context, MyReceiver.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT))
                        .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));


        // Gets an instance of the NotificationManager service//

        NotificationManager notificationManager =

                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        notificationManager.notify(1, mBuilder.build());
    }
}
