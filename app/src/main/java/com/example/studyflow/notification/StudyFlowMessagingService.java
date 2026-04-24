package com.example.studyflow.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.studyflow.R;
import com.example.studyflow.ui.main.MainActivity;

/**
 * Nhận push notification từ Firebase Cloud Messaging.
 * Server gửi notification khi deadline gần tới.
 */
public class StudyFlowMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "fcm_deadlines";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Lấy data từ FCM message
        String title = "StudyFlow";
        String body  = "Bạn có deadline sắp tới!";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        } else if (!remoteMessage.getData().isEmpty()) {
            // Nếu dùng data payload thay notification payload
            title = remoteMessage.getData().getOrDefault("title", title);
            body  = remoteMessage.getData().getOrDefault("body", body);
        }

        showNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Lưu FCM token vào Firestore để server có thể gửi
        // notification trực tiếp tới user
        saveTokenToFirestore(token);
    }

    private void saveTokenToFirestore(String token) {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update("fcmToken", token);
    }

    private void showNotification(String title, String body) {
        createChannel();

        // Nhấn notification → mở MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở deadline",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Thông báo từ server khi deadline gần tới");
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}