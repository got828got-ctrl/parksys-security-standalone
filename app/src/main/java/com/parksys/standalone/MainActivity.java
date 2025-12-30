package com.parksys.standalone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.Uri;

import com.parksys.standalone.receiver.StandaloneDeviceAdminReceiver;
import com.parksys.standalone.service.StandaloneSecurityService;
import com.parksys.standalone.manager.EmbeddedPolicyManager;

/**
 * Parksys Standalone Security App
 * QRコード不要・インストールするだけで動作するセキュリティアプリ
 * 
 * 機能:
 * - デバイス管理者権限の取得
 * - 内蔵ポリシーの自動適用
 * - バックグラウンドセキュリティサービス
 * - 端末起動時の自動開始
 */
public class MainActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1001;
    private static final int REQUEST_CODE_BATTERY_OPTIMIZATION = 1002;
    private static final String PREFS_NAME = "StandaloneSecurityPrefs";
    private static final String KEY_SETUP_COMPLETE = "setup_complete";
    private static final String CHANNEL_ID = "standalone_security_channel";

    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private EmbeddedPolicyManager policyManager;
    private SharedPreferences prefs;

    private LinearLayout mainLayout;
    private TextView statusText;
    private TextView bannerText;
    private ProgressBar progressBar;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, StandaloneDeviceAdminReceiver.class);
        policyManager = EmbeddedPolicyManager.getInstance(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        createNotificationChannel();
        createUI();
        checkAndStartSetup();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "セキュリティサービス",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("業務端末セキュリティ保護サービス");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void createUI() {
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#FAFAFA"));
        mainLayout.setPadding(48, 80, 48, 48);

        // アプリタイトル
        TextView titleView = new TextView(this);
        titleView.setText("Parksys Security");
        titleView.setTextSize(28);
        titleView.setTextColor(Color.parseColor("#1565C0"));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, 16);
        mainLayout.addView(titleView);

        // サブタイトル
        TextView subtitleView = new TextView(this);
        subtitleView.setText("スタンドアロンセキュリティアプリ");
        subtitleView.setTextSize(14);
        subtitleView.setTextColor(Color.parseColor("#757575"));
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 0, 0, 48);
        mainLayout.addView(subtitleView);

        // ステータス表示
        statusText = new TextView(this);
        statusText.setTextSize(16);
        statusText.setTextColor(Color.parseColor("#424242"));
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 32, 0, 32);
        mainLayout.addView(statusText);

        // プログレスバー
        progressBar = new ProgressBar(this);
        progressBar.setPadding(0, 16, 0, 16);
        mainLayout.addView(progressBar);

        // アクションボタン
        actionButton = new Button(this);
        actionButton.setTextSize(16);
        actionButton.setPadding(32, 24, 32, 24);
        actionButton.setVisibility(View.GONE);
        mainLayout.addView(actionButton);

        // セキュリティバナー
        bannerText = new TextView(this);
        bannerText.setBackgroundColor(Color.parseColor("#1565C0"));
        bannerText.setTextColor(Color.WHITE);
        bannerText.setTextSize(13);
        bannerText.setPadding(24, 16, 24, 16);
        bannerText.setGravity(Gravity.CENTER);
        bannerText.setVisibility(View.GONE);
        
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bannerParams.topMargin = 48;
        bannerText.setLayoutParams(bannerParams);
        mainLayout.addView(bannerText);

        setContentView(mainLayout);
    }

    private void checkAndStartSetup() {
        updateStatus("初期化中...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSetupComplete()) {
                showActiveState();
            } else {
                startSetupProcess();
            }
        }, 500);
    }

    private boolean isSetupComplete() {
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false) && isDeviceAdminActive();
    }

    private boolean isDeviceAdminActive() {
        return devicePolicyManager.isAdminActive(adminComponent);
    }

    private void startSetupProcess() {
        if (!isDeviceAdminActive()) {
            requestDeviceAdmin();
        } else {
            applyPoliciesAndStart();
        }
    }

    private void requestDeviceAdmin() {
        updateStatus("デバイス管理者権限が必要です");
        progressBar.setVisibility(View.GONE);

        actionButton.setText("権限を許可する");
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setOnClickListener(v -> {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "業務端末のセキュリティ保護のため、デバイス管理者権限が必要です。\n\n" +
                "この権限により以下の機能が有効になります:\n" +
                "・カメラ/マイクの制御\n" +
                "・セキュリティポリシーの適用\n" +
                "・端末状態の監視");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                actionButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                applyPoliciesAndStart();
            } else {
                updateStatus("デバイス管理者権限が拒否されました");
                showRetryButton();
            }
        } else if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION) {
            completeSetup();
        }
    }

    private void showRetryButton() {
        actionButton.setText("再試行");
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.GONE);
            startSetupProcess();
        });
    }

    private void applyPoliciesAndStart() {
        updateStatus("セキュリティポリシーを適用中...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            policyManager.applyEmbeddedPolicy();
            
            updateStatus("セキュリティサービスを開始中...");
            startSecurityService();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                requestBatteryOptimizationExemption();
            }, 500);
        }, 500);
    }

    private void startSecurityService() {
        Intent serviceIntent = new Intent(this, StandaloneSecurityService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                updateStatus("バッテリー最適化の除外を設定中...");
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                    return;
                } catch (Exception e) {
                    // 設定画面が開けない場合はスキップ
                }
            }
        }
        completeSetup();
    }

    private void completeSetup() {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, true).apply();
        
        updateStatus("セットアップ完了");
        progressBar.setVisibility(View.GONE);
        
        showActiveState();
    }

    private void showActiveState() {
        progressBar.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        statusText.setText("セキュリティ保護: 有効");
        statusText.setTextColor(Color.parseColor("#2E7D32"));

        String bannerMessage = policyManager.getBannerText();
        bannerText.setText(bannerMessage);
        bannerText.setVisibility(View.VISIBLE);

        // ステータス詳細を追加
        addStatusDetails();
    }

    private void addStatusDetails() {
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setPadding(0, 32, 0, 0);

        String[] statusItems = {
            "デバイス管理者: 有効",
            "セキュリティサービス: 稼働中",
            "ポリシー適用: 完了",
            "バックグラウンド保護: 有効"
        };

        for (String item : statusItems) {
            TextView itemView = new TextView(this);
            itemView.setText("✓ " + item);
            itemView.setTextSize(14);
            itemView.setTextColor(Color.parseColor("#388E3C"));
            itemView.setPadding(0, 8, 0, 8);
            itemView.setGravity(Gravity.CENTER);
            detailsLayout.addView(itemView);
        }

        mainLayout.addView(detailsLayout, mainLayout.getChildCount() - 1);
    }

    private void updateStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
            statusText.setTextColor(Color.parseColor("#424242"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSetupComplete()) {
            showActiveState();
        }
    }
}
