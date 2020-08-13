package com.little_wizard.tdc.util.draw;

import android.os.Parcel;
import android.os.Parcelable;

public class Coordinates implements Parcelable {
    private float x;
    private float y;

    public Coordinates(){}
    public Coordinates(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Coordinates(Parcel in){
        x = in.readFloat();
        y = in.readFloat();
    }

    public static final Creator<Coordinates> CREATOR = new Creator<Coordinates>() {
        @Override
        public Coordinates createFromParcel(Parcel parcel) {
            return new Coordinates(parcel);
        }

        @Override
        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeFloat(x);
        dest.writeFloat(y);
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }
}
