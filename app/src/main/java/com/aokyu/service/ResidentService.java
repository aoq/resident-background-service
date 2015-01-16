/*
 * Copyright (c) 2015 Yu AOKI
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.aokyu.service;

import com.aokyu.service.setting.Settings;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class ResidentService extends Service {

    private static final String TAG = ResidentService.class.getSimpleName();

    private class Intents {

        private Intents() {}

        public static final String ACTION_START = "com.aokyu.service.intent.action.START";
        public static final String ACTION_STOP = "com.aokyu.service.intent.action.STOP";
    }

    private Context mContext;

    private Settings mSettings;

    private ServiceStateHandler mStateHandler;

    /**
     * Starts the {@link com.aokyu.service.ResidentService}.
     * @param context The application context.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, ResidentService.class);
        intent.setPackage(context.getPackageName());
        intent.setAction(Intents.ACTION_START);
        context.startService(intent);
    }

    /**
     * Stops the {@link com.aokyu.service.ResidentService}.
     * @param context The application context.
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, ResidentService.class);
        intent.setPackage(context.getPackageName());
        intent.setAction(Intents.ACTION_STOP);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mSettings = Settings.getInstance(mContext);
        mStateHandler = new ServiceStateHandler(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            handleIntent(intent);
        } else {
            // The service was restarted by the system.
            mStateHandler.sendStartMessage();
        }

        return Service.START_STICKY;
    }

    /**
     * Handles an {@link android.content.Intent} for this service.
     * @param intent The {@link android.content.Intent} to handle.
     */
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intents.ACTION_START.equals(action)) {
            mStateHandler.sendStartMessage();
        } else if (Intents.ACTION_STOP.equals(action)) {
            mStateHandler.sendStopMessage();
        } else {
            // This service should not work for unknown actions.
            throw new IllegalArgumentException("action not supported");
        }
    }

    @Override
    public void onDestroy() {}

    /**
     * Called when the service started running in the foreground.
     */
    private void onServiceInForeground() {
        mSettings.setServiceEnabled(true);
    }

    /**
     * Called when the service started running in the background.
     */
    private void onServiceInBackground() {
        mSettings.setServiceEnabled(false);
        // This service should not be running in the background.
        stopSelf();
    }

    private static final class ServiceStateHandler {

        private static final int NOTIFICATION_ID = 1;

        private static final int REQUEST_MAIN_ACTIVITY = 0x00000001;

        public static final int MSG_START = 0x00000001;
        public static final int MSG_STOP = 0x00000002;

        private ResidentService mService;

        private boolean mForeground = false;

        private Notification mNotification;

        private Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case MSG_START:
                        startForeground();
                        break;
                    case MSG_STOP:
                        stopForeground();
                        break;
                    default:
                        throw new IllegalArgumentException("invalid message");
                }
            }
        };

        public ServiceStateHandler(ResidentService service) {
            super();
            mService = service;
        }

        public void sendStartMessage() {
            mHandler.sendEmptyMessage(MSG_START);
        }

        public void sendStopMessage() {
            mHandler.sendEmptyMessage(MSG_STOP);
        }

        public synchronized boolean isForeground() {
            return mForeground;
        }

        private synchronized void startForeground() {
            if (!mForeground) {
                Context context = mService.getApplicationContext();
                String title = context.getString(R.string.title_notification);
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        REQUEST_MAIN_ACTIVITY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotification = new Notification.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle(title)
                        .build();
                mService.startForeground(NOTIFICATION_ID, mNotification);
                mForeground = true;
                mService.onServiceInForeground();
            }
        }

        private synchronized void stopForeground() {
            if (mForeground) {
                mService.stopForeground(true);
                mForeground = false;
                mService.onServiceInBackground();
            }
        }
    }
}
