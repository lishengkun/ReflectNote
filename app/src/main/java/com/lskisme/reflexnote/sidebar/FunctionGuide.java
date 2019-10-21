package com.lskisme.reflexnote.sidebar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lskisme.reflexnote.R;

public class FunctionGuide extends AppCompatActivity {
    private TextView title_text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.function_guide);

        intView();  //初始化数据
    }
    //初始化数据
    private void intView() {
        //隐藏系统标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        title_text = findViewById(R.id.title_name_title_back_name);
        title_text.setText("功能指南");
    }
}
