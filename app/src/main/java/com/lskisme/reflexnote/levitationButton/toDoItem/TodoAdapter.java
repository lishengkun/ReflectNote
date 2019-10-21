package com.lskisme.reflexnote.levitationButton.toDoItem;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lskisme.reflexnote.R;

import java.util.List;

public class TodoAdapter extends ArrayAdapter<Todo> {
    private int todo_item_resourseId;
    //构造函数,将上下文,ListView的子项布局的id,和数据都传进来
    public TodoAdapter(Context context, int resourceId, List<Todo> objects) {
        super(context, resourceId, objects);
        todo_item_resourseId = resourceId;
    }
    //重写getView方法,当子项滚动到屏幕中被调用,getItem方法获得当前项实例,然后用LayoutInflate为子项加载布局
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Todo todo = (Todo) getItem(position);
        //        将加载好的布局进行缓存,提升效率
        View view;
        ViewHolder viewHolder;
        if (convertView == null){   //如果没有初始化控件
           view = LayoutInflater.from(getContext()).inflate(todo_item_resourseId,parent,false);
           viewHolder = new ViewHolder(view, position);
           view.setTag(viewHolder);

//            将ViewHolder存在View中
            view.setTag(viewHolder);
        }else {     //初始化了,就从view中获取
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();    //从view中获取ViewHolder
        }
//        设置初始化时控件属性
        viewHolder.show_content.setText(todo.getItemName());
        viewHolder.insure_content.setVisibility(todo.getInsureIsShow());
        viewHolder.input_content.setVisibility(todo.getInputIsShow());
        viewHolder.show_content.setVisibility(todo.getTextIsShow());

        //       确定按钮点击事件
        viewHolder.insure_content.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mOnItemInsureListener.onInsureClick(position);
            }
        });
        return view;
    }
    //view实例化缓存
    class ViewHolder{
        ImageView twoLine;
        ImageView logo;
        TextView show_content;
        EditText input_content;
        Button insure_content;

        //构造函数获得控件实例
        public ViewHolder(View view,int position){
            twoLine = view.findViewById(R.id.to_do_item_item);
            show_content = view.findViewById(R.id.show_name_to_do_item);
            input_content = view.findViewById(R.id.input_name_to_do_item);
            insure_content = view.findViewById(R.id.insure_input_content_to_do_item);

            //输入框监听事件
            input_content.setTag(position);   //存Tag值
            input_content.addTextChangedListener(new TextSwitcher(this)); //编辑框文本监听
        }
    }
    //    编辑框文本监听
    class TextSwitcher implements TextWatcher {
        private ViewHolder viewHolder;
        public TextSwitcher(ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence str, int start, int before, int count) {
            int position = (int) viewHolder.input_content.getTag(); //取Tag值
            ((ToDoItem)getContext()).saveEditTextData(position, str.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
//    输入框信息确定按钮点击事件借口
//    确定按钮的监听接口
    private onItemInsureListener mOnItemInsureListener;
    public interface onItemInsureListener{
        void onInsureClick(int position);
    }
    public void setOnItemInsureListener(onItemInsureListener mOnItemInsureListener){
        this.mOnItemInsureListener = mOnItemInsureListener;
    }
}
