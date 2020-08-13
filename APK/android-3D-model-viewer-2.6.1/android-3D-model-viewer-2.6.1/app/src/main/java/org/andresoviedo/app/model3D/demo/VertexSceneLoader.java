package org.andresoviedo.app.model3D.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.andresoviedo.android_3d_model_engine.model.Object3D;
import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.Object3DBuilder;
import org.andresoviedo.android_3d_model_engine.services.Object3DDisorganization;
import org.andresoviedo.app.model3D.view.ModelActivity;
import org.andresoviedo.util.android.ContentUtils;

import java.util.ArrayList;
import java.util.List;

public class VertexSceneLoader extends SceneLoader {
    List<float[]> vertexArrayList;

    public VertexSceneLoader(ModelActivity modelActivity, Object3DData obj) {
        super(modelActivity);
        Object3DDisorganization disorganization = new Object3DDisorganization(obj);
        disorganization.unpackingBuffer();
        disorganization.OutFileToVertexBuffer();
        vertexArrayList = disorganization.getVertexArrayList();
    }

    @SuppressLint("StaticFieldLeak")
    public void init() {
        super.init();
        new AsyncTask<Void, Void, Void>() {

            ProgressDialog dialog = new ProgressDialog(parent);
            List<Exception> errors = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setCancelable(false);
                dialog.setMessage("Loading demo...");
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                    try {
                        for(int i=0;i<vertexArrayList.size();i++) {
                            Object3DData objPoint = Object3DBuilder.buildPoint(new float[]{0.0f, 0.0f, 0.0f});
                            objPoint.setPosition(vertexArrayList.get(i));
                            objPoint.setColor(new float[]{0.0f, 1.0f, 1f, 1.0f});
                            addObject(objPoint);
                        }
                    } catch (Exception ex) {
                        errors.add(ex);
                    } finally {
                    ContentUtils.setThreadActivity(null);
                    ContentUtils.clearDocumentsProvided();
                }
                    return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (!errors.isEmpty()) {
                    StringBuilder msg = new StringBuilder("There was a problem loading the data");
                    for (Exception error : errors) {
                        Log.e("Example", error.getMessage(), error);
                        msg.append("\n" + error.getMessage());
                    }
                    Toast.makeText(parent.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
