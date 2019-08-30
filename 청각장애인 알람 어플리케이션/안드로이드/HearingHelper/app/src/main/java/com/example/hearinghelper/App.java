package com.example.hearinghelper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public class App extends Application {

    public static final int SAMPLE_RATE = 16000;
    public static final int RECORDING_LENGTH = SAMPLE_RATE * 1;

    public static final String MODEL_FILENAME = "file:///android_asset/sunggu_frozen_graph.pb";
    public static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    public static final String INPUT_SAMPLE_RATE_NAME = "decoded_sample_data:1";
    public static final String OUTPUT_NODE_NAME = "labels_softmax";

    public static final String NOTIFICATON_CHANNEL_ID = "HH_NOTI_ID";
    public static final String NOTIFICATON_CHANNEL_NAME = "HH_NOTI_NAME";
    public static final String NOTIFICATON_CHANNEL_DESCRIPTION = "HH_NOTI_DESC";

    @Override
    public void onCreate() {
        super.onCreate();

        /*
            Android Oreo(SDK Version 26) 부터는 헤드업 알람을 위하여 채널이 필요하다

            https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=ko
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            /*
                채널생성은 앱의 첫 실행시에 만들어 두면 된다

                앱이 한번이라도 실행 됐었다면 이미 채널이 생성돼어있어
                notificationManager.getNotificationChannel(NOTIFICATON_CHANNEL_ID); 의 리턴이 != null이 나올테지만

                첫 실행이라면 notificationManager.getNotificationChannel(NOTIFICATON_CHANNEL_ID); 의 리턴이 null이므로
             */
            NotificationChannel channel = notificationManager.getNotificationChannel(NOTIFICATON_CHANNEL_ID);

            /*
                요기서 채널을 만들어 준다
             */
            if (channel == null) {
                channel = new NotificationChannel(
                        NOTIFICATON_CHANNEL_ID,
                        NOTIFICATON_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);

                channel.setDescription(NOTIFICATON_CHANNEL_DESCRIPTION);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
