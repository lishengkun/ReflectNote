package com.lskisme.reflexnote.levitationButton.handWritingDrawing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.levitationButton.keepGood.KeepGood;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.MainActivity;
import com.lskisme.reflexnote.utils.FileOperationUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 手写绘图
 * @author 李胜坤
 * @date 2019/5/18 13:29
 */
public class HandWritingDrawing extends AppCompatActivity {

    private ImageView iv;
    private Bitmap baseBitmap;
    private Button btn_resume;
    private Button btn_save;
    private Canvas canvas;
    private Paint paint;

    float radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_writing_drawing);

        radio = 5;
        iv = (ImageView) findViewById(R.id.iv);
        // 初始化一个画笔，笔触宽度为5，颜色为红色
        paint = new Paint();
        paint.setStrokeWidth(radio);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        iv = (ImageView) findViewById(R.id.iv);
        btn_resume = (Button) findViewById(R.id.btn_resume);
        btn_save = (Button) findViewById(R.id.btn_save);

        btn_resume.setOnClickListener(click);
        btn_save.setOnClickListener(click);
        iv.setOnTouchListener(touch);
    }

    private View.OnTouchListener touch = new View.OnTouchListener() {
        // 定义手指开始触摸的坐标
        float startX;
        float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    // 第一次绘图初始化内存图片，指定背景为白色
                    if (baseBitmap == null) {
                        baseBitmap = Bitmap.createBitmap(iv.getWidth(),
                                iv.getHeight(), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(baseBitmap);
                        canvas.drawColor(Color.WHITE);
                    }
                    // 记录开始触摸的点的坐标
                    startX = event.getX();
                    startY = event.getY();
                    break;
                // 用户手指在屏幕上移动的动作
                case MotionEvent.ACTION_MOVE:
                    // 记录移动位置的点的坐标
                    float stopX = event.getX();
                    float stopY = event.getY();

                    Thread t =new Thread(new Runnable() {
                        @Override
                        public void run() {
                            radio+=0.1;

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();

                    paint.setStrokeWidth(radio);
                    //根据两点坐标，绘制连线
                    canvas.drawLine(startX, startY, stopX, stopY, paint);

                    // 更新开始点的位置
                    startX = event.getX();
                    startY = event.getY();
                    // 把图片展示到ImageView中
                    iv.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:
                    radio = 5;
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_save:
                    if (baseBitmap == null){
                        Toast.makeText(HandWritingDrawing.this, "请先绘制后再保存！", Toast.LENGTH_SHORT).show();
                    }else {
                        saveBitmap();
                    }
                    break;
                case R.id.btn_resume:
                    resumeCanvas();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存图片到SD卡上
     */
    protected void saveBitmap() {
        //创建绘图图片文件夹
        File dir = new File(FileOperationUtils.getFilesPath(HandWritingDrawing.this)+"/HandWriting/");
        if (!dir.exists()){
            dir.mkdirs();
        }
        String time = String.valueOf(System.currentTimeMillis());
        String picName = time + ".jpg";
        File picFile = new File(dir,picName);
        try {
            FileOutputStream fos = new FileOutputStream(picFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            baseBitmap.compress(Bitmap.CompressFormat.JPEG,70,bos);
            fos.close();
            bos.close();
            //把本张图片记录放到数据库
            FileAddress fileAddress = new FileAddress();
            fileAddress.setType("HandWriting");
            fileAddress.setName(time);
            fileAddress.setCreatedTime(time);
            fileAddress.setAtRecycleBin(false);
            fileAddress.setAddress(FileOperationUtils.getFilesPath(HandWritingDrawing.this)+"/HandWriting/"+picName);
            fileAddress.save();
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            //让首页显示刷新
            Intent intent = new Intent(HandWritingDrawing.this, MainActivity.class);
            intent.putExtra("FragmentNumber",1);
            startActivity(intent);
            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 手动清除画板的绘图，重新创建一个画板
    protected void resumeCanvas() {
        if (baseBitmap != null) {
            baseBitmap = Bitmap.createBitmap(iv.getWidth(),
                    iv.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
            canvas.drawColor(Color.WHITE);
            iv.setImageBitmap(baseBitmap);
            Toast.makeText(HandWritingDrawing.this, "清除画板成功，可以重新开始绘图", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 使用内部类 自定义一个简单的View
     */
    class CustomView1 extends View {

        Paint paint;

        public CustomView1(Context context) {
            super(context);
            paint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
            paint.setColor(Color.YELLOW);//颜色
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(3);//画笔大小
        }


        @Override
        protected void onDraw(Canvas canvas) {
            //直接将View显示区域用某个颜色填充满
            //canvas.drawColor(Color.BLUE);

            //绘圆
            canvas.drawCircle(100, 100, 90, paint);
            //绘线
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(10);
            canvas.drawLine(300,300,400,500,paint);

            RectF rect = new RectF(100, 100, 300, 300);

            //绘制弧线区域
            paint.setColor(Color.RED);
            canvas.drawArc(rect, //弧线所使用的矩形区域大小
                    0,  //开始角度
                    120, //扫过的角度
                    true, //是否使用中心
                    paint);

            //矩形区域内切椭圆
            rect = new RectF(500,500,600,700);
            canvas.drawOval(rect, paint);

            //绘矩形
            paint.setColor(Color.BLUE);
            rect = new RectF(800,800,1000,1000);
            canvas.drawRect(rect,paint);

            //绘圆角矩形
            paint.setColor(Color.YELLOW);
            canvas.drawRoundRect(rect,
                    50, //x轴的半径
                    50, //y轴的半径
                    paint);

            Path path = new Path(); //定义一条路径
            path.moveTo(100, 500); //移动到 坐标10,10
            path.lineTo(300, 600);
            path.lineTo(200,500);
            path.lineTo(100, 500);
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(HandWritingDrawing.this,MainActivity.class));
    }
}

