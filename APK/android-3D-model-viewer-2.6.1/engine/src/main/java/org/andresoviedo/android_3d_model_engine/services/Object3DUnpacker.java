package org.andresoviedo.android_3d_model_engine.services;

import android.util.Log;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Faces;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Tuple3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Object3DUnpacker {
    Object3DData object3DData;

    // meta data
    FloatBuffer vertexBuffer;
    Faces faces;
    IntBuffer IndexBuffer;

    List<float[]> vertexArrayList = new ArrayList<float[]>();

    public Object3DUnpacker(Object3DData object3DData) {
        this.object3DData = object3DData;
        this.vertexBuffer = object3DData.getVertexBuffer();
        this.faces = object3DData.getFaces();
        this.IndexBuffer = faces.getIndexBuffer();
    }

    // Unpacking FloatBuffer vertexArrayBuffer to vertexBuffer
    public void unpackingArrayBuffer() {
        FloatBuffer vertexArrayBuffer = object3DData.getVertexArrayBuffer();

        // allocating memory to returnVertexBuffer
        vertexBuffer = createNativeByteBuffer(object3DData.getNumVerts() * 3 * 4).asFloatBuffer();

        // Unpacking vertexArrayBuffer
        for(int i = 0; i < faces.getVerticesReferencesCount(); i++) {
            vertexBuffer.put(IndexBuffer.get(i) * 3, vertexArrayBuffer.get(i * 3));
            vertexBuffer.put(IndexBuffer.get(i) * 3 + 1,vertexArrayBuffer.get(i * 3 + 1));
            vertexBuffer.put(IndexBuffer.get(i) * 3 + 2,vertexArrayBuffer.get(i * 3 + 2));
        }
    }

    public void unpackingBuffer() {
        // setting v contents of obj's file
        for(int i = 0; i < object3DData.getNumVerts(); i++) {
            float[] position = new float[3];

            position[0] = vertexBuffer.get(i * 3);
            position[1] = vertexBuffer.get(i * 3 + 1);
            position[2] = vertexBuffer.get(i * 3 + 2);

            // add element of vertexArrayList
            vertexArrayList.add(i, position);
        }
    }

    // make original state to obj's vertex
    public void ReinstateVertex() {
        float scaleFactor = 1.0f;
        float largest = object3DData.getDimensions().getLargest();

        if (largest != 0.0f)
            scaleFactor = (1.0f / largest);

        Tuple3 center = object3DData.getDimensions().getCenter();

        // modify the model's vertices
        float x0, y0, z0;
        float x, y, z;

        for (int i = 0; i < vertexBuffer.capacity()/3; i++) {
            x0 = vertexBuffer.get(i * 3);
            y0 = vertexBuffer.get(i * 3 + 1);
            z0 = vertexBuffer.get(i * 3 + 2);

            x = (x0 / scaleFactor) + center.getX();
            vertexBuffer.put(i * 3, x);

            y = (y0 / scaleFactor) + center.getY();
            vertexBuffer.put(i * 3 + 1, y);

            z = (z0 / scaleFactor) + center.getZ();
            vertexBuffer.put(i * 3 + 2, z);
        }
    }

    public List<float[]> getVertexArrayList() {
        return vertexArrayList;
    }

    private static ByteBuffer createNativeByteBuffer(int length) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }
}
