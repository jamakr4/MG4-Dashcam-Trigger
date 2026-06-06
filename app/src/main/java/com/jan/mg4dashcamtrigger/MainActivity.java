package com.jan.mg4dashcamtrigger;

import com.jan.mg4dashcamtrigger.banner.TriggerErrorBannerService;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_TRIGGER_DASHCAM_EVENT =
            "com.drivehub.kamera.action.TRIGGER_DASHCAM_EVENT";
    private static final String TARGET_PACKAGE = "com.drivehub.kamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendDashcamTrigger();
        finish();
        overridePendingTransition(0, 0);
    }

    private void sendDashcamTrigger() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(TARGET_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            TriggerErrorBannerService.showError(
                    this,
                    R.string.trigger_error_title,
                    R.string.trigger_error_target_missing,
                    R.string.notification_trigger_error_target_missing);
            return;
        }

        Intent intent = new Intent(ACTION_TRIGGER_DASHCAM_EVENT);
        intent.setPackage(TARGET_PACKAGE);
        List<ResolveInfo> receivers = pm.queryBroadcastReceivers(intent, 0);
        if (receivers == null || receivers.isEmpty()) {
            TriggerErrorBannerService.showError(
                    this,
                    R.string.trigger_error_title,
                    R.string.trigger_error_receiver_missing,
                    R.string.notification_trigger_error_receiver_missing);
            return;
        }
        sendBroadcast(intent);
    }
}
