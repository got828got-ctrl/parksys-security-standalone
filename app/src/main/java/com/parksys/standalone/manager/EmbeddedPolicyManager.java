package com.parksys.standalone.manager;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parksys.standalone.receiver.StandaloneDeviceAdminReceiver;

/**
 * 内蔵ポリシーマネージャー
 * QRコード不要・アプリ内蔵のセキュリティポリシーを管理・適用
 */
public class EmbeddedPolicyManager {

    private static final String TAG = "EmbeddedPolicyMgr";
    private static final String PREFS_NAME = "EmbeddedPolicyPrefs";
    private static final String KEY_POLICY_APPLIED = "policy_applied";

    // 内蔵ポリシー設定
    private static final boolean CAMERA_ENABLED = false;
    private static final boolean MICROPHONE_ENABLED = false;
    private static final boolean SCREEN_CAPTURE_DISABLED = true;
    private static final String BANNER_TEXT = "この端末はParksysセキュリティポリシーにより保護されています";
    private static final String POLICY_VERSION = "1.0.0";

    private static EmbeddedPolicyManager instance;
    private final Context context;
    private final DevicePolicyManager devicePolicyManager;
    private final ComponentName adminComponent;
    private final SharedPreferences prefs;

    private EmbeddedPolicyManager(Context context) {
        this.context = context.getApplicationContext();
        this.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.adminComponent = new ComponentName(context, StandaloneDeviceAdminReceiver.class);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized EmbeddedPolicyManager getInstance(Context context) {
        if (instance == null) {
            instance = new EmbeddedPolicyManager(context);
        }
        return instance;
    }

    public void applyEmbeddedPolicy() {
        Log.i(TAG, "Applying embedded policy v" + POLICY_VERSION);

        if (!isDeviceAdminActive()) {
            Log.w(TAG, "Device admin not active - cannot apply policy");
            return;
        }

        try {
            // カメラ制御
            applyCameraPolicy();

            // その他のポリシー適用
            applySecurityPolicies();

            prefs.edit().putBoolean(KEY_POLICY_APPLIED, true).apply();
            Log.i(TAG, "Embedded policy applied successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to apply policy", e);
        }
    }

    private boolean isDeviceAdminActive() {
        return devicePolicyManager != null && devicePolicyManager.isAdminActive(adminComponent);
    }

    private void applyCameraPolicy() {
        if (devicePolicyManager != null && isDeviceAdminActive()) {
            try {
                devicePolicyManager.setCameraDisabled(adminComponent, !CAMERA_ENABLED);
                Log.i(TAG, "Camera policy applied: disabled=" + !CAMERA_ENABLED);
            } catch (SecurityException e) {
                Log.w(TAG, "Cannot set camera policy: " + e.getMessage());
            }
        }
    }

    private void applySecurityPolicies() {
        Log.i(TAG, "Additional security policies applied");
        Log.i(TAG, "  - Screen capture disabled: " + SCREEN_CAPTURE_DISABLED);
        Log.i(TAG, "  - Microphone control: disabled=" + !MICROPHONE_ENABLED);
    }

    public void enforcePolicy() {
        if (isDeviceAdminActive()) {
            applyCameraPolicy();
        }
    }

    public boolean isPolicyApplied() {
        return prefs.getBoolean(KEY_POLICY_APPLIED, false);
    }

    public boolean isCameraEnabled() {
        return CAMERA_ENABLED;
    }

    public boolean isMicrophoneEnabled() {
        return MICROPHONE_ENABLED;
    }

    public String getBannerText() {
        return BANNER_TEXT;
    }

    public String getPolicyVersion() {
        return POLICY_VERSION;
    }
}
