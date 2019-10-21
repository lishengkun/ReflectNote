package com.lskisme.reflexnote.levitationButton.toDoItem;

public class Todo {
    private String itemName = null;    //待办事项名
    private int inputIsShow;    //显示状态
    private int textIsShow;
    private int insureIsShow;

    public Todo(String itemName,int inputIsShow,int insureIsShow, int textIsShow) {
        this.inputIsShow = inputIsShow;
        this.insureIsShow =insureIsShow;
        this.textIsShow =textIsShow;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public void setInputIsShow(int inputIsShow) {
        this.inputIsShow = inputIsShow;
    }
    public void setInsureIsShow(int insureIsShow) {
        this.insureIsShow = insureIsShow;
    }
    public void setTextIsShow(int textIsShow) {
        this.textIsShow = textIsShow;
    }


    public int getInputIsShow() {
        return inputIsShow;
    }
    public String getItemName() {
        return itemName;
    }
    public int getInsureIsShow() {
        return insureIsShow;
    }
    public int getTextIsShow() {
        return textIsShow;
    }
}
