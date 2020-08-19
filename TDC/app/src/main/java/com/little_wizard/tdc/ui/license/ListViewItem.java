package com.little_wizard.tdc.ui.license;

public class ListViewItem {
    private String name;
    private String url;
    private String holder;
    private String licenseName;
    private String contents;

    public void setName(String name){
        this.name = name;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public void setHolder(String holder){
        this.holder = holder;
    }
    public void setLicenseName(String licenseName){
        this.licenseName = licenseName;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public String getName(){
        return this.name;
    }
    public String getUrl(){
        return this.url;
    }
    public String getHolder(){
        return this.holder;
    }
    public String getLicenseName(){
        return this.licenseName;
    }
    public String getContents(){return this.contents;}

}
