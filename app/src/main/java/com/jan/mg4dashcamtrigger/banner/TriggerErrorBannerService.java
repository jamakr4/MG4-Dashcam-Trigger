package com.jan.mg4dashcamtrigger.banner;

import com.jan.mg4dashcamtrigger.R;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public final class TriggerErrorBannerService extends Service {
    private static final String CHANNEL_ID = "mg4_dashcam_trigger_error_banner";
    private static final int NOTIF_ID = 201;
    private static final long AUTO_HIDE_MS = 4000L;
    private static final String EXTRA_TITLE_RES_ID = "title_res_id";
    private static final String EXTRA_SUBTITLE_RES_ID = "subtitle_res_id";
    private static final String EXTRA_NOTIFICATION_TEXT_RES_ID = "notification_text_res_id";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideRunnable = this::stopSelf;

    private WindowManager windowManager;
    private View overlayView;
    private TextView titleView;
    private TextView subtitleView;

    public static void showError(
            Context context,
            int titleResId,
            int subtitleResId,
            int notificationTextResId) {
        Intent intent = new Intent(context, TriggerErrorBannerService.class);
        intent.putExtra(EXTRA_TITLE_RES_ID, titleResId);
        intent.putExtra(EXTRA_SUBTITLE_RES_ID, subtitleResId);
        intent.putExtra(EXTRA_NOTIFICATION_TEXT_RES_ID, notificationTextResId);
        context.startForegroundService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int titleResId = intent != null
                ? intent.getIntExtra(EXTRA_TITLE_RES_ID, R.string.trigger_error_title)
                : R.string.trigger_error_title;
        int subtitleResId = intent != null
                ? intent.getIntExtra(EXTRA_SUBTITLE_RES_ID, R.string.trigger_error_generic)
                : R.string.trigger_error_generic;
        int notificationTextResId = intent != null
                ? intent.getIntExtra(
                        EXTRA_NOTIFICATION_TEXT_RES_ID, R.string.notification_trigger_error_generic)
                : R.string.notification_trigger_error_generic;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(notificationTextResId))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(NOTIF_ID, notification);

        if (overlayView == null && !showOverlayWindow()) {
            Toast.makeText(this, subtitleResId, Toast.LENGTH_LONG).show();
        }
        bindText(titleResId, subtitleResId);
        playWarningTone();
        mainHandler.removeCallbacks(hideRunnable);
        mainHandler.postDelayed(hideRunnable, AUTO_HIDE_MS);
        return START_NOT_STICKY;
    }

    private boolean showOverlayWindow() {
        if (windowManager == null) {
            return false;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = dpToPx(32);

        try {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_trigger_error_banner, null, false);
            titleView = overlayView.findViewById(R.id.tvTriggerBannerTitle);
            subtitleView = overlayView.findViewById(R.id.tvTriggerBannerSubtitle);
            windowManager.addView(overlayView, params);
            return true;
        } catch (Throwable ignored) {
            overlayView = null;
            titleView = null;
            subtitleView = null;
            return false;
        }
    }

    private void bindText(int titleResId, int subtitleResId) {
        if (titleView != null) {
            titleView.setText(titleResId);
        }
        if (subtitleView != null) {
            subtitleView.setText(subtitleResId);
        }
    }

    private void playWarningTone() {
        try {
            // AAOS routes audio strictly by usage/zone via CarAudioService and silently drops
            // streams without explicit AudioAttributes + a granted AudioFocusRequest.
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(focusChange -> { })
                    .build();
            if (audioManager != null) {
                audioManager.requestAudioFocus(focusRequest);
            }

            MediaPlayer player = new MediaPlayer();
            player.setAudioAttributes(attributes);
            try (AssetFileDescriptor afd = getResources()
                    .openRawResourceFd(R.raw.notification_sound_7062_henrycena82595)) {
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }
            player.setVolume(1f, 1f);
            player.setOnCompletionListener(mp -> releasePlayer(mp, audioManager, focusRequest));
            player.setOnErrorListener((mp, what, extra) -> {
                releasePlayer(mp, audioManager, focusRequest);
                return true;
            });
            player.prepare();
            player.start();
        } catch (Throwable ignored) {
        }
    }

    private void releasePlayer(MediaPlayer player, AudioManager audioManager, AudioFocusRequest focusRequest) {
        player.reset();
        player.release();
        if (audioManager != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        mainHandler.removeCallbacks(hideRunnable);
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
        overlayView = null;
        titleView = null;
        subtitleView = null;
        stopForeground(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_trigger_error_banner),
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
