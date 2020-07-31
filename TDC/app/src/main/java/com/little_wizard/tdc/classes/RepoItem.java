package com.little_wizard.tdc.classes;

import android.net.Uri;

import java.io.Serializable;

public class RepoItem implements Serializable {
    public String name;
    public Uri jpgPath;

    public RepoItem(String name, Uri jpgPath) {
        this.name = name;
        this.jpgPath = jpgPath;
    }
}
