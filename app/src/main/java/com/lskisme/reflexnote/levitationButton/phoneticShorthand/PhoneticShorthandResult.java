package com.lskisme.reflexnote.levitationButton.phoneticShorthand;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.asr.AsrConstants;
import com.huawei.hiai.asr.AsrError;
import com.huawei.hiai.asr.AsrListener;
import com.huawei.hiai.asr.AsrRecognizer;
import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.utils.PCMAudioPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PhoneticShorthandResult extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PhoneticShorthandResult";
    private final static int INIT_ENGINE = 0;
    private final static int TEST_FINISH = 1;
    private static final int NEXT_FILE_TEST = 2;
    private static final int DELAYED_SATRT_RECORD = 3;
    private static final int DELAYED_WRITE_PCM = 4;

    private int timerCount =0;
    //    自建文件夹     /storage/emulated/0/Android/data/包名/files/voice
    private String TEST_FILE_PATH = "/storage/emulated/0/Android/data/com.lskisme.reflexnote/files/voice/";


    // voice file test
    private View fileTestLy;
//    private Button testDefaultFile;
    private LinearLayout fileTestResultLL;
    private TextView showFileTestResult;
    private static TextView detect_tip_voice;
    private TextView fileTestEndTest;
    private Button copyText,backBtn,edit_detected_result;


    private boolean isVoiceFileTest = true;

    private AsrRecognizer mAsrRecognizer;
    private MyHandler mHandler;
    private String mResult = null;
    private int fileTotalCount;
    private int count = 0;
    private long startTime;
    private long endTime;
    private long waitTime;
    private List<String> pathList = new ArrayList<>();
    private List<String> resultList = new ArrayList<>();
    private List<String> writePcmList = new ArrayList<>();
    private MyAsrListener mMyAsrListener = new MyAsrListener();

    //延时提示识别失败,请重新录入可识别语音
    private Thread myThread;
    private boolean isExit = false;
    public PhoneticShorthandResult() {
    }

    private View check_view;
    private Button startPlayBtn,stopPlayBtn,copyBtn;

    private EditText check_text;
    private String checkText = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonetic_shorthand_result);

        mHandler = new MyHandler(PhoneticShorthandResult.this);
        StoragePermission.getAllPermission(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (!isSupportAsr()) {
            Toast.makeText(this, "not support asr!", Toast.LENGTH_SHORT).show();
        } else {
            initView();
        }

        startTest("");
    }
    //是否支持语音识别
    private boolean isSupportAsr() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo("com.huawei.hiai", 0);
            if (packageInfo.versionCode <= 801000300) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void initView() {

        // test file view
        fileTestLy = findViewById(R.id.file_test_ly);
//        testDefaultFile = findViewById(R.id.test_default_file);
//        testDefaultFile.setOnClickListener(this);
        backBtn = findViewById(R.id.backBtn_phonetic_shorthand);
        backBtn.setOnClickListener(this);
        showFileTestResult = (TextView) findViewById(R.id.show_result);
        fileTestResultLL = (LinearLayout) findViewById(R.id.test_result_ll);
        fileTestEndTest = (TextView) findViewById(R.id.end_test);
        copyText = findViewById(R.id.copy_voice);
        copyText.setOnClickListener(this);
        edit_detected_result = findViewById(R.id.edit_detected_result);
        edit_detected_result.setOnClickListener(this);
        copyBtn = findViewById(R.id.copy_voice2);
        copyBtn.setOnClickListener(this);
        check_view = findViewById(R.id.check_layout);
        startPlayBtn = findViewById(R.id.start_play_btn);
        startPlayBtn.setOnClickListener(this);
        stopPlayBtn = findViewById(R.id.stop_play_btn);
        stopPlayBtn.setOnClickListener(this);
        check_text = findViewById(R.id.voice_check_text);
        check_view.setVisibility(View.GONE);
        stopPlayBtn.setVisibility(View.GONE);
        detect_tip_voice =findViewById(R.id.detect_tip_voice);
        detect_tip_voice.setText("正在识别中...");
    }
    @Override
    protected void onPause() {
        super.onPause();
        cancelListening();
        setBtEnabled(true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyEngine();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copy_voice:
                if (mResult!=null&&mResult.length()!=0){
                    //获取剪贴板管理器
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String voiceText = mResult;
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("VoiceText", voiceText);
                    // 将ClipData内容放到系统剪贴板里
                    clipboardManager.setPrimaryClip(mClipData);
                    Toast.makeText(PhoneticShorthandResult.this, "复制成功!", Toast.LENGTH_SHORT).show();
                    Log.d("copy", ""+voiceText);
                }else{
                    Toast.makeText(this, "没有内容可复制!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.copy_voice2:
                if (check_text.getText().toString()!=null&&check_text.getText().toString().length()!=0){
                    //获取剪贴板管理器
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String voiceText = check_text.getText().toString();
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("VoiceText1", voiceText);
                    // 将ClipData内容放到系统剪贴板里
                    clipboardManager.setPrimaryClip(mClipData);
                    Toast.makeText(PhoneticShorthandResult.this, "复制成功!", Toast.LENGTH_SHORT).show();
                    Log.d("copy", ""+voiceText);
                }else{
                    Toast.makeText(this, "没有内容可复制!", Toast.LENGTH_SHORT).show();
                }
                break;
                //更正识别结果
            case R.id.edit_detected_result:
                if (checkText.length()!=0){
                    //显示View
                    fileTestResultLL.setVisibility(View.GONE);
                    check_view.setVisibility(View.VISIBLE);
                    showFileTestResult.setVisibility(View.GONE);
                    copyText.setVisibility(View.GONE);
                    //点击更正，把识别结果显示到EditText
                    check_text.setText(checkText);
                }else {
                    Toast.makeText(this, "还未能得到语音识别结果，无需更正", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.start_play_btn:
                startPlayBtn.setVisibility(View.GONE);
                stopPlayBtn.setVisibility(View.VISIBLE);
                Toast.makeText(this, "开始播放", Toast.LENGTH_SHORT).show();
                PCMAudioPlayer.getInstance().startPlay(getExternalFilesDir(null).getPath()+"/voice/"+"disposable.pcm");
                break;
            case R.id.stop_play_btn:
                stopPlayBtn.setVisibility(View.GONE);
                startPlayBtn.setVisibility(View.VISIBLE);
                Toast.makeText(this, "停止播放", Toast.LENGTH_SHORT).show();
                PCMAudioPlayer.getInstance().stopPlay();
                break;
            case R.id.backBtn_phonetic_shorthand:
                finish();
                task.cancel();
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed() : finish()");
        finish();
    }

    //销毁引擎
    private void destroyEngine() {
        Log.d(TAG, "destroyEngine() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.destroy();
            mAsrRecognizer = null;
        }
    }
    //初始化引擎
    private void handleInitEngine() {
        if (pathList.size() <= 0) {
            Toast.makeText(PhoneticShorthandResult.this, "请返回,录入语音文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        initEngine(true);
        setBtEnabled(false);
        Log.d(TAG, "handleMessage: " + count + " path :" + pathList.get(count));
        startListening(true, pathList.get(count));

    }

    public void setBtEnabled(boolean isEnabled) {
//        testDefaultFile.setEnabled(isEnabled);
    }


    private void startTest(String filePath) {
        pathList.clear();
        fileTestResultLL.setVisibility(View.GONE);
        if (pathList.size() > 0) {
            showFileTestResult.setText("识别中...");
        }
        if (TextUtils.isEmpty(filePath)) {
            fileTotalCount = getFilePath(TEST_FILE_PATH);
        } else {
            fileTotalCount = 1;
            pathList.add(filePath);
        }
        if (mAsrRecognizer != null) {
            mAsrRecognizer.destroy();
        }
        mHandler.sendEmptyMessage(INIT_ENGINE);
        Timer timer = new Timer();
        timer.schedule(task,8000);//10秒后执行TimeTask的run方法
    }
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            //要执行的操作,检测是否识别完毕
            if (!detect_tip_voice.getText().equals("识别结果如下")){  //没有识别完毕
                Looper.prepare();
                Toast.makeText(PhoneticShorthandResult.this, "检测到语音识别时间较长，若无识别结果，请返回重新录入语音", Toast.LENGTH_LONG).show();
                Log.d("tip",detect_tip_voice.getText().toString());
                Looper.loop();
            }
        }
    };
    //初始化引擎
    private void initEngine(boolean isFile) {
        mAsrRecognizer = AsrRecognizer.createAsrRecognizer(this);
        Intent initIntent = new Intent();
        if (isFile) {
            initIntent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, AsrConstants.ASR_SRC_TYPE_FILE);
        } else {
            initIntent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, AsrConstants.ASR_SRC_TYPE_RECORD);
        }
        if (mAsrRecognizer != null) {
            mAsrRecognizer.init(initIntent, mMyAsrListener);
        }
    }

    private void startListening(boolean isFile, String filePath) {
        Log.d(TAG, "startListening() " + "isFile:" + isFile);
        if (count == 0) {
            startTime = getTimeMillis();
        }
        Intent intent = new Intent();
        intent.putExtra(AsrConstants.ASR_VAD_FRONT_WAIT_MS, 4000);
        intent.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, 3000);
        intent.putExtra(AsrConstants.ASR_TIMEOUT_THRESHOLD_MS, 20000);
        if (isFile) {
            Log.d(TAG, "startListening() :filePath= " + filePath);
            intent.putExtra(AsrConstants.ASR_SRC_FILE, filePath);
        }
        if (mAsrRecognizer != null) {
            mAsrRecognizer.startListening(intent);
        }
    }


    private void stopListening() {
        Log.d(TAG, "stopListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.stopListening();
        }
    }

    private void cancelListening() {
        Log.d(TAG, "cancelListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.cancel();
        }
    }

    /**
     * 通过递归得到当前文件夹里所有的文件数量和路径
     *
     * @param path
     * @return
     */
    public int getFilePath(String path) {
        int sum = 0;
        Log.i(TAG, "getFilePath()" + path);
        try {
            File file = new File(path);
            File[] list = file.listFiles();
            if (list == null) {
                Log.i(TAG, "getFilePath: fileList is null!");
                return 0;
            }
            for (int i = 0; i < list.length; i++) {
                Log.i(TAG, "getFilePath: file name" + list[i].getName());
                if (list[i].isFile()) {
                    String[] splitPATH = list[i].toString().split("\\.");
                    if (splitPATH[splitPATH.length - 1].equals("pcm") ||
                            splitPATH[splitPATH.length - 1].equals("wav")) {
                        sum++;
                        pathList.add(list[i].toString());
                        writePcmList.add(list[i].toString());
                    }
                } else {
                    sum += getFilePath(list[i].getPath());
                }
            }
        } catch (NullPointerException ne) {
            Toast.makeText(this, "找不到指定路径！", Toast.LENGTH_SHORT).show();
        }
        return sum;
    }

    public long getTimeMillis() {
        long time = System.currentTimeMillis();
        return time;
    }

    private class MyHandler extends Handler {

        private final WeakReference<PhoneticShorthandResult> mActivity;

        public MyHandler(PhoneticShorthandResult activity) {
            mActivity = new WeakReference<PhoneticShorthandResult>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PhoneticShorthandResult activity = mActivity.get();
            if (activity != null) {
                Log.d(TAG, "handleMessage: " + activity.mResult);
                switch (msg.what) {
                    case INIT_ENGINE:
                        activity.handleInitEngine();
                        break;
                    case TEST_FINISH:
                        activity.fileTestResultLL.setVisibility(View.VISIBLE);
                        activity.fileTestEndTest.setText(activity.waitTime + "毫秒");
                        activity.setBtEnabled(true);
                        activity.resultList.clear();
                        activity.destroyEngine();
                        activity.detect_tip_voice.setText("识别结果如下");
                        break;
                    case DELAYED_SATRT_RECORD:
                        if (!activity.isVoiceFileTest) {
                            activity.startListening(false, null);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class MyAsrListener implements AsrListener {
        @Override
        public void onInit(Bundle params) {
            Log.d(TAG, "onInit() called with: params = [" + params + "]");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech() called");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged() called with: rmsdB = [" + rmsdB + "]");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived() called with: buffer = [" + buffer + "]");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");


        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "onError() called with: error = [" + error + "]");
            if (error == AsrError.ERROR_SERVER_INSUFFICIENT_PERMISSIONS) {
                if (mAsrRecognizer != null) {
                    mAsrRecognizer.startPermissionRequestForEngine();
                }
            }
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "onResults() called with: results = [" + results + "]");
            endTime = getTimeMillis();
            waitTime = endTime - startTime;
            mResult = getOnResult(results, AsrConstants.RESULTS_RECOGNITION);
            stopListening();
            if (isVoiceFileTest) {
                resultList.add(pathList.get(count) + "\t" + mResult);
                Log.d(TAG, "isAutoTest: " + waitTime + "count :" + count);
                if (count == fileTotalCount - 1) {
                    resultList.add("\n\nwaittime:\t" + waitTime + "ms");
                    mHandler.sendEmptyMessage(TEST_FINISH);
                    Log.d(TAG, "waitTime: " + waitTime + "count :" + count);
                    count = 0;
                } else {
                    Log.d(TAG, "isAutoTest: else" + waitTime + "count :" + count);
                    count += 1;
                    mHandler.sendEmptyMessageDelayed(NEXT_FILE_TEST, 1000);
                }
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults() called with: partialResults = [" + partialResults + "]");
            getOnResult(partialResults, AsrConstants.RESULTS_PARTIAL);
        }

        @Override
        public void onEnd() {

        }

        private String getOnResult(Bundle partialResults, String key) {
//            Log.d(TAG, "getOnResult() called with: getOnResult = [" + partialResults + "]");
            String json = partialResults.getString(key);
            final StringBuffer sb = new StringBuffer();
            try {
                JSONObject result = new JSONObject(json);
                JSONArray items = result.getJSONArray("result");
                for (int i = 0; i < items.length(); i++) {
                    String word = items.getJSONObject(i).getString("word");
                    double confidences = items.getJSONObject(i).getDouble("confidence");
                    sb.append(word);
//                    Log.d(TAG, "asr_engine: result str " + word);
//                    Log.d(TAG, "asr_engine: confidence " + String.valueOf(confidences));
                }
//                Log.d(TAG, "getOnResult: " + sb.toString());
                if (isVoiceFileTest) {
                    showFileTestResult.setText(sb.toString());
                    checkText = sb.toString();
                } else {
//                    Log.d(TAG, "getOnResult: " + sb.toString());
                }
            } catch (JSONException exp) {
                Log.w(TAG, "JSONException: " + exp.toString());
            }
            return sb.toString();
        }


        @Override
        public void onEvent(int eventType, Bundle params) {
//            Log.d(TAG, "onEvent() called with: eventType = [" + eventType + "], params = [" + params + "]");
        }
        @Override
        public void onLexiconUpdated(String s, int i) {

        }
    }
}
