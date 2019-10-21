package com.lskisme.reflexnote.levitationButton.keepGood;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hiai.vision.image.detector.AestheticsScoreDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.AestheticsScore;

import org.json.JSONException;
import org.json.JSONObject;

public class ASTask extends AsyncTask<Bitmap, Void, Float> {
    private static final String LOG_TAG = "aesthetics_score_demo";
    private MMListener listener;
    private long startTime;
    private long endTime;

    public static AestheticsScoreDetector aestheticsScoreDetector;
    public ASTask(MMListener listener) {
        this.listener = listener;
    }

    @Override
    protected Float doInBackground(Bitmap... bmp) {
        aestheticsScoreDetector = new AestheticsScoreDetector((Context)listener);
        startTime = System.currentTimeMillis();
        float result_score = getScore(bmp[0]);
        endTime = System.currentTimeMillis();
        return result_score;
    }

    @Override
    protected void onPostExecute(Float resultScore) {
        listener.onTaskCompleted(resultScore);
        //release engine after detect finished
        aestheticsScoreDetector.release();
        super.onPostExecute(resultScore);
    }
    public  float getScore(Bitmap bitmap) {
        if (bitmap == null) {
            return -1;
        }
        Frame frame = new Frame();
        frame.setBitmap(bitmap);
        JSONObject jsonObject = aestheticsScoreDetector.detect(frame,null);
        Log.d(LOG_TAG,"json result is " + jsonObject.toString());
        float score = -1;
        if(jsonObject!=null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                AestheticsScore aestheticsScores = aestheticsScoreDetector.convertResult(jsonObject);
                score = aestheticsScores.getScore();
            }else{
                try {
                    String aestheticsScore = jsonObject.getString("aestheticsScore");
                    if(!TextUtils.isEmpty(aestheticsScore)){
                        JSONObject object = new JSONObject(aestheticsScore);
                        score = (float) object.getDouble("score");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return score;
    }
}