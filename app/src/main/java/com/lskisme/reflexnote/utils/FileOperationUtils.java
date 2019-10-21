package com.lskisme.reflexnote.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件操作工具类
 * @author 李胜坤
 * @date 2019/5/8 22:17
 */
public class FileOperationUtils {

//    用户文件保存路径
    public static final String TextPATH = "/storage/emulated/0/Android/data/com.lskisme.reflexnote/files/Text";
    public static final String AudioPATH = "/storage/emulated/0/Android/data/com.lskisme.reflexnote/files/Audio";
    public static final String PicturePATH = "/storage/emulated/0/Android/data/com.lskisme.reflexnote/files/Picture";
    public static final String HandWritingPath = "/storage/emulated/0/Android/data/com.lskisme.reflexnote/files/HandWriting";
    /*
    *   在存储中创建文本文件
    *   静态方法,上下文
    * */
    public static boolean saveTextFile(String title, String content, Context context){
        boolean isSavedFile = false;
        File newDir = new File(getFilesPath(context)+"/Text/");
        if (!newDir.exists()){
            newDir.mkdir();
        }
        File newFile = new File(getFilesPath(context)+"/Text/"+title+".txt");
        //创建文本文件
        if (newFile.exists()){
            Toast.makeText(context, "同名文件已存在", Toast.LENGTH_SHORT).show();
            isSavedFile = false;
        }else{
            try {
                newFile.createNewFile();
                isSavedFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isSavedFile){
            //写入数据
            try {
                FileOutputStream fos = new FileOutputStream(getFilesPath(context)+"/Text/"+title+".txt");
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                fos.write(content.getBytes());
                bos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSavedFile;

    }

    /*
    *   获得新建文件的绝对地址
    * */
    public static String getFileAbsolutePath(String dir, String title, String extensionName){
        String absolutePath = null;
        switch (dir){
            case "Text":
                absolutePath = TextPATH + "/" + title + "." + extensionName;
                break;
            case "Picture":
                absolutePath = PicturePATH + "/" + title + "." + extensionName;
                break;
            case "Audio":
                absolutePath = AudioPATH + "/" + title + "." + extensionName;
                break;
            case "HandWriting":
                absolutePath = HandWritingPath + "/" +title + "." +extensionName;
            default:
                break;
        }
        return absolutePath;
    }
    public static String getPictureAbsolutePath(String title, String extensionName){
        return TextPATH + "/" + title + "." + extensionName;
    }

    //获得系统存储路径
    public static String getFilesPath( Context context ){
        String filePath ;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            filePath = context.getExternalFilesDir(null).getPath();
        }else {
            //外部存储不可用
            filePath = context.getFilesDir().getPath() ;
        }
        return filePath ;
    }
    /*删除指定名称加扩展名的文件*/
    public static void deleteFile(Context context,String dir, String fileName, String extensionName){
        File deleteFile = new File(FileOperationUtils.getFileAbsolutePath(dir,fileName, extensionName));
        if (deleteFile.exists()){     //如果存在删除
            deleteFile.delete();
        }else{
            Toast.makeText(context, "该文件已不存在!", Toast.LENGTH_SHORT).show();
        }
    }
}
