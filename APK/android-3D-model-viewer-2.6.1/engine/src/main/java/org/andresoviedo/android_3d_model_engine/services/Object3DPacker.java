package org.andresoviedo.android_3d_model_engine.services;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Faces;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Object3DPacker {
    Object3DData object3DData;

    // meta data
    FloatBuffer vertexBuffer;
    FloatBuffer vertexArrayBuffer;
    Faces faces;
    IntBuffer IndexBuffer;

    public Object3DPacker(Object3DData selectedObject) {
        this.object3DData = selectedObject;
        this.faces = selectedObject.getFaces();
        this.IndexBuffer = faces.getIndexBuffer();

        // Initialized buffers used to take vertex's position
        this.vertexBuffer = createNativeByteBuffer(selectedObject.getNumVerts() * 3 * 4).asFloatBuffer();
        this.vertexArrayBuffer = createNativeByteBuffer(faces.getVerticesReferencesCount() * 3 * 4).asFloatBuffer();
    }

    public void packingBuffer(float[] position, int offset) {
        for(int i = 0; i < 3; i++) {
            vertexBuffer.put(offset + i, position[i]);
        }
    }

    public void packingArrayBuffer() {
        for(int i = 0; i < faces.getVerticesReferencesCount(); i++) {
            for(int j = 0; j < 3; j++) {
                vertexArrayBuffer.put(i * 3 + j, vertexBuffer.get(IndexBuffer.get(i) * 3 + j));
            }
        }
    }

    public FloatBuffer getVertexArrayBuffer() {
        return vertexArrayBuffer;
    }

    private static ByteBuffer createNativeByteBuffer(int length) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }
}
