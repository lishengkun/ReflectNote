package com.lskisme.reflexnote.sidebar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签所在页
 * @author 李胜坤
 * @date 2019/6/2 18:16
 */
public class ShowLabelContent extends AppCompatActivity {
    private SimpleAdapter simpleAdapter;
    private List<Map<String,String>> data = new ArrayList<>();
    private ListView listView;
    private List<String> titleArr = new ArrayList<>();
    private List<String> timeArr = new ArrayList<>();
    private List<String> contentArr = new ArrayList<>();
    private List<String> addressArr = new ArrayList<>();
    private String label;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_label_content);

        label = getIntent().getStringExtra("label");
        listView = findViewById(R.id.show_label_content_list_view);
        //获取标签名为label的所有记录的title和content
        List<FileAddress> titleList = LitePal.select("name").where("label = ? and type = ?",label,"Text").find(FileAddress.class);
        List<FileAddress> timeList = LitePal.select("createdTime").where("label = ? and type = ?",label, "Text").find(FileAddress.class);
        List<FileAddress> addressesList = LitePal.select("address").where("label = ? and type = ?",label,"Text").find(FileAddress.class);
        for (FileAddress title : titleList){
            titleArr.add(title.getName());
        }
        for (FileAddress time :timeList){
            timeArr.add(time.getCreatedTime());
        }
        for (FileAddress address : addressesList){
            addressArr.add(address.getAddress());
        }
        for (int i=0;i<titleArr.size();i++){
            // 读取地址指向的文件内容,传给下个页面显示
            String s = null;
            File in = new File(addressArr.get(i));
            try {
                FileInputStream fis = new FileInputStream(in);
                byte[] bytes = new byte[1024];
                int len =0;
                while((len = fis.read(bytes))!=-1){
                    s = new String(bytes,0,len);
                }
                contentArr.add(s);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i=0;i<titleArr.size();i++){
            Map<String,String> map = new HashMap<>();
            map.put("title",titleArr.get(i));
            map.put("time",timeArr.get(i));
            data.add(map);
        }
        simpleAdapter = new SimpleAdapter(ShowLabelContent.this,data,R.layout.show_label_content_item,
                new String[]{"title","time"},new int[]{R.id.show_label_content_content_item_title,R.id.show_label_content_content_item_time});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //把title和content传送
                Intent intent = new Intent(ShowLabelContent.this, NoteBookShowContent.class);
                intent.putExtra("标题",titleArr.get(position));
                intent.putExtra("内容",contentArr.get(position));
                intent.putExtra("标签名",label);
                startActivity(intent);
            }
        });
    }
}
