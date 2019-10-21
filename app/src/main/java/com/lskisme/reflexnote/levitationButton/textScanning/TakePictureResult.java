package com.lskisme.reflexnote.levitationButton.textScanning;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.text.TextDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.text.Text;
import com.huawei.hiai.vision.visionkit.text.TextConfiguration;
import com.huawei.hiai.vision.visionkit.text.TextDetectType;
import com.lskisme.reflexnote.R;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
/*识别结果显示*/
public class TakePictureResult extends AppCompatActivity {
    private Button backBtn_take_picture;
/******/
    private static final String TAG = "TakePictureResult";
    private static final int TYPE_CHOOSE_PHOTO = 1;
    private static final int TYPE_SHOW_RESULE = 2;
    private Object mWaitResult = new Object();
    private Bitmap mBitmap;
    private Button mBtnStartDetect,check_text_value;
    private ImageView mImageView,show_img;
    private TextView mTxtViewResult,title_ocr,detect_tip;
    private TextDetector mTextDetector;
    private Button copy_ocr_text,copy_ocr_text2;
    private EditText show_text_edit;
    private boolean isDetected;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_picture_result);
        initView();
        int picFrom = getIntent().getIntExtra("PicFrom",0);
        if (picFrom==10){
            //获取相机拍照图片
            String path = getIntent().getStringExtra("picpath");
            try {
                FileInputStream fis = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                mImageView.setImageBitmap(bitmap);
                show_img.setImageBitmap(bitmap);
                mBitmap = BitmapFactory.decodeFile(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if(picFrom==20){
            //获取图库图片
            String picPath = getIntent().getStringExtra("picturePath");
            mBitmap = BitmapFactory.decodeFile(picPath);
            mHander.sendEmptyMessage(TYPE_CHOOSE_PHOTO);
        }


        /* To connect vision service */
        VisionBase.init(getApplicationContext(), new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i(TAG, "onServiceConnect.");
            }
            @Override
            public void onServiceDisconnect() {
                Log.i(TAG, "onServiceDisconnect.");
            }
        });
        mThread.start();

        //ocr按钮
        mBtnStartDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetect();
                check_text_value.setVisibility(View.VISIBLE);
            }
        });
        //返回按钮
        backBtn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent TextScanning = new Intent(TakePictureResult.this, com.lskisme.reflexnote.levitationButton.textScanning.TextScanning.class);
                startActivity(TextScanning);
            }
        });
        copy_ocr_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String ocrText = mTxtViewResult.getText().toString();
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("OcrText", ocrText);
                // 将ClipData内容放到系统剪贴板里。
                clipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(TakePictureResult.this, "复制成功!", Toast.LENGTH_SHORT).show();
            }
        });
        copy_ocr_text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String ocrText = show_text_edit.getText().toString();
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("OcrText", ocrText);
                // 将ClipData内容放到系统剪贴板里。
                clipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(TakePictureResult.this, "复制成功!", Toast.LENGTH_SHORT).show();
            }
        });
