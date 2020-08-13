package org.andresoviedo.android_3d_model_engine.services.wavefront;

import android.util.Log;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.Object3DDisorganization;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Faces;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Tuple3;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Materials;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *  This class is saving obj file use Object3DData's metadata
 */

public class WavefrontSaver {
    Object3DData object3DData;

    // meta data
    FloatBuffer textureCoordsBuffer;
    Faces faces;

    Object3DDisorganization object3DDisorganization;

    Materials materials = null;

    public WavefrontSaver(Object3DData object3DData) {
        this.object3DDisorganization = new Object3DDisorganization(object3DData);
        this.object3DData = object3DData;
        this.textureCoordsBuffer = null;
        this.faces = object3DData.getFaces();
    }

    // Unpacking FloatBuffer vertexArrayBuffer to vertexBuffer
    public void unpackingBuffer() {
        ArrayList<Tuple3> texCoords = object3DData.getTexCoords();
        FloatBuffer textureCoordsArrayBuffer = object3DData.getTextureCoordsArrayBuffer();

        // allocating memory to returnVertexBuffer
        if(texCoords != null)
            textureCoordsBuffer = createNativeByteBuffer(texCoords.size() * 2 * 4).asFloatBuffer();

        // Unpacking textureCoordsArrayBuffer
        int count = 0;
        for(int i = 0; i < faces.facesTexIdxs.size(); i++) {
            int[] text = faces.facesTexIdxs.get(i);
            for(int j = 0; j < text.length; j++) {
                textureCoordsBuffer.put(text[j] * 2, textureCoordsArrayBuffer.get(count++));
                textureCoordsBuffer.put(text[j] * 2 + 1, textureCoordsArrayBuffer.get(count++));
            }
        }

        object3DDisorganization.unpackingBuffer();
        object3DDisorganization.ReinstateVertex();
    }

    public void OutFileToVertexBuffer() {
        float u = 0, v = 0;

        int[] VT = new int[3];

        ArrayList<String> facesString = new ArrayList<String>();

        // get .mtl file's name
        materials = object3DData.getMaterials();

        // unpacking ArrayBuffers to Buffers and reinstating vertex
        unpackingBuffer();

        object3DDisorganization.OutFileToVertexBuffer();
        List<float[]> vertexArrayList = object3DDisorganization.getVertexArrayList();

        // setting v contents of obj's file
        for(int i = 0; i < vertexArrayList.size(); i++) {
            float[] position = vertexArrayList.get(i);

            //TODO Need to change vertex's data for sync obj file
            Log.d("vertex","v " + String.valueOf(position[0]) + " " + String.valueOf(position[1]) + " " + String.valueOf(position[2]));
        }

        // setting vt contents of obj's file
        for(int i = 0; i < object3DData.getNumTextures(); i++) {
            u = textureCoordsBuffer.get(i * 2);
            v = (object3DData.isFlipTextCoords() ? 1 - textureCoordsBuffer.get(i * 2 + 1) : textureCoordsBuffer.get(i * 2 + 1));

            //TODO Need to change texture vertex's data for sync obj file
            Log.d("texture vertex","vt " + String.valueOf(u) + " " + String.valueOf(v));
        }

        // setting f contents of obj's file
        for(int i=0;i<faces.facesTexIdxs.size();i++) {
            String[] face = new String[3];
            int[] text = faces.facesTexIdxs.get(i);

            for(int j = 0; j < 3; j++)
                face[j] =String.valueOf(faces.facesVertIdxs.get(i * 3 + j) + 1) + "/" + String.valueOf(text[j] + 1);

            //TODO Need to change faces's data for sync obj file
            Log.d("face","f "+face[0]+" "+face[1]+" "+face[2]);
        }
    }

    private static ByteBuffer createNativeByteBuffer(int length) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }
}
