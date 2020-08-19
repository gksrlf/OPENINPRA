package com.little_wizard.tdc.util.draw;

import android.graphics.Bitmap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class DrawQueue {

    private class Element {
        private Bitmap bitmap;
        private Coordinates start;
        private Coordinates end;

        public Element(Bitmap bitmap, Coordinates start, Coordinates end) {
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            this.start = start;
            this.end = end;
        }

        public Coordinates getStart(){
            return start;
        }

        public Coordinates getLast(){
            return end;
        }
    }

    private final int maxSize = 10;
    // bitmap, axis X List, axis Y List
    private ArrayDeque<Element> queue;

    // axis : X
    private ArrayList<ArrayList<Coordinates>> listX;

    // axis : Y
    private ArrayList<ArrayList<Coordinates>> listY;

    int minX, minY, maxX, maxY;

    public DrawQueue() {
        queue = new ArrayDeque<>(maxSize);
        listX = new ArrayList<>();
        listY = new ArrayList<>();
        clear();
    }

    // 사용자가 그린 이미지상의 좌표리스트 arrayList에 추가
    public void push(List listX, List listY) {
        ArrayList<Coordinates> newListX = null;
        if (listX != null) {
            newListX = new ArrayList<>(listX);
        }

        ArrayList<Coordinates> newListY = null;
        if (listY != null) {
            newListY = new ArrayList<>(listY);
        }
        this.listX.add(newListX);
        this.listY.add(newListY);
    }

    // 이미지의 바로 전 상태 저장 // undo
    public void push(Bitmap bitmap, Coordinates start, Coordinates end) {
        if (queue.size() == maxSize) queue.removeFirst();
        queue.add(new Element(bitmap.copy(Bitmap.Config.ARGB_8888, true), start, end));
    }

    // 이미지를 전 상태로 되돌림
    public void undo() {
        if (queue.size() > 1) {
            queue.removeLast();
            listX.remove(listX.size() - 1);
            listY.remove(listY.size() - 1);
        }
    }

    public void clear() {
        queue.clear();
        listX.clear();
        listY.clear();
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
    }

    public int size() {
        return queue.size();
    }

    public Bitmap getPreviousBitmap() {
        return queue.getLast().bitmap;
    }

    public Coordinates getLastPoint() {
        if (queue.size() > 0 && queue.getLast() != null) {
            return queue.getLast().getLast();
        }
        return null;
    }

    // 비대칭 x축 기준 변환
    public Coordinates getLastPointX(){
        if(listX.size() > 0 && queue.getLast().getLast() != null){
            int size = queue.size();
            if(size > 0)
                return queue.getLast().getLast();
            else return null;
        }
        return null;
    }

    // 비대칭 y축 기준 변환
    public Coordinates getLastPointY(){
        if(listX.size() > 0 && queue.getLast().getLast() != null){
            int size = queue.size();
            if(size > 0)
                return queue.getLast().getLast();
            else return null;
        }
        return null;
    }

    public boolean isClear() {
        if(queue.getLast().getLast() == null) return true;
        else return false;
    }

    // ObjectBuffer의 buffer에 넘겨줌
    public ArrayList<Coordinates> getResultX() {
        ArrayList<Coordinates> fullList = new ArrayList<>();
        for (ArrayList<Coordinates> list : listX) {
            if(list != null) fullList.addAll(list);
        }

        Iterator<Coordinates> it = fullList.iterator();
        Coordinates previous = new Coordinates(-1, -1);
        Coordinates lastEquals = null;
        while(it.hasNext()){
            Coordinates current = it.next();
            if(previous.getX() == current.getX()) {
                lastEquals = current;
                it.remove();
            }
            else{
                if(lastEquals != null){
                    float x = previous.getX();
                    float y = (previous.getY() + lastEquals.getY()) / 2;
                    previous = new Coordinates(x, y);
                }
                previous = current;
                lastEquals = null;
            }
        }

        ArrayList<Coordinates> result = new ArrayList<>();
        for (Coordinates c : fullList) {
            minX = Math.min(minX, (int) c.getX());
            minY = Math.min(minY, (int) c.getY());
            maxX = Math.max(maxX, (int) c.getX());
            maxY = Math.max(maxY, (int) c.getY());
            result.add(new Coordinates(c.getX() / 1000, c.getY() / 1000));
        }
        return result;
    }

    // ObjectBuffer의 buffer에 넘겨줌
    //대칭 모드에서 사용
    public ArrayList<Coordinates> getResultY() {
        ArrayList<Coordinates> fullList = new ArrayList<>();
        for (ArrayList<Coordinates> list : listY) {
            if(list != null) fullList.addAll(list);
        }

        Iterator<Coordinates> it = fullList.iterator();
        Coordinates previous = new Coordinates(-1, -1);
        Coordinates lastEquals = null;
        while(it.hasNext()){
            Coordinates current = it.next();
            if(previous.getY() == current.getY()) {
                lastEquals = current;
                it.remove();
            }
            else{
                if(lastEquals != null){
                    float x = previous.getX();
                    float y = (previous.getY() + lastEquals.getY()) / 2;
                    previous = new Coordinates(x, y);
                }
                previous = current;
                lastEquals = null;
            }
        }

        ArrayList<Coordinates> result = new ArrayList<>();
        for (Coordinates c : fullList) {
            minX = Math.min(minX, (int) c.getX());
            minY = Math.min(minY, (int) c.getY());
            maxX = Math.max(maxX, (int) c.getX());
            maxY = Math.max(maxY, (int) c.getY());
            result.add(new Coordinates(c.getX() / 1000, c.getY() / 1000));
        }
        return result;
    }

    // 비대칭 모드
    //x값을 기준으로 점의 쌍 구함
    public ArrayList<Coordinates> getPairX() {
        HashMap<Float, TreeSet<Float>> hashMap = new HashMap<>();
        ArrayList<Coordinates> fullList = new ArrayList<>();
        for (ArrayList<Coordinates> list : listX) {
            if(list != null) fullList.addAll(list);
        }

        Iterator<Coordinates> it = fullList.iterator();
        Coordinates previous = new Coordinates(-1, -1);
        Coordinates lastEquals = null;
        while(it.hasNext()){
            Coordinates current = it.next();
            if(previous.getX() == current.getX()) {
                lastEquals = current;
                it.remove();
            }
            else{
                if(lastEquals != null){
                    float x = previous.getX();
                    float y = (previous.getY() + lastEquals.getY()) / 2;
                    previous = new Coordinates(x, y);
                }
                previous = current;
                lastEquals = null;
            }
        }

        //X값 기준 정렬
        for (Coordinates c : fullList) {
            float key = c.getX() / 1000;
            float value = c.getY() / 1000;
            if (!hashMap.containsKey(key)) {
                TreeSet<Float> set = new TreeSet<>();
                set.add(value);
                hashMap.put(key, set);
            } else {
                hashMap.get(key).add(value);
            }

            minX = Math.min(minX, (int) c.getX());
            minY = Math.min(minY, (int) c.getY());
            maxX = Math.max(maxX, (int) c.getX());
            maxY = Math.max(maxY, (int) c.getY());
        }

        TreeMap<Float, TreeSet<Float>> treeMap = new TreeMap<>(hashMap);
        ArrayList<Coordinates> result = new ArrayList<>();
        for(Float x : treeMap.keySet()){
            if(treeMap.get(x).first() == treeMap.get(x).last()) continue;
            result.add(new Coordinates(x, treeMap.get(x).last()));
            result.add(new Coordinates(x, treeMap.get(x).first()));
        }
        return result;
    }

    // 비대칭 모드
    //y값을 기준으로 점의 쌍 구함
    public ArrayList<Coordinates> getPairY() {
        HashMap<Float, TreeSet<Float>> hashMap = new HashMap<>();
        ArrayList<Coordinates> fullList = new ArrayList<>();
        for (ArrayList<Coordinates> list : listY) {
            if(list != null) fullList.addAll(list);
        }

        Iterator<Coordinates> it = fullList.iterator();
        Coordinates previous = new Coordinates(-1, -1);
        Coordinates lastEquals = null;
        while(it.hasNext()){
            Coordinates current = it.next();
            if(previous.getY() == current.getY()) {
                lastEquals = current;
                it.remove();
            }
            else{
                if(lastEquals != null){
                    float x = (previous.getX() + lastEquals.getX()) / 2;
                    float y = previous.getY();
                    previous = new Coordinates(x, y);
                }
                previous = current;
                lastEquals = null;
            }
        }

        //y값 기준 정렬
        for (Coordinates c : fullList) {
            float key = c.getY() / 1000;
            float value = c.getX() / 1000;
            if (!hashMap.containsKey(key)) {
                TreeSet<Float> set = new TreeSet<>();
                set.add(value);
                hashMap.put(key, set);
            } else {
                hashMap.get(key).add(value);
            }

            minX = Math.min(minX, (int) c.getX());
            minY = Math.min(minY, (int) c.getY());
            maxX = Math.max(maxX, (int) c.getX());
            maxY = Math.max(maxY, (int) c.getY());
        }

        TreeMap<Float, TreeSet<Float>> treeMap = new TreeMap<>(hashMap);
        ArrayList<Coordinates> result = new ArrayList<>();
        for(Float y : treeMap.keySet()){
            if(treeMap.get(y).first() == treeMap.get(y).last()) continue;
            result.add(new Coordinates(treeMap.get(y).last(), y));
            result.add(new Coordinates(treeMap.get(y).first(), y));
        }
        return result;
    }


    public int getStartX() {
        return minX;
    }

    public int getStartY() {
        return minY;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public int getWidth() {
        return maxX - minX;
    }
}
