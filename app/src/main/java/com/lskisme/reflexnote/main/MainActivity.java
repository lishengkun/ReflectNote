package com.lskisme.reflexnote.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.levitationButton.NoteBook;
import com.lskisme.reflexnote.levitationButton.handWritingDrawing.HandWritingDrawing;
import com.lskisme.reflexnote.levitationButton.keepGood.KeepGood;
import com.lskisme.reflexnote.levitationButton.phoneticShorthand.PhoneticShorthand;
import com.lskisme.reflexnote.levitationButton.textScanning.TextScanning;
import com.lskisme.reflexnote.levitationButton.toDoItem.ToDoItem;
import com.lskisme.reflexnote.main.KeepGood.KeepGoodFragment;
import com.lskisme.reflexnote.main.NoteBookFragment.NotebookFragment;
import com.lskisme.reflexnote.main.toDoItemFragment.TodoItemsFragment;
import com.lskisme.reflexnote.sidebar.AboutUs;
import com.lskisme.reflexnote.sidebar.FunctionGuide;
import com.lskisme.reflexnote.sidebar.ShowLabelContent;
import com.lskisme.reflexnote.sidebar.labelManagement.LabelManagement;
import com.lskisme.reflexnote.utils.LitePalOperation;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    //   侧边栏
    private DrawerLayout mDrawerLayout;
    private View labelListLayout;
    //    viewpage icon
    private ImageView notebook_btn;
    private ImageView voice_btn;
    private ImageView to_do_items_btn;
    private ImageView hand_write_btn;
    //    tabbar
    protected Toolbar tab_bar_homepage;
    private ViewPager viewPager_homepage;
    //悬浮按钮项
    private FloatingActionMenu float_button;
    private FloatingActionButton fab_text_scanning;
    private FloatingActionButton fab_phonetic_shorthand;
    private FloatingActionButton fab_hand_writing_drawing;
    private FloatingActionButton fab_to_do_items;
    private FloatingActionButton fab_keep_good;
    private FloatingActionButton fab_note_book;

    //侧边栏文本控件点击事件
    private TextView allLabel,labelManage,functionGuide,shared,about,tip;
    private Button foldBtn;
    private boolean isFoldLabelList = true;
    private List<Map<String,String>> data = new ArrayList<>();
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    //控制显示哪个Fragment
    private int FragmentNumber = 0;
    private int recieveDeletePosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //权限还没有授予，需要在这里写申请权限的代码
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},0x0010);
            }
        }
        //        viewpager逻辑处理
        initView();
        initContentFragment();
        //隐藏侧边栏标签列表展示
        if (isFoldLabelList){
            labelListLayout = findViewById(R.id.sidebar_list_view);
            labelListLayout.setVisibility(View.GONE);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("DataBaseInfo", MODE_PRIVATE);
        //        如果不存在数据库
        if (sharedPreferences.getBoolean("isExist", false) == false) {
            //创建数据库
            LitePal.getDatabase();
            //保存数据库创建信息
            SharedPreferences.Editor editor = getSharedPreferences("DataBaseInfo", MODE_PRIVATE).edit();
            editor.putString("name", "database");
            editor.putInt("version", 1);
            editor.putBoolean("isExist", true);
            editor.apply();
//            Toast.makeText(this, "数据库创建成功!", Toast.LENGTH_SHORT).show();
        }
        //侧边栏展示标签
        if (LitePalOperation.getAllLabelNames().length!=0){
            foldBtn.setVisibility(View.VISIBLE);
            for (int i=0;i<LitePalOperation.getLabelSameNameNumber().size();i++){
                Map<String,String> map = new HashMap<>();
                map.put("name",LitePalOperation.getLabelSameNameNumber().get(i).get("labelName"));
                map.put("number",LitePalOperation.getLabelSameNameNumber().get(i).get("labelNumber"));
                data.add(map);
            }
            simpleAdapter = new SimpleAdapter(this,data,R.layout.show_label_item,
                    new String[]{"name","number"},new int[]{R.id.show_label_name,R.id.show_label_number});
            listView = findViewById(R.id.sidebar_list_view);
            listView.setAdapter(simpleAdapter);
            //标签项点击直达内容展示页
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //跳转页面,传入此标签名
                    Intent toShow = new Intent(MainActivity.this, ShowLabelContent.class);
                    toShow.putExtra("label",data.get(position).get("name"));
                    startActivity(toShow);
                }
            });
        }else {
            //隐藏展开按钮
            foldBtn.setVisibility(View.GONE);
        }
        //ToolBar设置
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");   //把toolbar标题设空,添加TextView
        setSupportActionBar(toolbar);
        //sidebar提示
        mDrawerLayout = findViewById(R.id.drawer_layout_homepage);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.sidebar);
        }
        //        悬浮点击事件
        float_button = findViewById(R.id.float_button);
        float_button.setClosedOnTouchOutside(true);  //使用setClosedOnTouchOutside方法可以设置点击蒙版关闭的开关
        fab_text_scanning = findViewById(R.id.fab_text_scanning);
        fab_phonetic_shorthand = findViewById(R.id.fab_phonetic_shorthand);
        fab_hand_writing_drawing = findViewById(R.id.fab_hand_writing_drawing);
        fab_to_do_items = findViewById(R.id.fab_to_do_items);
        fab_keep_good = findViewById(R.id.fab_keep_good);
        fab_note_book = findViewById(R.id.fab_note_book);
        //        悬浮按钮项点击事件
        fab_text_scanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent text_scanning = new Intent(MainActivity.this, TextScanning.class);
                startActivity(text_scanning);
            }
        });
        fab_phonetic_shorthand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent phonetic_shorthand = new Intent(MainActivity.this, PhoneticShorthand.class);
                startActivity(phonetic_shorthand);
            }
        });
        fab_hand_writing_drawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent hand_writing_drawing = new Intent(MainActivity.this, HandWritingDrawing.class);
                startActivity(hand_writing_drawing);
            }
        });
        fab_to_do_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent to_do_items = new Intent(MainActivity.this, ToDoItem.class);
                startActivity(to_do_items);
            }
        });
        fab_keep_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent sound_color_life = new Intent(MainActivity.this, KeepGood.class);
                startActivity(sound_color_life);
            }
        });
        fab_note_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent note_book = new Intent(MainActivity.this, NoteBook.class);
                startActivity(note_book);
            }
        });
    }

    //初始化view函数*********************************************************************************************************
    public void initView() {
        // ********tabbar btn******
        notebook_btn = findViewById(R.id.notebook_btn);
        notebook_btn.setBackgroundResource(R.drawable.notebook_select);
        voice_btn = findViewById(R.id.voice_btn);
        voice_btn.setBackgroundResource(R.drawable.handwrite_select);
        to_do_items_btn = findViewById(R.id.to_do_items_btn);
        to_do_items_btn.setBackgroundResource(R.drawable.to_do_items_select);
        hand_write_btn = findViewById(R.id.hand_write_btn);
        hand_write_btn.setBackgroundResource(R.drawable.to_do_items_select);
        // **********tab_bar viewpager***********
        tab_bar_homepage = findViewById(R.id.tabbars_homepage);
        viewPager_homepage = findViewById(R.id.viewpager_homepage);
        tip = findViewById(R.id.name_homepage);
        tip.setText("文字笔记");
        //侧边栏文本控件点击事件
        allLabel = findViewById(R.id.all_label_text);
        labelManage = findViewById(R.id.label_manage_text);
        functionGuide = findViewById(R.id.function_guide_text);
        shared = findViewById(R.id.shared_text);
        about = findViewById(R.id.about_text);
        foldBtn = findViewById(R.id.fold_btn);
        //绑定点击事件
        allLabel.setClickable(true);
        allLabel.setOnClickListener(this);
        labelManage.setClickable(true);
        labelManage.setOnClickListener(this);
        functionGuide.setClickable(true);
        functionGuide.setOnClickListener(this);
        shared.setClickable(true);
        shared.setOnClickListener(this);
        about.setClickable(true);
        about.setOnClickListener(this);
        foldBtn.setOnClickListener(this);
        // 批量tab_bar按钮设置点击事件
        notebook_btn.setOnClickListener(this);
        voice_btn.setOnClickListener(this);
        to_do_items_btn.setOnClickListener(this);
        hand_write_btn.setOnClickListener(this);
    }

    //    初始化viwp_ager中的fragment
    public void initContentFragment() {
        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        mFragmentList.add(new NotebookFragment());
        mFragmentList.add(new VoiceFragment());
        mFragmentList.add(new TodoItemsFragment());
        mFragmentList.add(new KeepGoodFragment());

        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), mFragmentList);
        viewPager_homepage.setAdapter(adapter);
        viewPager_homepage.setOffscreenPageLimit(3);
        viewPager_homepage.addOnPageChangeListener(this);
        //如果第一次打开默认显示第一个，其他界面返回显示对应
        if (FragmentNumber == 0){
            setCurrentItem(0);
        }else {
            setCurrentItem(FragmentNumber);
        };
    }
    //显示哪个Fragment方法
    public void setCurrentItem(int i) {
        boolean[] status = {false,false,false,false};
        String[] tips = {"文字笔记","手写绘图","待办事项","留住美好"};
        int[] no_select = {R.drawable.notebook,R.drawable.handwrite,R.drawable.to_do_items,R.drawable.keep_good};
        int[] select = {R.drawable.notebook_select,R.drawable.handwrite_select,R.drawable.to_do_items_select,R.drawable.keep_good_select};
        View[] id = {notebook_btn,voice_btn,to_do_items_btn,hand_write_btn};
        viewPager_homepage.setCurrentItem(i);

        notebook_btn.setSelected(false);
        voice_btn.setSelected(false);
        to_do_items_btn.setSelected(false);
        hand_write_btn.setSelected(false);
        switch (i) {
            case 0:
                notebook_btn.setSelected(true);
                status[0] = true;
                break;
            case 1:
                voice_btn.setSelected(true);
                status[1] = true;
                break;
            case 2:
                to_do_items_btn.setSelected(true);
                status[2] = true;
                break;
            case 3:
                hand_write_btn.setSelected(true);
                status[3] = true;
                break;
        }
        //循环状态,点击的换,其余的不动
        for (int j=0;j<4;j++){
            if (status[j]==true){
                tip.setText(tips[j]);
                id[j].setBackgroundResource(select[j]);
            }else {
                id[j].setBackgroundResource(no_select[j]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    /*菜单项点击事件*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.recommend_share_btn:
                shareApp("MainActivity", "即刻笔记", "华为应用市场搜索“即刻笔记”下载", null);
                break;
            case R.id.support_evaluation_btn:
                Toast.makeText(this, "你点击了支持评价按钮", Toast.LENGTH_SHORT).show();
                goToMarket();
                break;
            case R.id.contact_us_btn:
                Intent aboutUs = new Intent(MainActivity.this, AboutUs.class);
                startActivity(aboutUs);
                break;
            case R.id.function_guide_btn:
                Intent functionGuide = new Intent(MainActivity.this, FunctionGuide.class);
                startActivity(functionGuide);
                break;
            default:
                break;
        }
        return true;
    }
    /**
     * 去应用市场评分
     */
    private void goToMarket() {
        if (!isMarketInstalled(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "您的手机没有安装应用市场", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse("market://details?id="+getPackageName());
//            Uri uri = Uri.parse("market://details?id=" + "com.tencent.mobileqq");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            // 也可以调到某个网页应用市场
            Toast.makeText(MainActivity.this, "手机没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 本手机是否安装了应用市场
     * @param context
     * @return
     */
    public static boolean isMarketInstalled(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("market://details?id=android.browser"));
        List list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return 0 != list.size();
    }

    /* viewpager相关*/
    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }
    @Override
    public void onPageSelected(int i) {
        setCurrentItem(i);
    }
    @Override
    public void onPageScrollStateChanged(int i) {

    }
    /*按钮点击事件*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notebook_btn:
                if (viewPager_homepage.getCurrentItem() != 0) {
                    setCurrentItem(0);
                }
                break;
            case R.id.voice_btn:
                if (viewPager_homepage.getCurrentItem() != 1) {
                    setCurrentItem(1);
                }
                break;
            case R.id.to_do_items_btn:
                if (viewPager_homepage.getCurrentItem() != 2) {
                    setCurrentItem(2);
                }
                break;
            case R.id.hand_write_btn:
                if (viewPager_homepage.getCurrentItem() != 3) {
                    setCurrentItem(3);
                }
                break;
            case R.id.label_manage_text: //跳转到标签管理
                Intent intent = new Intent(MainActivity.this,LabelManagement.class);
                startActivityForResult(intent,1);
                break;
            case R.id.function_guide_text:   //功能指南
                Intent functionGuide = new Intent(MainActivity.this, FunctionGuide.class);
                startActivity(functionGuide);
                break;
            case R.id.shared_text:   //推荐分享
                shareApp("MainActivity", "即刻笔记", "华为应用市场搜索“即刻笔记”下载", null);
                break;
            case R.id.about_text:   //关于
                Intent aboutUs = new Intent(MainActivity.this, AboutUs.class);
                startActivity(aboutUs);
                break;
            //动态添加view
            case R.id.fold_btn:
                //                如果隐藏就显示
                if (isFoldLabelList){
                    labelListLayout.setVisibility(View.VISIBLE);
                    isFoldLabelList = !isFoldLabelList;
                }else {
                    labelListLayout.setVisibility(View.GONE);
                    isFoldLabelList = !isFoldLabelList;
                }
                if (LitePalOperation.getAllLabelNames().length!=0){
                    simpleAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(this, "还没有标签!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    recieveDeletePosition = data.getIntExtra("refreshPosition",-1);
                }
                break;
        }
    }

    //  分享功能
    public void shareApp(String activityTitle, String msgTitle, String msgText,
                         String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/png");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //如果标签为0,隐藏展开按钮
        if (LitePalOperation.getAllLabelNames().length==0){
            foldBtn.setVisibility(View.GONE);
            labelListLayout.setVisibility(View.GONE);
        }else {
            //不为0,删除删除的那个,刷新
            if (recieveDeletePosition!=-1){
//                Toast.makeText(this, recieveDeletePosition+"", Toast.LENGTH_SHORT).show();
                data.remove(recieveDeletePosition);
                simpleAdapter.notifyDataSetChanged();
                recieveDeletePosition = -1;
            }
        }
        FragmentNumber = getIntent().getIntExtra("FragmentNumber",0);       //获得其他页面传来的fragment显示值,显示此页面
        setCurrentItem(FragmentNumber);
    }

}
