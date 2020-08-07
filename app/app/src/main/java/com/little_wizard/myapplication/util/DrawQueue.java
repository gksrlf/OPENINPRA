package com.little_wizard.myapplication.util;

import android.graphics.Bitmap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class DrawQueue {
    private class Element{
        private Bitmap bitmap;
        private ArrayList<Coordinates> list;
        public Element(Bitmap bitmap, List list){
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            this.list = (ArrayList<Coordinates>) list;
        }
    }

    private final int maxSize = 10;
    private ArrayDeque<Element> queue;
    private ArrayList<ArrayList<Coordinates>> points;

    public DrawQueue(){
        queue = new ArrayDeque<>(maxSize);
        points = new ArrayList<>();
    }

    public void push(Bitmap e, List list){
        if(queue.size() == maxSize) queue.removeFirst();
        queue.add(new Element(e.copy(Bitmap.Config.ARGB_8888, true), list));
        points.add((ArrayList<Coordinates>)list);
    }

    public void undo(){
        if(queue.size() > 1){
            queue.removeLast();
            points.remove(points.size() - 1);
        }
    }

    public void clear(){
        queue.clear();
        points.clear();
    }

    public int size(){
        return queue.size();
    }
    public Bitmap getPreviousBitmap(){
        return queue.getLast().bitmap;
    }

    public Coordinates getLastPoint(){
        if(queue.size() > 0 && queue.getLast().list != null){
            int size = queue.getLast().list.size();
            if(size > 0)
                return queue.getLast().list.get(size - 1);
            else return null;
        }
        return null;
    }

    public ArrayList<Coordinates> getResult(){
        ArrayList<Coordinates> result = new ArrayList<>();
        for(Element e : queue){
            for(Coordinates c : (ArrayList<Coordinates>)e.list){
                result.add(new Coordinates(c.getX() / 1000, c.getY() / 1000));
            }
        }
        return result;
    }
}
