package com.parksys.standalone.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.parksys.standalone.MainActivity;
import com.parksys.standalone.manager.EmbeddedPolicyManager;

/**
 * スタンドアロンセキュリティサービス
 * バックグラウンドで常駐し、セキュリティポリシーを継続的に適用
 */
public class StandaloneSecurityService extends Service {

    private static final String TAG = "StandaloneSecurity";
    private static final int NOTIFICATION_ID = 2001;
    private static final String CHANNEL_ID = "standalone_security_fg";
    private static final long MONITOR_INTERVAL = 60 * 1000; // 1分

    private Handler handler;
    private EmbeddedPolicyManager policyManager;
    private boolean isRunning = false;

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                performSecurityCheck();
                handler.postDelayed(this, MONITOR_INTERVAL);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Standalone Security Service created");

        handler = new Handler(Looper.getMainLooper());
        policyManager = EmbeddedPolicyManager.getInstance(this);

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Standalone Security Service starting");

        startForeground(NOTIFICATION_ID, createNotification());

        if (!isRunning) {
            isRunning = true;
            policyManager.applyEmbeddedPolicy();
            handler.post(monitorRunnable);
            Log.i(TAG, "Security monitoring started");
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "セキュリティ保護",
                NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("業務端末セキュリティ保護サービス");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
            .setContentTitle("セキュリティ保護")
            .setContentText(policyManager.getBannerText())
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void performSecurityCheck() {
        Log.d(TAG, "Performing security check...");
        
        // ポリシーの再適用（設定が変更されていないか確認）
        policyManager.enforcePolicy();
        
        // 端末状態のログ
        logDeviceState();
    }

    private void logDeviceState() {
        Log.d(TAG, "Device state check: OK");
        Log.d(TAG, "  - Policy enforced: " + policyManager.isPolicyApplied());
        Log.d(TAG, "  - Camera disabled: " + !policyManager.isCameraEnabled());
        Log.d(TAG, "  - Service running: " + isRunning);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Security Service destroyed - attempting restart");
        isRunning = false;
        handler.removeCallbacks(monitorRunnable);

        // サービスが停止された場合、再起動を試みる
        Intent restartIntent = new Intent(this, StandaloneSecurityService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
