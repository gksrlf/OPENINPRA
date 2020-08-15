package org.andresoviedo.util;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ObjectManager {
    // Key : mainObject,  Value : points
    static HashMap<Integer, ArrayList<Integer>> parts = new HashMap<>();

    static HashMap<Object3DData, ArrayList<Object3DData>> parts2 = new HashMap<>();

    static HashSet<Object3DData> points = new HashSet<>();
    static HashSet<Object3DData> mainObjects = new HashSet<>();

    static public void addParts(Integer mainObj, List pointsList){
        ArrayList<Integer> list = new ArrayList<>(pointsList);
        parts.put(mainObj, list);
    }

    static public void addParts(Object3DData mainObj, List pointsList){
        ArrayList<Object3DData> list = new ArrayList<>(pointsList);
        for(Object3DData obj : list) points.add(obj);
        mainObjects.add(mainObj);
        parts2.put(mainObj, list);
    }

    static public Object3DData getObjByPoint(Object3DData point){
        for(Object3DData obj : parts2.keySet()){
            for(Object3DData subObj : Objects.requireNonNull(parts2.get(obj))){
                if(subObj.equals(point)) return obj;
            }
        }
        return null;
    }

    static public boolean isPoint(Object3DData obj){
        return points.contains(obj);
    }
}
