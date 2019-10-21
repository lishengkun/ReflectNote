package com.lskisme.reflexnote.levitationButton.textScanning;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.main.MainActivity;
import com.lskisme.reflexnote.utils.AspectRatio;
import com.lskisme.reflexnote.utils.OverCameraView;
import com.lskisme.reflexnote.utils.SizeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
*   文本识别
* */
public class TextScanning extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    /*相机相关*/
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean isPreview;
    /*预览尺寸集合*/
    private final SizeMap mPreviewSizes = new SizeMap();
    /*图片尺寸集合*/
    private final SizeMap mPictureSizes = new SizeMap();
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
    /*view 控件*/
    private Button backBtn,flashLight,takePicture;
    private FrameLayout focusLayout;
    private ImageView album;

    private boolean isHaveCamera;
    private static final int REQUEST_CHOOSE_PHOTO_CODE = 2;

    //    硬件传感器自动对焦
    private float mLastX,mLastY,mLastZ;
    private boolean initFirstSensor = true;

    private List<String> allPicPath ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_scanning);
        initView();
        //让相册显示拍照第一张图片,获取DICM文件夹第一个文件转换成bitmap显示到ImageView中
        setFirstPictureToAlbum();
    }

    private void initView() {
        isHaveCamera = false;
        isTakePhoto = false;
        isFlashing = false;
        isFoucing = false;
        //绑定控件id
        focusLayout = findViewById(R.id.focus_layout);
        mOverCameraView = new OverCameraView(this);
        mSurfaceView = findViewById(R.id.camera_preview);
        backBtn = findViewById(R.id.backBtn_text_scanning);
        backBtn.setOnClickListener(this);
        flashLight = findViewById(R.id.light_camera);
        flashLight.setOnClickListener(this);
        flashLight.setBackgroundResource(R.drawable.light_off);
        album = findViewById(R.id.album_camera);
        album.setOnClickListener(this);
        takePicture = findViewById(R.id.take_picture_btn);
        takePicture.setOnClickListener(this);

        //控件事件
        focusLayout.addView(mOverCameraView);   //把聚焦控件显示到FramLayout
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new MySurfaceViewCallBack());
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation();
        mAspectRatio = AspectRatio.of(16,9);

        //传感器管理
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }
    public void setFirstPictureToAlbum() {
        //获得所有图片
        allPicPath = new ArrayList<>();
        allPicPath.clear();
        /**
         * 需要从数据库中获取的信息：
         * BUCKET_DISPLAY_NAME  文件夹名称
         * DATA  文件路径
         */
        String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA};

        /**
         * 通过ContentResolver 从媒体数据库中读取图片信息
         */
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  //限制类型为图片
                projection,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg","image/jpg"},  // 这里筛选了jpg和png格式的图片
                MediaStore.Images.Media.DATE_ADDED); // 排序方式：按添加时间排序
                while (cursor.moveToNext()) {
                    //获取图片路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    allPicPath.add(path);
                }
                cursor.close();
        //把获得的图片显示到imageview，如果没抓到，显示初始的
        if (allPicPath.size()==0){
            album.setBackgroundResource(R.drawable.album);
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(allPicPath.get(0)+"");
            album.setImageBitmap(bitmap);
        }
    }
    /*选择图片*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Uri selectedImage = data.getData();
            getBitmap(selectedImage);
        }
    }
    private void getBitmap(Uri imageUri) {
        String[] pathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(imageUri, pathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(pathColumn[0]);
        /* get image path */
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        finish();
        Intent intent = new Intent(TextScanning.this, TakePictureResult.class);
        intent.putExtra("picturePath",picturePath);
        intent.putExtra("PicFrom",20);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //拍照
            case R.id.take_picture_btn:
                isTakePhoto = true;
                mCamera.takePicture(null,null,new TakePicture());
                break;
            //相册
            case R.id.album_camera:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CHOOSE_PHOTO_CODE);
                break;
            case R.id.light_camera:
                switchFlash();
                break;
            case R.id.backBtn_text_scanning:
                finish();
                startActivity(new Intent(TextScanning.this, MainActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(TextScanning.this, MainActivity.class));
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
        if (isHaveCamera){
            if ((deltaX > 2.5 || deltaY > 2.5 || deltaZ > 2.5)&&isTakePhoto==false&&!isFoucing) {//计算坐标偏移值
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        isFoucing = true;
                        if (success){
                            isFoucing = false;
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

    /*拍照回调*/
    class TakePicture implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data.length>0){
                //拍照成功逻辑
                try {
                    File tempFile = new File(getExternalCacheDir().getPath()+"/temporary.png");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(data);
                    fos.close();
                    isTakePhoto = true;
                    finish();
                    Intent intent = new Intent(TextScanning.this,TakePictureResult.class);
                    intent.putExtra("picpath",tempFile.getAbsolutePath()+"");
                    intent.putExtra("PicFrom",10);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
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
            mAspectRatio = getDeviceAspectRatio(TextScanning.this);
            //设置预览方向
            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters = mCamera.getParameters();
            //获取所有支持的预览尺寸
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setRotation(90);
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
                mSurfaceHolder.removeCallback(this);
                mCamera.release();
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isFoucing) {
                //点击位置坐标
                float x = event.getX();
                float y = event.getY() - 100;
                isFoucing = true;
                if (mCamera != null && isTakePhoto==false) {
                    mOverCameraView.setTouchFoucusRect(mCamera, autoFocusCallback, x, y);
                }
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TextScanning.this, "自动聚焦超时,请调整合适的位置拍摄！", Toast.LENGTH_SHORT);
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
     * 注释：自动对焦回调
     */
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            mOverCameraView.setFoucuing(false);
            mOverCameraView.disDrawTouchFocusRect();
            isFoucing = false;
            //停止聚焦超时回调
            mHandler.removeCallbacks(mRunnable);
        }
    };
    /**
     * 注释：获取设备屏宽比
     */
    private AspectRatio getDeviceAspectRatio(Activity activity) {
        int width = activity.getWindow().getDecorView().getWidth();
        int height = activity.getWindow().getDecorView().getHeight();
        return AspectRatio.of(height, width);
    }

    /**
     * Test if the supplied orientation is in landscape.
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
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
    @Override   //  相机随系统的API的变化而变化
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}