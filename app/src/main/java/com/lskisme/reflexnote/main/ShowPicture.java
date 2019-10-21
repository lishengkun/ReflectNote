package com.lskisme.reflexnote.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.utils.FileOperationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 点击图片跳转到详情页,展示,分享
 * 展示图片
 * 获取已存在的文件夹图片,转换成bitmap
 * @author 李胜坤
 * @date 2019/6/5 10:31
 */
public class ShowPicture extends AppCompatActivity implements View.OnClickListener {
    private int imgPosition =0;
    private float mPosX;
    private float mCurPosX;
    private View show_picture_view;
    private Button backBtn;
    private Button shared_picture;
    private ImageView displayPicture;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private File file;
    private File[] listFiles;
    private int from = -1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_picture_main);
        imgPosition = getIntent().getIntExtra("imagePosition",-1);
        from = getIntent().getIntExtra("from",-1);

        show_picture_view = findViewById(R.id.show_picture_view);
        backBtn = findViewById(R.id.back_show_picture);
        backBtn.setOnClickListener(this);
        shared_picture = findViewById(R.id.shared_show_picture);
        shared_picture.setOnClickListener(this);
        displayPicture = findViewById(R.id.show_Picture_img);
        setGestureListener();
        conversionFileToBitMap();
        displayPicture.setImageBitmap(bitmaps.get(imgPosition));

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_show_picture:
                finish();
                break;
            case R.id.shared_show_picture:
                shareSingleImage(imgPosition);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void shareSingleImage(int position) {
        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(listFiles[position]+""));
        Intent s = new Intent();
        s.setAction(Intent.ACTION_SEND);
        s.putExtra(Intent.EXTRA_STREAM, imageUri);
        s.setType("image/*");
        startActivity(Intent.createChooser(s, "分享到"));
    }

    /*把文件夹中的图片文件转换成Bitmap*/
    public void conversionFileToBitMap(){
        if (from==1){
            file = new File(FileOperationUtils.HandWritingPath);
        }else {
            if (from==2){
                file = new File(FileOperationUtils.PicturePATH);
            }
        }
        listFiles = file.listFiles();
        for (int i=0;i<listFiles.length;i++){
            if (listFiles[i].isFile()){
                Bitmap bitmap = BitmapFactory.decodeFile(listFiles[i]+"");
                bitmaps.add(bitmap);
            }
        }
    }

    /*手势滑动监听*/
    private void setGestureListener() {
        show_picture_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        //向右滑
                        if ((mCurPosX-mPosX)>0 && Math.abs(mCurPosX-mPosX)>50){
                            //0到3可以加1
                            if (imgPosition>=0 && imgPosition <listFiles.length-1){
                                imgPosition = imgPosition + 1;
                            }else {
                                //如果小于0,等于最后的图片下表
                                if (imgPosition <0){
                                    imgPosition = listFiles.length-1;
                                }
                                //如果大于等于4,等于0
                                if (imgPosition >= listFiles.length-1){
                                    imgPosition = 0;
                                }
                            }
                            displayPicture.setImageBitmap(bitmaps.get(imgPosition));
                        }else {
                            //向左滑
                            if ((mCurPosX-mPosX)<0 && Math.abs(mCurPosX-mPosX)>50){
                                //1-4都可以减1
                                if (imgPosition>0 && imgPosition <listFiles.length){
                                    imgPosition = imgPosition - 1;
                                }else {
                                    //如果小于0,等于最后的图片下表
                                    if (imgPosition <=0){
                                        imgPosition = listFiles.length-1;
                                    }
                                    //如果大于等于4,等于0
                                    if (imgPosition >= listFiles.length){
                                        imgPosition = 0;
                                    }
                                }
                                displayPicture.setImageBitmap(bitmaps.get(imgPosition));
                            }
                        }
                        break;

                }
                return true;
            }
        });
    }

}
