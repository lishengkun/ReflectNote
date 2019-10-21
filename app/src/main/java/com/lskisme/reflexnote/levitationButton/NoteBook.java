package com.lskisme.reflexnote.levitationButton;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.MainActivity;
import com.lskisme.reflexnote.utils.FileOperationUtils;
import com.lskisme.reflexnote.utils.LitePalOperation;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.litepal.LitePal;

import java.util.Date;

public class NoteBook extends AppCompatActivity {
    private Button backBtn;
    private Button insureBtn;
    private EditText inputTitleText;
    private EditText inputContentText;
    private Button showLabel;
    private EditText inputNewlabelName;
    private Button addNewLabel,insureAddLabel;
//    输入内容
    private String FIleTYPE = "Text";
    private String saveInputTitle = null;
    private String saveInputContent = null;
    private String saveLabelName = null;

    private String receivedTitle = null;
    private String receivedContent = null;
    private int receivedCode = 0;
    private String receivedLabelName = null;
    private String[] labelNameArray ;
    private boolean isNewOrEdit = true;     //是新建文件还是编辑重新保存,true 新建, false 编辑

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_book);

        //绑定控件
        backBtn = findViewById(R.id.backBtn_note_book);
        insureBtn = findViewById(R.id.insure_note_book);
        inputTitleText = findViewById(R.id.input_title_note_book);
        inputContentText = findViewById(R.id.input_content_note_book);
        showLabel = findViewById(R.id.show_label_note_book);
        inputNewlabelName = findViewById(R.id.add_label_name);
        addNewLabel = findViewById(R.id.add_label_note_book);
        insureAddLabel =findViewById(R.id.insure_add_label_note_book);
        inputNewlabelName.setVisibility(View.GONE);
        insureAddLabel.setVisibility(View.GONE);

        //获得编辑页面传来的预编辑值
        receivedTitle = getIntent().getStringExtra("标题");
        receivedContent = getIntent().getStringExtra("内容");
        receivedCode = getIntent().getIntExtra("重新编辑",0);
        receivedLabelName = getIntent().getStringExtra("标签名");
//        如果接到其他页面传来的值,就直接设置到界面显示,编辑文件重新保存
        if (receivedCode==1){
            //编辑文件
            isNewOrEdit = false;
            inputTitleText.setText(receivedTitle);
            inputContentText.setText(receivedContent);
            showLabel.setText(receivedLabelName);
        }else{
            isNewOrEdit = true;     //新建
        }

//        按钮点击事件
        //添加新标签名
        addNewLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击添加,显示输入框,输入后点击确定,提交数据
                addNewLabel.setVisibility(View.GONE);
                inputNewlabelName.setVisibility(View.VISIBLE);
                insureAddLabel.setVisibility(View.VISIBLE);
                inputNewlabelName.setText("");  //清空上次输入值
            }
        });
        //新标签名确定
        insureAddLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputNewlabelName.getText().toString().contains(" ")  && inputNewlabelName.getText().length() != 0 ){
                    //把新标签名赋给当前标签名显示
                    showLabel.setText(inputNewlabelName.getText().toString());
                }else{
                    Toast.makeText(NoteBook.this, "请输入有效标签名!", Toast.LENGTH_SHORT).show();
                }
                inputNewlabelName.setVisibility(View.GONE);
                insureAddLabel.setVisibility(View.GONE);
                addNewLabel.setVisibility(View.VISIBLE);
            }
        });
        //选择已有标签添加,显示popupMenu
        showLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果标签名个数不为0
                if (LitePalOperation.getAllLabelNames().length > 0){
                    labelNameArray = new String[LitePalOperation.getLabelNameList().length];
                    for (int i=0;i<LitePalOperation.getLabelNameList().length;i++){
                        labelNameArray[i] = LitePalOperation.getLabelNameList()[i];
                    }
                    //显示popupMenu
                    new XPopup.Builder(NoteBook.this)
                            .atView(v)
                            .asAttachList(labelNameArray,
                                    null,
                                    new OnSelectListener() {
                                        @Override
                                        public void onSelect(int position, String text) {
                                            showLabel.setText(text);
                                        }
                                    }).show();
                }else {
                    if (LitePalOperation.getLabelNameList().length==1){
                        Toast.makeText(NoteBook.this, "您可以修改添加标签!", Toast.LENGTH_SHORT).show();
                    }
                    if(LitePalOperation.getLabelNameList().length==0){
                        Toast.makeText(NoteBook.this, "还没有标签,请添加标签", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
//        新文件提交按钮
        insureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSavedFile = false;
                //点击确定按钮,接收输入的字符串
                saveInputTitle = inputTitleText.getText().toString();
                saveInputContent = inputContentText.getText().toString();
                saveLabelName = showLabel.getText().toString();
                //如果标题不为空
                if (!inputTitleText.getText().toString().contains(" ") && inputTitleText.getText().toString().length() != 0
                        ){
                    //如果是重新编辑文件,就把原来的删除之后再重新保存,否则直接保存新文件
                    if (isNewOrEdit==false){
                        FileOperationUtils.deleteFile(NoteBook.this,"Text",receivedTitle,"txt" );
                        //删除数据库本条记录
                        LitePal.deleteAll(FileAddress.class,"name = ?",receivedTitle);
                        //把文本文件存到本地内存中
                        isSavedFile = FileOperationUtils.saveTextFile(saveInputTitle,saveInputContent,NoteBook.this);
                    }else{//新建文件
                        //把文本文件存到本地内存中
                        isSavedFile = FileOperationUtils.saveTextFile(saveInputTitle,saveInputContent,NoteBook.this);
                    }
                    if (isSavedFile==true){
                        //向数据库中添加文件地址信息fileName,filePath
                        FileAddress fileAddress = new FileAddress();
                        fileAddress.setName(saveInputTitle);
                        fileAddress.setType(FIleTYPE);
                        if (!showLabel.getText().toString().contains("选择标签")&&!showLabel.getText().toString().contains(" ")){
                            fileAddress.setLabel(saveLabelName);
                        }else {
                            fileAddress.setLabel("");
                        }
                        fileAddress.setAddress(FileOperationUtils.getFileAbsolutePath("Text",saveInputTitle,"txt"));
                        fileAddress.setCreatedTime(new Date().toLocaleString());    //设置文件存储时间
                        fileAddress.save();

                        //让首页显示刷新
                        Intent intent = new Intent(NoteBook.this, MainActivity.class);
                        intent.putExtra("FragmentNumber",0);
//                        intent.putExtra("AddedNewLabelName",saveLabelName);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(NoteBook.this, "请换个标题!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                        Toast.makeText(NoteBook.this, "请输入标题后再确定!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(NoteBook.this,MainActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(NoteBook.this,MainActivity.class));
    }
}
