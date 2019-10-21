package com.lskisme.reflexnote.sidebar;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.lskisme.reflexnote.R;
/*
*   自定义标题栏
* */
public class TitleBackAndName extends ConstraintLayout {
    public TitleBackAndName(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title_back_name,this);
        Button backBtn = findViewById(R.id.backBtn_title_back_name);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });
    }
}


