package com.lskisme.reflexnote.litepal;

import org.litepal.crud.LitePalSupport;

/**
 * 存储文件信息表
 * 文本文件,一个文件一条记录,需要文件存储
 * 图片,一个图片一条记录,需要文件存储
 * 语音,一个文件,一条记录,需要文件存储
 * 待办事项,一个待办事项,一条记录,存储到数据库中地址中
 *
 * @author 李胜坤
 * @date 2019/5/8 22:09
 */
public class FileAddress extends LitePalSupport {
//一条数据库记录的组成

    private int id;      //文件id

    private String type;         //文件类型

    private String name;         //存储文件名

    private String label;        //标签

    private String address;      //文件存储绝对地址,标签存储到地址中

    private boolean isAtRecycleBin;   //是否放到数据库里

    private String createdTime;     //文件创建时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean getIsAtRecycleBin() {
        return isAtRecycleBin;
    }

    public void setAtRecycleBin(boolean atRecycleBin) {
        this.isAtRecycleBin = atRecycleBin;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
