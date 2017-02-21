package com.android.woojn.coursebookmarkapplication.adapter;

import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_REQUEST_WEB_ACTIVITY;
import static com.android.woojn.coursebookmarkapplication.Constants.REQUEST_WEB_ACTIVITY_WITHOUT_SAVE;

import static com.android.woojn.coursebookmarkapplication.Constants.KEY_STRING_URL;
import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_VIEW_ID;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.setTextViewEmptyVisibility;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.WebActivity;
import com.android.woojn.coursebookmarkapplication.model.Section;
import com.android.woojn.coursebookmarkapplication.model.SectionDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;

/**
 * Created by wjn on 2017-02-07.
 */

public class CourseSectionAdapter extends RealmRecyclerViewAdapter<Section, CourseSectionAdapter.CourseSectionViewHolder> {

    private final Context mContext;
    private final OnRecyclerViewClickListener mListener;

    public CourseSectionAdapter(Context context, OrderedRealmCollection<Section> data, OnRecyclerViewClickListener listener) {
        super(context, data, true);
        mListener = listener;
        mContext = context;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int id, View view);
    }

    @Override
    public CourseSectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.course_section_list_item, parent, false);
        return new CourseSectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseSectionViewHolder holder, int position) {
        if (getData() != null) {
            Section section = getData().get(position);
            holder.itemView.setTag(section.getId());
            holder.textViewSectionTitle.setText(section.getTitle());
            holder.textViewSectionSearchWord.setText("(" + section.getSearchWord() + ")");
            CourseSectionDetailAdapter adapter = new CourseSectionDetailAdapter(mContext, section.getSectionDetails()
                    .sort(FIELD_NAME_ID, Sort.DESCENDING), holder);
            holder.recyclerViewCourseSectionDetail.setAdapter(adapter);
            setTextViewEmptyVisibility(SectionDetail.class, section.getId(), holder.textViewCourseSectionDetailEmpty);
        }
    }

    class CourseSectionViewHolder extends RecyclerView.ViewHolder
        implements CourseSectionDetailAdapter.OnRecyclerViewClickListener {

        @BindView(R.id.tv_section_title)
        TextView textViewSectionTitle;
        @BindView(R.id.tv_section_search_word)
        TextView textViewSectionSearchWord;
        @BindView(R.id.tv_course_section_detail_empty)
        TextView textViewCourseSectionDetailEmpty;
        @BindView(R.id.rv_course_section_detail_list)
        RecyclerView recyclerViewCourseSectionDetail;

        public CourseSectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            recyclerViewCourseSectionDetail.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        @OnClick({R.id.btn_search_section_detail, R.id.btn_delete_section, R.id.btn_section_overflow})
        public void onClick(View view) {
            mListener.onItemClick((int) itemView.getTag(), view);
        }

        @Override
        public void onItemClick(int id, int viewId) {
            switch (viewId) {
                case DEFAULT_VIEW_ID:
                    Realm realm = Realm.getDefaultInstance();
                    SectionDetail sectionDetail = realm.where(SectionDetail.class).equalTo(
                            FIELD_NAME_ID, id).findFirst();
                    String stringUrl = sectionDetail.getUrl();
                    realm.close();

                    Intent webIntent = new Intent(mContext, WebActivity.class);
                    webIntent.putExtra(KEY_REQUEST_WEB_ACTIVITY, REQUEST_WEB_ACTIVITY_WITHOUT_SAVE);
                    webIntent.putExtra(KEY_STRING_URL, stringUrl);
                    mContext.startActivity(webIntent);
                    break;
                case R.id.btn_share_section_detail:
                    // TODO: 섹션 항목 공유
                    break;
                case R.id.btn_delete_section_detail:
                    deleteSectionDetail(id);
                    break;
            }
        }

        private void deleteSectionDetail(int sectionDetailId) {
            Realm realm = Realm.getDefaultInstance();
            SectionDetail sectionDetail = realm.where(SectionDetail.class).equalTo(FIELD_NAME_ID, sectionDetailId).findFirst();
            realm.beginTransaction();
            sectionDetail.deleteFromRealm();
            realm.commitTransaction();
            realm.close();
            setTextViewEmptyVisibility(SectionDetail.class, (int) itemView.getTag(), textViewCourseSectionDetailEmpty);
        }
    }
}