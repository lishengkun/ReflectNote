package com.lskisme.reflexnote.sidebar.labelManagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.MainActivity;
import com.lskisme.reflexnote.utils.LitePalOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
*   显示标签列表
*   长按多选删除
*   点击直达标签所在处.
* */
public class LabelManagement extends AppCompatActivity{

    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private Button bacBtn;
    private View emptyLayout;
    private View listLayout;
    private int deletePosition;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.label_management);

        bacBtn = findViewById(R.id.title_back_btn);
        listView = findViewById(R.id.label_list_view);
        listLayout = findViewById(R.id.list_view_layout);
        emptyLayout = findViewById(R.id.no_label_layout);
        //获取所有去重复标签名,渲染到listview中,长按删除
        final List<Map<String,Object>> listItems = new ArrayList<>();
        if (LitePalOperation.getAllLabelNames().length!=0){
            listLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
            for (int i=0;i< LitePalOperation.getLabelNameList().length;i++){
                Map<String,Object> listItem = new HashMap<>();
                listItem.put("labelName",LitePalOperation.getLabelNameList()[i]);
                listItems.add(listItem);
            }
            simpleAdapter = new SimpleAdapter(LabelManagement.this,listItems,R.layout.label_manage_list_item,
                    new String[]{"labelName"},new int[]{R.id.label_manage_item_text});
            listView.setAdapter(simpleAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LabelManagement.this);
                    builder.setTitle("删除确认");
                    builder.setMessage("您是否确认删除?");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String title = (String) listItems.get(position).get("labelName");
                            deletePosition = position;
                            //把有此标签的记录的标签项值置空
                            FileAddress fileAddress = new FileAddress();
                            fileAddress.setLabel("");
                            fileAddress.updateAll("type = ? and label = ?","Text",title);
                            //删除加载数据中的此项
                            listItems.remove(position);
                            simpleAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    return true;
                }
            });
        }else {
            //还没有标签
            listLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
        bacBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(LabelManagement.this, MainActivity.class);
                intent.putExtra("refreshPosition",deletePosition);
                setResult(RESULT_OK,intent);
            }
        });

    }
    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(LabelManagement.this, MainActivity.class);
        intent.putExtra("refreshPosition",deletePosition);
        setResult(RESULT_OK,intent);
    }
}