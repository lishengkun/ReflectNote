package com.lskisme.reflexnote.main.NoteBookFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.lskisme.reflexnote.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李胜坤
 * @date 2019/5/14 19:28
 */
public class NoteBookSimpleAdapter extends SimpleAdapter {
    private List<Map<String, String>> mapList = new ArrayList<>();
    private Map<String, String> map = new HashMap<>();
    private int resourceId;
    private Context mContext;

    public NoteBookSimpleAdapter(Context context, List<Map<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        resourceId = resource;
        mapList = data;
        mContext = context;
    }
    @Override
    public int getCount() {
        return mapList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public Object getItem(int position) {
        return mapList.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //控制控件加载时机,减少次数,优化性能
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else{
            view =convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        //设置列表项中初始化时控件属性
        viewHolder.showTitle.setText(mapList.get(position).get("文本标题"));
        viewHolder.showTime.setText(mapList.get(position).get("文本创建时间"));
        return view;
    }
    class ViewHolder{
        TextView showTitle,showTime;
        public ViewHolder(View view){
            showTitle = view.findViewById(R.id.show_title_note_book_main);
            showTime = view.findViewById(R.id.show_time_note_book_main);
        }
    }

}
