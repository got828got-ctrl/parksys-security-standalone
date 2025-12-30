package com.parksys.standalone.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.parksys.standalone.service.StandaloneSecurityService;
import com.parksys.standalone.manager.EmbeddedPolicyManager;

/**
 * デバイス管理者レシーバー
 * Device Admin権限の有効化/無効化を処理
 */
public class StandaloneDeviceAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "StandaloneAdmin";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.i(TAG, "Device Admin enabled");

        // ポリシーを即座に適用
        EmbeddedPolicyManager.getInstance(context).applyEmbeddedPolicy();

        // セキュリティサービスを開始
        Intent serviceIntent = new Intent(context, StandaloneSecurityService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        Toast.makeText(context, "セキュリティ保護が有効になりました", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.w(TAG, "Device Admin disable requested");
        return "セキュリティ保護を無効にすると、業務端末としての保護機能が停止します。\n\n管理者の許可なく無効化することはできません。";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.w(TAG, "Device Admin disabled");
        Toast.makeText(context, "セキュリティ保護が無効になりました", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Log.i(TAG, "Password changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Log.w(TAG, "Password attempt failed");
    }
}
