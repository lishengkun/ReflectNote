package com.lskisme.reflexnote.utils;

import android.util.Log;

import com.lskisme.reflexnote.litepal.FileAddress;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作工具类
 * @author 李胜坤
 * @date 2019/5/15 20:24
 */
public class LitePalOperation {
    private static String[] name = new String[0] ;
    /*
    *   添加数据
    * */

    /*
    *   删除数据
    * */
    /*
    *   查询数据
    * */

    /*获得所有label*/
    public static List<String> queryAllLabelName(){
        List<FileAddress> labelItem = LitePal.select("label").where("type = ? and label != ?","Text"," ").find(FileAddress.class);
        List<String> labelNames = new ArrayList<>();
        for (FileAddress fa : labelItem){
            labelNames.add(fa.getLabel());
        }
        return labelNames;
    }
    /*获得所有Picture的绝对路径*/
    public static String[] getAllPicturePaths(){
        String[] returnPicPaths = {""};
        List<FileAddress> picItem = LitePal.select("address").where("type = ?","Picture").find(FileAddress.class);
        List<String> picPaths = new ArrayList<>();
        for (FileAddress fa:picItem){
            picPaths.add(fa.getAddress());
        }
        returnPicPaths = picPaths.toArray(new String[picPaths.size()]);
        return returnPicPaths;
    }
    /*获得所有图片的文件名*/
    public static String[] getAllPictureName(){
        String[] returnPicNames = {""};
        List<FileAddress> picItem = LitePal.select("name").where("type = ?","Picture").find(FileAddress.class);
        List<String> picNames = new ArrayList<>();
        for (FileAddress fa:picItem){
            picNames.add(fa.getName());
        }
        returnPicNames = picNames.toArray(new String[picNames.size()]);
        return returnPicNames;
    }

    /*
    *
    * */
    /*获得所有绘图图片的绝对路径*/
    public static String[] getAllHandWritingPaths(){
        String[] returnPicPaths = {""};
        List<FileAddress> picItem = LitePal.select("address").where("type = ? and address !=?","HandWriting"," ").find(FileAddress.class);
        List<String> picPaths = new ArrayList<>();
        for (FileAddress fa:picItem){
            picPaths.add(fa.getAddress());
        }
        returnPicPaths = picPaths.toArray(new String[picPaths.size()]);
        for (int i=0;i<picPaths.size();i++){
            Log.d("picpath","图片地址:"+picPaths.get(i));
        }
        Log.d("picpath","//**************");
        return returnPicPaths;
    }
    /*获得所有图片的文件名*/
    public static String[] getAllHandWritingName(){
        String[] returnPicNames = {""};
        List<FileAddress> picItem = LitePal.select("name").where("type = ?","HandWriting").find(FileAddress.class);
        List<String> picNames = new ArrayList<>();
        for (FileAddress fa:picItem){
            picNames.add(fa.getName());
        }
        returnPicNames = picNames.toArray(new String[picNames.size()]);
        return returnPicNames;
    }

    /*
    *   获得存在的所有标签名
    * */
    public static String[] getAllLabelNames(){
        String[] returnLabelNames = new String[0];
        List<FileAddress> nameItem = LitePal.select("label").where("type = ? and label != ?","Text","").find(FileAddress.class);
        if (nameItem.size()!=0){
            List<String> labelNames = new ArrayList<>();
            for (FileAddress fa:nameItem){
                labelNames.add(fa.getLabel());
            }
            returnLabelNames = labelNames.toArray(new String[labelNames.size()]);
        }
        return returnLabelNames;
    }
    //返回去重复的标签名
    public static String[] getLabelNameList(){
        if (getAllLabelNames().length!=0){
            if (getAllLabelNames().length>1){
                String[] names = new String[getAllLabelNames().length];
                for (int i=0;i<getAllLabelNames().length;i++){
                    names[i] = getAllLabelNames()[i];
                }
                int labelNum = names.length;
                int num = 0;
                for (int i=0;i<names.length-1;i++){    //从第一个到倒数第二个
                    for (int j=i+1;j<names.length;j++){    //分别和后边的进行比较
                        if (names[i].equals(names[j])&&!names[i].equals("*")){    //如果有相同的标签名,把后面的那个变成"*"
                            names[j] = "*";
                            labelNum--;
                        }
                    }
                }
                name = new String[labelNum];
                for (int i=0;i<names.length;i++){
                    if (!names[i].equals("*")){
                        name[num] = names[i];
                        num++;
                    }
                }
            }
            if (getAllLabelNames().length==1){
                name = new String[1];
                name[0] = getAllLabelNames()[0];
            }
        }
        return name;
    }
    /*
    *   获得标签名和对应的数量
    *   return 标签数量键,标签名值
    * */
    /*5个数 0 1 2 3 4
    * 0 1
    * 1 2
    * 2 3
    * 3 4
    * */
    /*
    *   获得不重复标签名与之对应的个数
    * */
    public static List<Map<String,String>> getLabelSameNameNumber(){
        List<Map<String,String>> labelList = new ArrayList<>();
        int num = 1;
        if (getAllLabelNames().length!=0){
            if (getAllLabelNames().length>1) {
                String[] names = new String[getAllLabelNames().length];
                for (int i=0;i<getAllLabelNames().length;i++){
                    names[i] = getAllLabelNames()[i];
                }
                // 光伏发电 光伏发电 营业厅
                //遍历去重复标签名
                for (int i = 0; i < names.length ; i++) {    //从第一个到倒数第二个
                    if (!names[i].equals("*")){
                        if (i<names.length-1){
                            for (int j = i + 1; j < names.length; j++) {    //分别和后边的进行比较
                                if (names[i].equals(names[j])) {    //如果有相同的标签名,把后面的那个变成"*"
                                    names[j] = "*";
                                    num++;  //记录每个标签名出现的次数
                                }
                            }
                            if (!names[i].equals("*")){
                                Map<String,String> map = new HashMap<>();
                                map.put("labelName",names[i]);
                                map.put("labelNumber",num+"");
                                labelList.add(map);
                                num = 1;
                            }
                        }else{
                            Map<String,String> map = new HashMap<>();
                            map.put("labelName",names[names.length-1]);
                            map.put("labelNumber","1");
                            labelList.add(map);
                            num = 1;
                        }
                    }
                }
            }
            if (getAllLabelNames().length==1){
                Map<String,String> map = new HashMap<>();
                map.put("labelName",getAllLabelNames()[0]);
                map.put("labelNumber",num+"");
                labelList.add(map);
            }
        }
        return labelList;
    }
}
