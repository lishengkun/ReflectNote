package com.lskisme.reflexnote.levitationButton.toDoItem;

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
import android.widget.TextView;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToDoItem extends AppCompatActivity implements View.OnClickListener{

    private Button backBtn,addItems,completeBtn;    //返回,添加项,完成
//    private EditText inputTitle;
    private TextView submitTime;  //

    private Map<Integer, String> existLabels = new HashMap<Integer,String>();//输入内容的label

    private List<Todo> todo_list = new ArrayList<>();     //待办事项项
    private ListView listView;
    private int AddItemCount = 1;   //添加的待办事项个数
    private String input_to_do;     //输入的待办事项
    private TodoAdapter list_view_adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_do_item);
        initView();
        initTodoitems();


//        listView列表项中的  确定输入按钮  点击事件
        list_view_adapter.setOnItemInsureListener(new TodoAdapter.onItemInsureListener() {
            @Override
            public void onInsureClick(int position) {
                Todo todo = todo_list.get(position); //获得点击实例
//                点击确定按钮后,把输入的值赋给TextView显示,显示TextView控件,隐藏EditText,Button控件
                if (input_to_do != null && input_to_do.length() != 0){
                    todo.setItemName(input_to_do);
                    todo.setInputIsShow(View.GONE);
                    todo.setInsureIsShow(View.GONE);
                    todo.setTextIsShow(View.VISIBLE);
                    list_view_adapter.notifyDataSetChanged(); //数据刷新
                }else{
                    Toast.makeText(ToDoItem.this,"请输入待办事项!",Toast.LENGTH_SHORT).show();
                }
                existLabels.put(position,input_to_do);  //添加一条
            }
        });
        //新建待办事项长按删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ToDoItem.this);
                builder.setTitle("删除确认");
                builder.setMessage("您是否确认删除?");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除加载数据中的此项
                        todo_list.remove(position);
                        list_view_adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                return true;
            }
        });
    }


//    初始化控件
    private void initView() {
//        按钮
        completeBtn = findViewById(R.id.complete_to_do_item);       //完成按钮
        backBtn = findViewById(R.id.backBtn_to_do_item);    //返回
        addItems = findViewById(R.id.add_item_to_do_item);  //添加项
        addItems.setOnClickListener(this);
        completeBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
//        文本和输入
        submitTime = findViewById(R.id.date_to_do_item);    //显示时间

//        设置此刻时间显示
        String nowTime = new Date().toLocaleString();
        submitTime.setText(nowTime);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn_to_do_item:
                finish();
                startActivity(new Intent(ToDoItem.this,MainActivity.class));
                break;
            case R.id.complete_to_do_item:  //提交
                    for (int i=0;i<existLabels.size();i++){
                        //向数据库中添加文件地址信息fileName,filePath
                        FileAddress fileAddress = new FileAddress();
                        fileAddress.setType("ToDoItem");
                        fileAddress.setAddress(existLabels.get(i));     //把待办事项放到address
                        fileAddress.setAtRecycleBin(false);
                        fileAddress.setLabel("");
                        fileAddress.setCreatedTime(new Date().toLocaleString());
                        fileAddress.save();
                    }
                    //让首页显示刷新
                    Intent intent = new Intent(ToDoItem.this, MainActivity.class);
                    intent.putExtra("FragmentNumber",2);
                    startActivity(intent);
                    finish();
                break;
            case R.id.add_item_to_do_item:  //添加待办事项
                if (isEmptyLabel()){
                    Toast.makeText(ToDoItem.this, "请输入全部代办事项的内容!", Toast.LENGTH_SHORT).show();
                }else{
                    todo_list.add(new Todo("", View.VISIBLE, View.VISIBLE, View.GONE));
                    list_view_adapter.notifyDataSetChanged();      //数据刷新
                }
                break;
            default:
                    break;
        }
    }
//  加载数据
    private void initTodoitems() {
        list_view_adapter = new TodoAdapter(ToDoItem.this,R.layout.to_do_items_item,todo_list);
        listView = findViewById(R.id.to_do_item_list_view);
        listView.setAdapter(list_view_adapter);
//        for (int i=0;i<AddItemCount;i++){
            Todo items = new Todo("",View.VISIBLE,View.VISIBLE,View.GONE);
            todo_list.add(items);
//        }
    }
//  输入框的值
    public void saveEditTextData(int position, String str){
            //接收文本框输入值
            input_to_do = str;
    }
//是否有待办事项项内容为空
    private boolean isEmptyLabel(){
        boolean empty = true;
        //A：首先遍历集合中的键，然后再根据获得的键，获取对应的值。
        //先获取集合中的键的集合
        Set<Integer> set = existLabels.keySet();
        //进行键的集合的遍历
        for (Integer key : set){
            //根据得到的键，获取对应的值
            String inputText = existLabels.get(key);
            //如果有文本为空,跳出循环,返回true
            if (inputText == null && inputText.length()==0){
                empty = true;
                break;
            }else{
                empty = false;
                break;
            }
        }
        return empty;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(ToDoItem.this,MainActivity.class));
    }
}
