package com.android.woojn.coursebookmarkapplication.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wjn on 2017-02-08.
 */

public class Section extends RealmObject {

    @PrimaryKey
    private int id;
    private String title;
    private RealmList<SectionDetail> sectionDetails;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<SectionDetail> getSectionDetails() {
        return sectionDetails;
    }

    public void setSectionDetails(RealmList<SectionDetail> sectionDetails) {
        this.sectionDetails = sectionDetails;
    }
}