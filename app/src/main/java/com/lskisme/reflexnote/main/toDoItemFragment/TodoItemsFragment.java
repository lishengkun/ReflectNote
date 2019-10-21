package com.lskisme.reflexnote.main.toDoItemFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
*   渲染待办事项界面数据
*   从数据库查询所有type为TodoItem的address,获取数据放到集合中
* */
public class TodoItemsFragment extends Fragment {
    private ToDoItemSimpleAdapter mySimpleAdapter;
    private List<String> data = new ArrayList<>();
    private ListView mListView;
    private boolean isGetData = false;  //数据刷新

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todoitems_fragment,container,false);
        mListView = view.findViewById(R.id.list_view_to_do_item);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //把数据库中存放信息放到data集合中
        List<FileAddress> fileAddresses = LitePal.where("type = ?","ToDoItem").find(FileAddress.class);
        for (FileAddress fa : fileAddresses) {
            data.add(fa.getAddress());
        }
        //创建一个list集合，list集合的元素是Map
        final List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = data.size()-1; i >=0 ; i--) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("待办事项内容", data.get(i));
            //加入list集合
            listItems.add(listItem);
        }
        //使用simpleAdapter加载数据,textview显示,设置按钮点击事件
        mySimpleAdapter = new ToDoItemSimpleAdapter(getContext(),listItems,R.layout.to_do_item_main_item,new String[]{"待办事项内容"},new int[]{R.id.to_do_item_main_title});
        mListView.setAdapter(mySimpleAdapter);
        mySimpleAdapter.setOnItemCompletedListener(new ToDoItemSimpleAdapter.OnItemCompletedListener() {
            @Override
            public void onCompletedClick(int position, String text) {
                //直接删除本条纪录,刷新适配器数据
                LitePal.deleteAll(FileAddress.class, "address = ? and type = ?",text,"TodoItem");
                listItems.remove(position);
                mySimpleAdapter.notifyDataSetChanged();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "点击方框完成该事项!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}