//        更正结果按钮
        check_text_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDetected==true){
                    //显示图片
                    show_img.setVisibility(View.VISIBLE);
                    copy_ocr_text.setVisibility(View.GONE);
                    detect_tip.setVisibility(View.VISIBLE);
                    copy_ocr_text2.setVisibility(View.VISIBLE);
                    //显隐文本
                    mTxtViewResult.setVisibility(View.GONE);
                    show_text_edit.setVisibility(View.VISIBLE);
                    //把识别结果复制到EditText中供用户修改编辑
                    show_text_edit.setText(mTxtViewResult.getText().toString());
                }else {
                    Toast.makeText(TakePictureResult.this, "未能识别内容，无需更正！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initView() {
        mImageView = findViewById(R.id.take_picture_result);    //图片源
        show_img = findViewById(R.id.show_img); //图片
        show_img.setVisibility(View.GONE);
        mTxtViewResult = findViewById(R.id.show_text);      //识别结果
        mBtnStartDetect = findViewById(R.id.startOcr);      //开始识别
        backBtn_take_picture = findViewById(R.id.backBtn_take_picture);     //返回
        show_text_edit = findViewById(R.id.show_text_edit);
        show_text_edit.setVisibility(View.GONE);//隐藏修改文本

        check_text_value = findViewById(R.id.check_text_value);
        check_text_value.setVisibility(View.GONE);


        title_ocr = findViewById(R.id.title_ocr);
        detect_tip = findViewById(R.id.detect_tip);
        copy_ocr_text = findViewById(R.id.copy_ocr_text);   //复制
        copy_ocr_text2 = findViewById(R.id.copy_ocr_text2);
        copy_ocr_text2.setVisibility(View.GONE);
        detect_tip.setVisibility(View.GONE);

        isShowView(true);
        isDetected = true;
    }
    public void isShowView(Boolean show){
        if (show){  //图片显示
            mImageView.setVisibility(View.VISIBLE);
            mBtnStartDetect.setVisibility(View.VISIBLE);

            detect_tip.setVisibility(View.GONE);
            mTxtViewResult.setVisibility(View.GONE);
            copy_ocr_text.setVisibility(View.GONE);
        }else{  //识别结果
            mImageView.setVisibility(View.GONE);
            mBtnStartDetect.setVisibility(View.GONE);

            title_ocr.setVisibility(View.VISIBLE);
            mTxtViewResult.setVisibility(View.VISIBLE);
            copy_ocr_text.setVisibility(View.VISIBLE);

        }
    }
    //    释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* release ocr instance and free the npu resources*/
        if (mTextDetector != null) {
            mTextDetector.release();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent TextScanning = new Intent(TakePictureResult.this, com.lskisme.reflexnote.levitationButton.textScanning.TextScanning.class);
        startActivity(TextScanning);
    }

    public Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            Log.d(TAG, "handleMessage status = " + status);
            switch (status) {
                case TYPE_CHOOSE_PHOTO: {   //选择的图片
                    if (mBitmap == null) {
                        Toast.makeText(TakePictureResult.this, "bitmap is null !!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mImageView.setImageBitmap(mBitmap);
                    show_img.setImageBitmap(mBitmap);
                    break;
                }
                case TYPE_SHOW_RESULE: {        //显示结果
                    Text result = (Text) msg.obj;
                    if (result == null) {
                        mTxtViewResult.setText("Failed to detect text lines, result is null.");
                        isDetected = false;
                        break;
                    }
//                    Log.d(TAG, "OCR Detection succeeded.");
                    mTxtViewResult.setText(" " + result.getValue());
                    break;
                }
                default:
                    break;
            }
        }
    };

    //开始识别
    private void startDetect() {
        isShowView(false);
        mTxtViewResult.setText("正在识别...");
        synchronized (mWaitResult) {
            mWaitResult.notifyAll();
        }

    }

    public Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (true) {
                try {
                    synchronized (mWaitResult) {
                        mWaitResult.wait();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
                Log.d(TAG, "start to detect ocr.");
                mTextDetector = new TextDetector(TakePictureResult.this);
                TextDetector textDetector = mTextDetector;
                /* create frame and set images*/
                Frame frame = new Frame();
                frame.setBitmap(mBitmap);

                /* create a TextDetector instance firstly */

                /* create a TextConfiguration instance here, */
                TextConfiguration config = new TextConfiguration();
                /* and set the EngineType as focus shoot ocr */
                config.setEngineType(TextDetectType.TYPE_TEXT_DETECT_FOCUS_SHOOT);
                textDetector.setTextConfiguration(config);

                /* start to detect and get the json object, which can be analyzed as Text */
                JSONObject jsonObject = textDetector.detect(frame, null);
                Log.d(TAG, "end to detect ocr. json: " + jsonObject.toString());  /*jsonObject never be null*/
                /* analyze the result */
                Text text = textDetector.convertResult(jsonObject);
                Log.d(TAG, "convert result.");
                /* do something follow your heart*/
                Message msg = new Message();
                msg.what = TYPE_SHOW_RESULE;
                msg.obj = text;
                mHander.sendMessage(msg);
                textDetector.release();
            }
        }
    });
}
