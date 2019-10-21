package com.lskisme.reflexnote.levitationButton.keepGood;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.MainActivity;
import com.lskisme.reflexnote.utils.AspectRatio;
import com.lskisme.reflexnote.utils.FileOperationUtils;
import com.lskisme.reflexnote.utils.OverCameraView;
import com.lskisme.reflexnote.utils.Size;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;

/**传感器拍照上传评分,显示分数,如果点击拍照停止定时器  错误
 * 自动聚焦完成拍照,传入,显示分数,点击拍照按钮,拍照存入手机,点击相册查看
 * 点击录音，再点击结束，或者时间结束，结束后直接跳转界面识别
 */
public class KeepGood extends AppCompatActivity implements View.OnClickListener, SensorEventListener,MMListener {
    /*view控件*/
    private Button flashLight,takePhoto;
    private TextView showScore;
    /*相机相关*/
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreview;
//    /*预览尺寸集合*/
//    private final SizeMap mPreviewSizes = new SizeMap();
//    /*图片尺寸集合*/
//    private final SizeMap mPictureSizes = new SizeMap();
    /*屏幕旋转显示角度*/
    private int mDisplayOrientation;
    /*设备屏宽比*/
    private AspectRatio mAspectRatio;
    /*聚焦视图*/
    private OverCameraView mOverCameraView;
    /*聚焦状态*/
    private boolean isFoucing;
    /*Hander*/
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    /*拍照状态*/
    private boolean isTakePhoto;
    /*闪光灯状态*/
    private boolean isFlashing;
    /*是否存在Camera实例*/
    private boolean isHaveCamera;
    /*是否存储拍照的照片*/
    private boolean isStorage = false;
    private FrameLayout layout;
    //    硬件传感器自动对焦
    private float mLastX,mLastY,mLastZ;
    private boolean initFirstSensor = true;
    /*美学评分*/
    private ASTask cnnTask;
    private Bitmap mbitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keep_good);
        initView();
        //To connect HiAi Engine service using VisionBase
        VisionBase.init(KeepGood.this,new ConnectionCallback(){
            @Override
            public void onServiceConnect() {

            }
            @Override
            public void onServiceDisconnect() {

            }
        });
    }
    /*初始化view*/
    private void initView() {
        isHaveCamera = false;
        isTakePhoto = false;
        isFoucing = false;  //不在聚焦
        isStorage = false;
        isFlashing = false; //没有灯
        showScore = findViewById(R.id.score_sound_color_life);
        mSurfaceView = findViewById(R.id.surface_view);
        layout = findViewById(R.id.focus_layout_keep_goog);
        mOverCameraView = new OverCameraView(KeepGood.this);
        flashLight = findViewById(R.id.flash_Light_sound_color_life);
        flashLight.setOnClickListener(this);
        takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);

        layout.addView(mOverCameraView);
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation();
        mAspectRatio = AspectRatio.of(16,9);
        mHolder.addCallback(new MySurfaceViewCallBack());
        //传感器管理
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_photo:
                isTakePhoto = true;
                isStorage = true;
                mCamera.startPreview();
                mCamera.takePicture(null,null,new TakePicture());
                break;
            case R.id.flash_Light_sound_color_life:
                switchFlash();
                break;
        }
    }
    /*拍照回调*/
    class TakePicture implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data.length>0){
                Log.d("执行状态", "拍照成功逻辑");
                //拍照成功逻辑
                if (isStorage){ //如果是按拍照按钮则存储
                   saveTakedPicture(data);
                }else {
                    //传图片源评分
                    mbitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                    //传入bitmap,评分
                    cnnTask = new ASTask((MMListener) KeepGood.this);
                    cnnTask.execute(mbitmap);
                }
            }else {
                Log.d("执行状态", "拍照失败");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(KeepGood.this,MainActivity.class));
    }

    @Override
    public void onTaskCompleted(Float result) {
        String s = String.valueOf(result);
        int i = s.lastIndexOf(".");
        String score = s.substring(0,i);
        showScore.setText(score);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (initFirstSensor) {//初始化默认进入时候的坐标
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            initFirstSensor = false;
            return;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);
        // 移动并且没点击拍照按钮,没正在聚焦自动聚焦
        if (isHaveCamera==true){
            if ((deltaX > 2.5 || deltaY > 2.5 || deltaZ > 2.5)&&isTakePhoto==false&&isFoucing==false) {//计算坐标偏移值
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        isFoucing = true;
                        if (success){
                            isFoucing = false;
//                            Toast.makeText(KeepGood.this, "聚焦成功!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //如果不在聚焦
            if (isFoucing==false) {
                //点击位置坐标
                float x = event.getX();
                float y = event.getY() - 100;
                isFoucing = true;
                if (mCamera != null && isTakePhoto==false) {
                    mCamera.cancelAutoFocus();
                    mOverCameraView.setTouchFoucusRect(mCamera, autoFocusCallback, x, y);
                }
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isFoucing = false;
                        mOverCameraView.setFoucuing(false);
                        mOverCameraView.disDrawTouchFocusRect();
                    }
                };
                //设置聚焦超时
                mHandler.postDelayed(mRunnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }
    /**
     * 注释：点击屏幕自动对焦回调
     */
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success){
                mOverCameraView.setFoucuing(false);
                mOverCameraView.disDrawTouchFocusRect();
                mCamera.startPreview();
                mCamera.takePicture(null,null,new TakePicture());
                isFoucing = false;
                //停止聚焦超时回调
                mHandler.removeCallbacks(mRunnable);
            }else {
                Toast.makeText(KeepGood.this, "调用失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    class MySurfaceViewCallBack implements SurfaceHolder.Callback{
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mCamera = Camera.open();
            isHaveCamera = true;
            //设置预览方向
            mCamera.setDisplayOrientation(90);
            //把这个预览效果展示在SurfaceView上面
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //开启预览效果
            mCamera.startPreview();
            isPreview = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //设置设备高宽比
            mAspectRatio = getDeviceAspectRatio(KeepGood.this);
            //设置预览方向
            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters = mCamera.getParameters();
            //获取所有支持的预览尺寸
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setRotation(90);
            parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
            //获取所有支持的图片尺寸
            mCamera.setParameters(parameters);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                isHaveCamera = false;
                if (isPreview) {
                    //正在预览
                    mCamera.stopPreview();
                }
                mHolder.removeCallback(this);
                mCamera.release();
            }
        }
    }

    /**
     * 注释：获取设备屏宽比
     */
    private AspectRatio getDeviceAspectRatio(Activity activity) {
        int width = activity.getWindow().getDecorView().getWidth();
        int height = activity.getWindow().getDecorView().getHeight();
        return AspectRatio.of(height, width);
    }
    /**
     * 注释：选择合适的预览尺寸
     * @param sizes
     * @return
     */
    private Size chooseOptimalSize(SortedSet<Size> sizes, int width, int height) {
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = width;
        final int surfaceHeight = height;
        if (isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) {
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;
            }
            result = size;
        }
        return result;
    }
    /**
     * Test if the supplied orientation is in landscape.
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     */
    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == 90 ||
                orientationDegrees == 270);
    }
    /**
     * 注释：切换闪光灯
     */
    private void switchFlash() {
        isFlashing = !isFlashing;
        flashLight.setBackgroundResource(isFlashing ? R.drawable.light_on : R.drawable.light_off);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isTakePhoto = false;
        isFlashing = false;
        isFoucing = false;
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    /*
     * 切换摄像头
     * */
    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();//停掉原来摄像头的预览
            mCamera.release();//释放资源
            mCamera = null;//取消原来摄像头
        }

    }

    public void saveTakedPicture(byte[] data){
        isTakePhoto = true;
        mCamera.stopPreview();
        //创建路径文件夹
        File dir = new File(FileOperationUtils.getFilesPath(KeepGood.this)+"/Picture/");
        if (!dir.exists()){
            dir.mkdirs();
        }
        String time = String.valueOf(System.currentTimeMillis());
        String picName = time + ".jpg";   //文件名,以时间为名
        try {
            File picFile = new File(dir+"",picName+"");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            FileOutputStream fo = new FileOutputStream(picFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fo);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bufferedOutputStream);
            fo.flush();
            fo.close();
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            //把本张图片记录放到数据库
            FileAddress fileAddress = new FileAddress();
            fileAddress.setType("Picture");
            fileAddress.setName(time);
            fileAddress.setCreatedTime(time);
            fileAddress.setAtRecycleBin(false);
            fileAddress.setAddress(FileOperationUtils.getFilesPath(KeepGood.this)+"/Picture/"+picName);
            fileAddress.save();
            Toast.makeText(this, "照片已保存!", Toast.LENGTH_SHORT).show();
            finish();
            //让首页显示刷新
            Intent intent = new Intent(KeepGood.this, MainActivity.class);
            intent.putExtra("FragmentNumber",3);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}





