package com.little_wizard.tdc.util.draw;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ObjectBuffer {
    public class Element {
        Bitmap bitmap;
        List<Coordinates> list = new ArrayList<>();
        String axis;

        public Element(Bitmap bitmap, List<Coordinates> list, String axis) {
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (list == null) {
                this.list = null;
            } else this.list.addAll(list);
            this.axis = axis;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public List<Coordinates> getList() {
            return list;
        }
        public String getAxis() {
            return axis;
        }
    }

    String filename;
    String filepath;
    List<Element> buffer;
    Bitmap originalImage;
    String mode;

    public ObjectBuffer(String filepath, String filename, Bitmap bitmap) {
        this.filepath = filepath;
        this.filename = filename;
        originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        buffer = new ArrayList<>();
    }

    public void push(Bitmap bitmap, List<Coordinates> list, String axis) {
        List<Coordinates> newList = null;
        if (list != null) {
            newList = new ArrayList<>();
            newList.addAll(list);
        }
        buffer.add(new Element(bitmap.copy(Bitmap.Config.ARGB_8888, true), newList, axis));
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

    public void setName(String name) {
        filename = name;
    }

    public String getName() {
        return filename;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode(){
        return mode;
    }

    public Element getElement(int index) {
        return buffer.get(index);
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }
}
