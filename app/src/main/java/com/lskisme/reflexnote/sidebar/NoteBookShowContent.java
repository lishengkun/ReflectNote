package com.lskisme.reflexnote.sidebar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.levitationButton.NoteBook;

/**
 * @author 李胜坤
 * @date 2019/5/15 13:00
 */
public class NoteBookShowContent extends AppCompatActivity{
    private String title;
    private String content;

    private Button backBtn;
    TextView showTitle,showContent;
    private String labelName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_book_show_content);
        labelName = getIntent().getStringExtra("标签名");

        title = getIntent().getStringExtra("标题");
        content = getIntent().getStringExtra("内容");
        showTitle = findViewById(R.id.show_title_note_book);
        showContent = findViewById(R.id.show_content_note_book);
        backBtn = findViewById(R.id.backBtn_show_content_note_book);
        showTitle.setText(title);
        showContent.setText(content);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        FloatingActionButton floatingActionButton = findViewById(R.id.edit_show_content_note_book);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到新建页面.编辑内容,重新保存
                finish();
                Intent toNoteBook = new Intent(NoteBookShowContent.this, NoteBook.class);
                toNoteBook.putExtra("标题",title);
                toNoteBook.putExtra("内容",content);
                toNoteBook.putExtra("标签名",labelName);
                toNoteBook.putExtra("重新编辑",1);
                startActivity(toNoteBook);
            }
        });

    }
}
