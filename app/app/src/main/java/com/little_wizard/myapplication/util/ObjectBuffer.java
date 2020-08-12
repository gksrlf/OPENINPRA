package com.little_wizard.myapplication.util;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ObjectBuffer {
    private class Element{
        Bitmap bitmap;
        ArrayList<Coordinates> list;
        public Element(Bitmap bitmap, List list){
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            this.list = new ArrayList<>();
            if(list == null){
                this.list = null;
            }else{
                this.list.addAll(list);
            }
        }
    }
    String filename;
    String filepath;
    ArrayList<Element> buffer;

    public ObjectBuffer(String filepath, String filename){
        this.filepath = filepath;
        this.filename = filename;
        buffer = new ArrayList<>();
    }

    public void push(Bitmap bitmap, List list){
        ArrayList<Coordinates> newList = null;
        if(list != null){
            newList = new ArrayList<>();
            newList.addAll(list);
        }
        buffer.add(new Element(bitmap.copy(Bitmap.Config.ARGB_8888, true), newList));
    }

    public void remove(int index){
        buffer.remove(index);
    }

    public List getBuffer(){
        return buffer;
    }

    public void upload(){
        //TODO: 파일 이름 filename - index 꼴로 bitmap, text 로 filepath에 저장
        //TODO: 파일 이름 filename - index 꼴로 bitmap, text 업로드
    }
}
