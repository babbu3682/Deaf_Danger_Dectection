package com.example.hearinghelper;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

public class TensorFlowTask extends AsyncTask<Void, Void, Integer> {

    private short[] mInputs = null;

    private int mNumberOfLabel = 0;

    private AssetManager mAssetManager = null;

    /*
        생성자

        작업에 필요한 정보들을 이 생성자를 통해 받음
     */
    public TensorFlowTask(
            AssetManager assetManager,  // Assets에 접근하기 위함
            int numberOfLabel,  // label의 갯수
            short[] input) {    // AudioRecord.read를 통해 모았던 로우 데이터

        mInputs = input;
        mAssetManager = assetManager;
        mNumberOfLabel = numberOfLabel;
    }

    /*
        new TensorFlowTask(.....) 하면 위의 위 생성자가 호출됨

        new TensorFlowTask(.....).execute() 하면 위 생성자를 호출한뒤 아래 doInBackground를 호출함
     */
    @Override
    protected Integer doInBackground(Void ... voids) {

        /*
            AudioRecord.read를 통해 모았던 로우 데이터를 0.0 ~ 1.0 사이의 값으로 사상시킴
         */
        float[] floatBuffer = new float[mInputs.length];
        for (int index = 0 ; index < mInputs.length ; ++index) {
            floatBuffer[index] = mInputs[index] / (float) Short.MAX_VALUE;
        }

        /*
            텐서플로우 관련 작업
         */
        TensorFlowInferenceInterface tensorFlowInferenceInterface
                = new TensorFlowInferenceInterface(mAssetManager, App.MODEL_FILENAME);

        tensorFlowInferenceInterface.feed(App.INPUT_SAMPLE_RATE_NAME, new int[] { App.SAMPLE_RATE });
        tensorFlowInferenceInterface.feed(App.INPUT_DATA_NAME, floatBuffer, App.RECORDING_LENGTH, 1);
        tensorFlowInferenceInterface.run(new String[] { App.OUTPUT_NODE_NAME });

        float[] outputScores = new float[mNumberOfLabel];
        tensorFlowInferenceInterface.fetch(App.OUTPUT_NODE_NAME, outputScores);

        int targetIndex = 0;
        float max = Float.MIN_VALUE;
        for (int index = 0 ; index < outputScores.length ; ++index) {
            if (outputScores[index] > max) {
                max = outputScores[index];
                targetIndex = index;
            } else {
                continue;
            }
        }

        tensorFlowInferenceInterface.close();

        return targetIndex;
    }
}
