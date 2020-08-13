package com.little_wizard.tdc.util.draw;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class DrawQueue {
    private class Element{
        private Bitmap bitmap;
        private ArrayList<Coordinates> list;
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

    private final int maxSize = 10;
    private ArrayDeque<Element> queue;
    private ArrayList<ArrayList<Coordinates>> points;

    int minX, minY, maxX, maxY;

    public DrawQueue(){
        queue = new ArrayDeque<>(maxSize);
        points = new ArrayList<>();
        clear();
    }

    public void push(Bitmap bitmap, List list){
        ArrayList<Coordinates> newList = null;
        if(list != null){
            newList = new ArrayList<>();
            newList.addAll(list);
        }
        if(queue.size() == maxSize) queue.removeFirst();
        queue.add(new Element(bitmap.copy(Bitmap.Config.ARGB_8888, true), newList));
        points.add(newList);
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
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
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

    public boolean isClear(){
        if(points == null || points.get(points.size() - 1) == null) return true;
        else return false;
    }

    public ArrayList<Coordinates> getResult(){
        ArrayList<Coordinates> result = new ArrayList<>();
        for(ArrayList<Coordinates> L : points){
            if(L == null) continue;
            for(Coordinates c : L){
                minX = minX > (int)c.getX() ? (int)c.getX() : minX;
                minY = minY > (int)c.getY() ? (int)c.getY() : minY;
                maxX = maxX < (int)c.getX() ? (int)c.getX() : maxX;
                maxY = maxY < (int)c.getY() ? (int)c.getY() : maxY;
                result.add(new Coordinates(c.getX() / 1000, c.getY() / 1000));
            }
        }
        Log.i("result size", String.valueOf(result.size()));
        return result;
    }

    public int getStartX(){
        return minX;
    }

    public int getStartY(){
        return minY;
    }

    public int getHeight(){
        return maxY - minY;
    }

    public int getWidth(){
        return maxX - minX;
    }
}
