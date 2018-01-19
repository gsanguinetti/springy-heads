package com.flipkart.springyheads.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationFactory.showNotification(context, "Hello!", "Based on your recent transaction history...");
            }
        }, TimeUnit.SECONDS.toMillis(20));
    }
}
