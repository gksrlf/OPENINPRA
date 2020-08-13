package com.little_wizard.tdc.util.draw;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ObjectBuffer {
    public class Element {
        Bitmap bitmap;
        List<Coordinates> list = new ArrayList<>();

        public Element(Bitmap bitmap, List<Coordinates> list) {
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (list == null) {
                this.list = null;
            } else this.list.addAll(list);
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
        public List<Coordinates> getList(){
            return list;
        }
    }

    String filename;
    String filepath;
    List<Element> buffer;

    public ObjectBuffer(String filepath, String filename) {
        this.filepath = filepath;
        this.filename = filename;
        buffer = new ArrayList<>();
    }

    public void push(Bitmap bitmap, List<Coordinates> list) {
        List<Coordinates> newList = null;
        if (list != null) {
            newList = new ArrayList<>();
            newList.addAll(list);
        }
        buffer.add(new Element(bitmap.copy(Bitmap.Config.ARGB_8888, true), newList));
    }

    public void remove(int index) {
        buffer.remove(index);
    }

    public List<Element> getBuffer() {
        return buffer;
    }

    public void upload() {
        //TODO: 파일 이름 filename - index 꼴로 bitmap, text 로 filepath에 저장
        //TODO: 파일 이름 filename - index 꼴로 bitmap, text 업로드
    }

    public String getName(){return filename;}
    public Element getElement(int index){
        return buffer.get(index);
    }
}
