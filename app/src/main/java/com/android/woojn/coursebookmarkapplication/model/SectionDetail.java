package com.android.woojn.coursebookmarkapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wjn on 2017-02-08.
 */

public class SectionDetail extends RealmObject {

    @PrimaryKey
    private int id;
    private String title;
    private String desc;
    private String imageUrl;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}