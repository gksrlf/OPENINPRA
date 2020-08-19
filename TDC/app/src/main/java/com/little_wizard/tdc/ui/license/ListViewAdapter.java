package com.little_wizard.tdc.ui.license;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.little_wizard.tdc.R;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItems = new ArrayList<>();
    TextView t1, t2, t3, t4, t5;

    public ListViewAdapter(){

    }

    @Override
    public int getCount(){
        return listViewItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.license_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        t1 = convertView.findViewById(R.id.license_name);
        t2 = convertView.findViewById(R.id.license_link);
        t3 = convertView.findViewById(R.id.license_holder);
        t4 = convertView.findViewById(R.id.license_type);
        t5 = convertView.findViewById(R.id.license_content);


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItems.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        t1.setText(listViewItem.getName());
        t2.setText(listViewItem.getUrl());
        t3.setText(listViewItem.getHolder());
        t4.setText(listViewItem.getLicenseName());
        t5.setText(listViewItem.getContents());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItems.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String url, String holder, String licenseType) {
        ListViewItem item = new ListViewItem();

        item.setName(name);
        item.setUrl(url);
        item.setHolder(holder);
        item.setLicenseName(licenseType);
        listViewItems.add(item);
    }

    public void addItem(String name, String contents) {
        ListViewItem item = new ListViewItem();

        item.setName(name);
        item.setContents(contents);
        listViewItems.add(item);
    }
}
