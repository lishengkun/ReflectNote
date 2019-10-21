package com.lskisme.reflexnote.main.NoteBookFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.sidebar.NoteBookShowContent;
import com.lskisme.reflexnote.utils.FileOperationUtils;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotebookFragment extends Fragment {
    private NoteBookSimpleAdapter noteBookSimpleAdapter;
    private List<String> titles = new ArrayList<>();
    private List<String> times = new ArrayList<>();
    private ListView mListView;
    private String AddedNewLabelName;

    private String content = null;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notebook_fragment,container,false);
        //此处可以对view及view中的子view进行设置
        mListView = view.findViewById(R.id.note_book_fragment_list_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//      把数据库中存放信息放到data集合中
        List<FileAddress> fileAddresses = LitePal.where("type = ?","Text").find(FileAddress.class);
        for (FileAddress fa : fileAddresses) {
            titles.add(fa.getName());
        }
        for (FileAddress fa : fileAddresses) {
            times.add(fa.getCreatedTime());
        }
        //创建一个list集合，list集合的元素是Map
        final List<Map<String, String>> listItems = new ArrayList<>();
        for (int i = titles.size()-1; i >=0 ; i--) {
            Map<String, String> listItem = new HashMap<>();
            String title1 = titles.get(i);
            String time1 = times.get(i);
            listItem.put("文本标题",title1 );
            listItem.put("文本创建时间",time1);
            //加入list集合
            listItems.add(listItem);
        }
//      使用simpleAdapter加载数据,绑定listview,textview显示数据
        noteBookSimpleAdapter = new NoteBookSimpleAdapter(getContext(), listItems,R.layout.note_book_main_item,
                new String[]{"文本标题","文本创建时间"},  new int[]{R.id.show_title_note_book_main, R.id.show_time_note_book_main});
        mListView.setAdapter(noteBookSimpleAdapter);
//listview子项点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //查找到此标题对应的文本地址
                List<FileAddress> noteBookValue = LitePal.select("address").where("name = ? and type = ?",listItems.get(position).get("文本标题"),"Text").find(FileAddress.class);
                //获取此标题记录的标签名
                List<FileAddress> labelName = LitePal.select("label").where("name = ? and type = ?",listItems.get(position).get("文本标题"),"Text").find(FileAddress.class);
                // 读取地址指向的文件内容,传给下个页面显示
                File in = new File(noteBookValue.get(0).getAddress());
                try {
                    FileInputStream fis = new FileInputStream(in);
                    byte[] bytes = new byte[1024];
                    int len =0;
                    while((len = fis.read(bytes))!=-1){
                        content = new String(bytes,0,len);
                    }
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //跳转到笔记本详情页
                Intent intent = new Intent(getActivity(), NoteBookShowContent.class);
                intent.putExtra("标题",listItems.get(position).get("文本标题"));
                intent.putExtra("内容",content);
                intent.putExtra("标签名",labelName.get(0).getLabel());
                startActivity(intent);
            }
        });
//长按事件
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("删除确认");
                builder.setMessage("您是否确认删除?");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = listItems.get(position).get("文本标题");
                        //删除文件
                        FileOperationUtils.deleteFile(getContext(),"Text", title, "txt");
                        //直接删除本条纪录,刷新适配器数据
                        LitePal.deleteAll(FileAddress.class, "name = ? and type = ?",title,"Text");
                        //删除加载数据中的此项
                        listItems.remove(position);
                        noteBookSimpleAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
