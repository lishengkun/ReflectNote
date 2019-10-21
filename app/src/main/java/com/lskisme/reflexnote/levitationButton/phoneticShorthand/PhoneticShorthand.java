package com.lskisme.reflexnote.levitationButton.phoneticShorthand;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.main.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhoneticShorthand extends AppCompatActivity implements View.OnClickListener
{
    //    控件相关
    private Chronometer record_time;   //录音时长
    //开始录音,取消录音,保存录音,设置录音,录音文件列表
    private Button startRecord,stopRecord;
    private TextView recorde_tip;

    //    录音相关
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private static final String TAG = "PhoneticShorthand";
    //采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
    private static final int SAMPLE_RATE_INHZ = 16000;
    //声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //需要申请的运行时权限
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //被用户拒绝的权限列表
    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecording;
    private AudioRecord audioRecord;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonetic_shorthand);

        //      控件绑定id
        record_time = findViewById(R.id.record_time);
        startRecord = findViewById(R.id.start_record);
        stopRecord = findViewById(R.id.stopRecord);
        recorde_tip = findViewById(R.id.recorde_tip);
        recorde_tip.setText("开始录音");
        record_time = findViewById(R.id.record_time);
        //        按钮点击事件绑定
        startRecord.setOnClickListener(this);
        stopRecord.setOnClickListener(this);
        checkPermissions();
        //        隐藏
        stopRecord.setVisibility(View.GONE);
        //时间超过到了20秒
        record_time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                // 如果从开始计时到现在超过了20s
                if (SystemClock.elapsedRealtime() - record_time.getBase() > 20 * 1000)
                {
                    record_time.stop();
                    record_time.setBase(SystemClock.elapsedRealtime()); //计时器零
                    stopRecord.setVisibility(View.GONE);
                    startRecord.setVisibility(View.VISIBLE);
                    stopRecord();
                    Toast.makeText(PhoneticShorthand.this, "20秒到了", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //    按钮点击逻辑处理
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_record:
                //开始计时
                record_time.setBase(SystemClock.elapsedRealtime()); //计时器清零
                //格式
                int hour = (int) ((SystemClock.elapsedRealtime() - record_time.getBase()) / 1000 / 60);
                record_time.setFormat("0"+String.valueOf(hour)+":%s");
                record_time.start();    //开始计时
                //控制显隐
                recorde_tip.setText("结束录音");
                startRecord.setVisibility(View.GONE);
                stopRecord.setVisibility(View.VISIBLE);
                //开始录音
                startRecord();
                break;
            case R.id.stopRecord:
                record_time.stop();
                record_time.setBase(SystemClock.elapsedRealtime()); //计时器零
                stopRecord.setVisibility(View.GONE);
                startRecord.setVisibility(View.VISIBLE);
                stopRecord();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(PhoneticShorthand.this, MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        recorde_tip.setText("开始录音");
    }

    //停止录音,释放资源
    public void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        Intent intent = new Intent(PhoneticShorthand.this, PhoneticShorthandResult.class);
        startActivity(intent);
    }

    //开始录音
    public void startRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        final File dir = new File(getExternalFilesDir(null).getPath()+"/voice/", "disposable.pcm");
        if (!dir.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (dir.exists()) {
            dir.delete();
        }

        audioRecord.startRecording();
        isRecording = true;

        // TODO: 2018/3/10 pcm数据无法直接播放，保存为WAV格式。

        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(dir);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "run: close file output stream !");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //  申请权限
    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }

    //运行时权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");
                }
            }
        }
    }
}