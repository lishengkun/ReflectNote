package com.lskisme.reflexnote.main.toDoItemFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.lskisme.reflexnote.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自定义适配器类
 * @author 李胜坤
 * @date 2019/5/13 12:39
 */
public class ToDoItemSimpleAdapter extends SimpleAdapter {
    private List<Map<String, Object>> listItems = new ArrayList<>();
    private int resourceId;

    private Context mContext;
    public ToDoItemSimpleAdapter(final Context context, List<Map<String,Object >> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        resourceId = resource;
        listItems =  data;
        mContext = context;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder(view, position);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        //设置列表项中初始化时控件属性
        viewHolder.textContent.setText(listItems.get(position).get("待办事项内容").toString());
        viewHolder.completedBtn.setVisibility(View.VISIBLE);
        viewHolder.rightBtn.setVisibility(View.VISIBLE);
        //按钮点击事件
        viewHolder.completedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.completedBtn.setVisibility(View.GONE);
                viewHolder.rightBtn.setVisibility(View.GONE);
                onItemCompletedListener.onCompletedClick(position,viewHolder.textContent.getText().toString());
            }
        });
        return view;
    }
    class ViewHolder{
        Button completedBtn;
        Button rightBtn;
        TextView textContent;

        public ViewHolder(View view, int position){
            textContent = view.findViewById(R.id.to_do_item_main_title);
            //绑定按钮控件
            completedBtn = view.findViewById(R.id.to_do_item_main_item_icon);
            rightBtn = view.findViewById(R.id.to_do_item_main_icon2);
        }

    }
//    完成按钮
    public interface OnItemCompletedListener{
        void onCompletedClick(int position,String text);
    }
    private OnItemCompletedListener onItemCompletedListener;
    public void setOnItemCompletedListener(OnItemCompletedListener onItemCompletedListener){
        this.onItemCompletedListener = onItemCompletedListener;
    }
}
