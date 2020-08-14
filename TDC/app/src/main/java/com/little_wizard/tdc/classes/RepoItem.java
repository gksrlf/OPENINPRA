package com.little_wizard.tdc.classes;

public class RepoItem {
    private String name;
    private String md5;

    public RepoItem(String name, String md5) {
        this.name = name;
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public String getMd5() {
        return md5;
    }
}