package com.lskisme.reflexnote.main.KeepGood;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.lskisme.reflexnote.R;
import com.lskisme.reflexnote.litepal.FileAddress;
import com.lskisme.reflexnote.main.ShowPicture;
import com.lskisme.reflexnote.utils.FileOperationUtils;
import com.lskisme.reflexnote.utils.LitePalOperation;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeepGoodFragment extends Fragment {
    private GridView gridView;
    private SimpleAdapter simpleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.keep_good_fragment,container,false);
        gridView = view.findViewById(R.id.gridview);
        return view;
    }

    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //如果数据不为空
        if (LitePalOperation.getAllPicturePaths().length != 0){
            String[] filePathData = LitePalOperation.getAllPicturePaths();

            final List<Map<String, Object>> imageList = new ArrayList<>();
            for (int i=0;i<filePathData.length;i++){
                Map<String,Object> map = new HashMap<>();
                Bitmap bitmap = BitmapFactory.decodeFile(filePathData[i]);
                map.put("localPicture",bitmap);
                imageList.add(map);
            }
            simpleAdapter = new SimpleAdapter(getContext(),imageList,R.layout.gird_view_item,
                    new String[]{"localPicture"},
                    new int[]{R.id.gird_view_item_image});
            //实现ViewBinder()这个接口，在里面定义数据和视图的匹配关系
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if ((view instanceof ImageView)&&(data instanceof Bitmap)){
                        ImageView iv = (ImageView) view;
                        Bitmap bitmap = (Bitmap) data;
                        iv.setImageBitmap(bitmap);
                        return true;
                    }
                    return false;
                }
            });
            gridView.setAdapter(simpleAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //跳转到图片展示分享页面
                    Intent toShow = new Intent(getActivity(), ShowPicture.class);
                    toShow.putExtra("imagePosition",position);
                    toShow.putExtra("from",2);
                    startActivity(toShow);
                }
            });
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("删除确认");
                    builder.setMessage("您是否确认删除?");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除存储的文件
                            FileOperationUtils.deleteFile(getContext(),"Picture", LitePalOperation.getAllPictureName()[position],"jpg");
                            //删除数据库记录
                            LitePal.deleteAll(FileAddress.class,"address = ? and type = ?",LitePalOperation.getAllPicturePaths()[position],"Picture");
                            imageList.remove(position);
                            simpleAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
