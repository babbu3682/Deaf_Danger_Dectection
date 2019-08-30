package com.example.hearinghelper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.example.hearinghelper.databinding.ActivityMainBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements PermissionListener {

    private static final String LABEL_FILENAME = "file:///android_asset/sunggu_conv_labels.txt";

    private static final int WHAT_UPDATE_RESULT = 0;

    private boolean mIsListening = false;
    private ExecutorService mListeningThread = null;

    private AudioRecord mAudioRecord = null;

    private ArrayList<String> mLabels = null;

    private boolean mIsPermissionGranted = false;

    private ActivityMainBinding mActivityMainBinding = null;

    /*
        onResume 이후 호출돼는 메서드

        1. 앱을 처음 켰을때만 호출된다(onResume -> onCreate)

        즉
        2. 앱이 켜져있다가 홈버튼을 눌러 홈화면으로 이동했다가 -> 앱 아이콘을 눌러 다시 키거나
                                                                -> 홈버튼 좌측의 최근 앱을 눌러 다시 켰을때
        3. 앱에서 다른 액티비티로 이동했다가 돌아올때

        위 2, 3의 경우에는 호출 안됨
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            상단에 있는 ActionBar를 제거한다
         */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        /*
            Vibrate와 RecordAudio에 대한 권한사용을 사용자에게 요청한다

            사용 수락시 onPermissionGranted로,
            사용 거절시 onPermissionDenied가 호출된다

            또한
            ㄱ. 이미 수락 했다면 대화상자가 뜨지않고 즉시 onPermissionGranted로 이동하며
            ㄴ. 아직 수락하지 않았거나, 이전에 거절했었다면 대화상자를 띄워주고 사용자의 선택에 따라
                onPermissionGranted 혹은 onPermissionDenied가 로 이동한다
         */
        TedPermission
                .with(this)
                .setPermissionListener(this)
                .setDeniedTitle(R.string.app_name)
                .setDeniedMessage(R.string.notice_permission)
                .setPermissions(
                        Manifest.permission.VIBRATE,
                        Manifest.permission.RECORD_AUDIO)
                .check();
    }

    /*
        기본적으로 onResume은 앱 화면이 꺼져있다가(가려져있다가) 다시 보일때 호출돼는 메서드이다

        이러한 시나리오는
        1. 앱을 처음 켰을때
        2. 앱이 켜져있다가 홈버튼을 눌러 홈화면으로 이동했다가 -> 앱 아이콘을 눌러 다시 키거나
                                                               -> 홈버튼 좌측의 최근 앱을 눌러 다시 켰을때
        3. 앱에서 다른 액티비티로 이동했다가 돌아올때
        호출된다
     */
    @Override
    protected void onResume() {
        super.onResume();

        /*
            앱을 맨 처음 켜면 onResume이 호출 될것이다
            하지만 이때는
            ㄱ. 아직 권한이 있는지 없는지도 모르고
            ㄴ. conv_labels.txt도 로드하지 않았으므로
            아무곳도 하지 않고 넘어가야 한다

            사용자가 권한을 수락 했다면 onPermissionGranted 이후 onResume이 다시 호출돼는데
            (눈에는 보이지 않지만 권한 관련 Activity가 켜졌다 꺼짐)
            (따라서 3번 케이스 (앱에서 다른 액티비티로 이동했다가 돌아올때) 이므로 onResume이 호출돼는거)

            이때는 onPermissionGranted에서 mIsPermissionGranted를 true로 바꿔줬기 떄문에
         */
        if (mIsPermissionGranted) {

            /*
                이 라인을 수행한다
             */
            startListeningThread();
        }
    }

    /*
        1. 백버튼을 누르거나
        2. 홈화면을 누르면
        호출된다

        즉 우리는 포그라운드에서 작동하는 시나리오를 짰기 때문에
        앱이 화면에서 보이지 않는 순간, 바로 청취/분석을 중단한다
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopListeningThread();
    }

    /*
        사용자가 권한사용을 수락 했다면 호출됨

        호출 흐름을 보면
        onResume() -> onCreate() -> TedPermission.....check -> 허용시 onPermissionGranted()  -> mIsPermissionGranted true로 변경 -> onResume() -> startListeningThread()
                                                            -> 거부시 onPermissionDenied()   -> 앱 종료
     */
    @Override
    public void onPermissionGranted() {

        /*
            res/layout/activity_main.xml 뷰를
            이 activity(MainActivity.java에) 바인딩시킨다(붙인다)

            이제부터 MainActivity를 켜면 activity_main.xml에서 정의한 뷰들이 화면에 그려진다
         */
        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mActivityMainBinding.setMainActivity(this);

        if (loadLabels()) {
            /*
                assets/conv_labels.txt파일을 로드하는데 성공하면
             */
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            /*
                mIsPermissionGranted를 true로 변경 해줌

                이 바로 다음에 onResume이 호출돼는데, 거기서 mIsPermissionGranted 이게 true일 경우에만
                Listening을 Thread 시작함(startCa
             */
            mIsPermissionGranted = true;
        } else {
            /*
                assets/conv_labels.txt파일을 로드하는데 실패하면
             */

            /*
                실패했다는 메시지를 띄우고 앱을 종료함
             */
            Toast.makeText(this, getString(R.string.notice_cant_load_labels), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*
        사용자가 권한사용을 거부하면 바로 앱을 종료시킴
     */
    @Override
    public void onPermissionDenied(List<String> deniedPermissions) {
        finish();
    }

    /*
        핸들러는 독립적인 Thread나 AsyncTask에서 MainThread에 접근하기 위하여 사용한다

        아래와 같이 new Handler로 생성해준뒤 handleMessage를 오버라이딩 해주고
        파라메터로 넘어오는 Message의 케이스에 따라 해야할일들을 정의 해주면 된다
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_UPDATE_RESULT:
                    switch (msg.arg1) {

                        /*
                            car_horn일 때는
                         */
                        case 3:
                        case 9:
                            /*
                                진동을 울려줌
                             */
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(2000,255));
                            } else {
                                vibrator.vibrate(2000);
                            }

                            /*
                                위험 이미지가 그려진 AppCompatImageView의 alpha값을

                                1초간 1.0에서 0.0으로 변경시키는 애니매이션을 동작한다
                             */
                            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mActivityMainBinding.appCompatImageViewWarning, View.ALPHA,1.0f, 0.0f);
                            objectAnimator.setInterpolator(new AccelerateInterpolator());
                            objectAnimator.setDuration(1500);
                            objectAnimator.start();

                            /*
                                헤드업 알람을 보여줌
                             */
                            showNotification();
                            break;

                        /*
                            이외의 경우에는 아무것도 하지 않음
                         */
                        default:
                            break;
                    }

                    /*
                        모든 경우에 대해 textView를 업데이트 해줌
                     */
                    mActivityMainBinding.appCompatTextViewCondition.setText("Detecting...");
                    mActivityMainBinding.appCompatTextViewLabel.setText(mLabels.get(msg.arg1));
                    break;
            }
        }
    };

    /*
        헤드업 알람을 생성해줌
     */
    private void showNotification() {

        NotificationCompat.Builder builder = null;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /*
            Android Oreo(SDK Version 26) 부터는 헤드업 알람을 위하여
            채널이 필요하므로 버전에 따라 구분 해준다

            https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=ko
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            /*
                Oreo 이상 버전,

                NotificationCompat.Builder의 생성자에 App.onCreate에서 생성했던 Channel의 id를 넣어줌
             */
            builder =
                    new NotificationCompat.Builder(MainActivity.this, App.NOTIFICATON_CHANNEL_ID)
                            .setSmallIcon(R.drawable.sharp_notification_important_24)   // 아이콘
                            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT))  // 눌렀을때 알람 사라지게
                            .setContentTitle("Detection Dangerous")  // 제목
                            .setContentText("위험!! 주위를 둘러보세요!");    // 내용
        } else {
            /*
                Oreo 미만 버전,

                NotificationCompat.Builder의 생성자에 App.onCreate에서 생성했던 Channel의 id를 넣어줄 필요가 없음
                (App.onCreate 에서 channel을 생성하지도 않음)
             */
            builder = new NotificationCompat.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.sharp_notification_important_24)   // 아이콘
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT))  // 눌렀을때 알람 사라지게
                    .setContentTitle("Detection Dangerous")  // 제목
                    .setContentText("위험!! 주위를 둘러보세요!");    // 내용
        }

        /*
            위에서 버전에 따라 만든 builder를 통해 헤드업 알림을 생성하고 보여줌
         */
        notificationManager.notify(0, builder.build());
    }

    /*
        Assets에 있는 conv_labels.txt파일을 로드하여
        mLabels ArrayList<String>에 저장한다

        성공시(파일 로드 성공했으며, 내용도 있다면) true를 리턴하고
        실패시(파일 로드에 실패했거나, 성공했어도 내용이 비어있다면) false를 리턴함

        이게 성공해야지만 앱이 계속 진행됨
     */
    private boolean loadLabels() {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            getAssets().open(LABEL_FILENAME.split("file:///android_asset/")[1])));
        } catch (IOException e) {
            return false;
        }

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (mLabels == null) {
                    mLabels = new ArrayList<>();
                }

                mLabels.add(line);
            }
        } catch (IOException e) {

        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return mLabels.size() > 0;
        }
    }

    /*
        가장 중요한 청취 -> 텐서플로우 AsyncTask를 생성해주는 부분이다

        큰 플로우는
        Listening Thread는 mIsListening 가 true인 동안 무한으로

        1. AudioRecord.read를 통해 microphone으로부터 소리를 받아와
        2. AsyncTask인 TensorFlowTask를 통해 분석해라고 던져주고
        3. 스스로는 다시 1번으로 돌아가 청취를 재시작하는

        와 같다
     */
    private void startListeningThread() {
        if (!mIsListening) {

            /*
                청취 Thread를 생성 해줌
             */
            mListeningThread = Executors.newSingleThreadExecutor();

            /*
                청취 Thread를 시작 해줌

                해야 할을은 생성 해줄떄 넘겨준 Runnable의 run메서드를 오버라이드 하여 정의함
             */
            mListeningThread.execute(new Runnable() {
                @Override
                public void run() {

                    /*
                        버퍼 크기를 계산함
                     */
                    int bufferSize = AudioRecord.getMinBufferSize(
                            App.SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

                    /*
                        Audio Record를 생성함
                     */
                    mAudioRecord = new AudioRecord(
                            MediaRecorder.AudioSource.DEFAULT,
                            App.SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize);

                    switch (mAudioRecord.getState()) {
                        /*
                            AudioRecord 생성 실패시 청취 쓰레드 즉시 종료함
                         */
                        default:
                            break;

                        /*
                            AudioRecord 생성 성공시
                         */
                        case AudioRecord.STATE_INITIALIZED:

                            /*
                                청취 Thread가 돌고있다는 플래그인 mIsListening를 true로 셋 해주고
                             */
                            mIsListening = true;
                            mAudioRecord.startRecording();

                            /*
                                stopListeningThread를 통해 mIsListening가 false 될때까지 계속 돌면서
                             */
                            while (mIsListening) {
                                int lengthOfResult = 0;
                                long timeStamp = System.currentTimeMillis();

                                short[] buffer = new short[bufferSize / 2];
                                short[] result = new short[App.RECORDING_LENGTH];

                                /*
                                    App.RECORDING_LENGTH만큼 읽어올떄까지 계속
                                 */
                                while (mIsListening && (lengthOfResult < App.RECORDING_LENGTH)) {

                                    /*
                                        AudioRecord의 read를 이용해 마이크에서 소리를 읽어옴
                                     */
                                    int lengthOfReading = mAudioRecord.read(buffer, 0, buffer.length);

                                    System.arraycopy(
                                            buffer, 0,
                                            result, lengthOfResult,
                                            lengthOfReading);

                                    lengthOfResult += lengthOfReading;
                                }

                                /*
                                    다 읽었고, 그 사이에 stopListeningThread가 호출이 안됐다면
                                 */
                                if (mIsListening) {
                                    /*
                                        작업을 독립적으로 수행하는 ThnsorFlowTask로 읽어들인 데이터를 보내 분석을 요청하고
                                     */
                                    new TensorFlowTask(getAssets(), mLabels.size(), result) {
                                        @Override
                                        protected void onPostExecute(Integer indexOfLabel) {
                                            if (mIsListening) {
                                                mHandler.obtainMessage(WHAT_UPDATE_RESULT, indexOfLabel, 0).sendToTarget();
                                            }
                                        }
                                    }.execute();

                                    /*
                                        스스로는 다시 407라인으로 이동하여 청취를 계속함
                                     */
                                }
                            }

                            mAudioRecord.stop();
                            break;
                    }

                    mAudioRecord.release();
                    mAudioRecord = null;
                    mListeningThread = null;
                }
            });

            mListeningThread.shutdown();
        }
    }

    /*
        Listening Thread는 mIsListening 가 true인 동안 무한으로

        1. AudioRecord.read를 통해 microphone으로부터 소리를 받아와
        2. AsyncTask인 TensorFlowTask를 통해 분석해라고 던져주고
        3. 스스로는 다시 1번으로 돌아가 청취를 재시작하는

        과정을 거친다.


        이 루프를 정상적으로 멈추는 방법은 mIsListening 를 false로 클리어 해주는것임
     */
    private void stopListeningThread() {
        mIsListening = false;
    }
}