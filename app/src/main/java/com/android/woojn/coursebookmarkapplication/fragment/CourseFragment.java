package com.android.woojn.coursebookmarkapplication.fragment;

import static com.android.woojn.coursebookmarkapplication.Constants.DEFAULT_VIEW_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_FAVORITE;
import static com.android.woojn.coursebookmarkapplication.Constants.FIELD_NAME_ID;
import static com.android.woojn.coursebookmarkapplication.Constants.KEY_COURSE_ID;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility.getNewIdByClass;
import static com.android.woojn.coursebookmarkapplication.util.RealmDbUtility
        .updateTextViewEmptyVisibility;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.woojn.coursebookmarkapplication.R;
import com.android.woojn.coursebookmarkapplication.activity.CourseActivity;
import com.android.woojn.coursebookmarkapplication.adapter.CourseAdapter;
import com.android.woojn.coursebookmarkapplication.model.Course;
import com.android.woojn.coursebookmarkapplication.model.Item;
import com.android.woojn.coursebookmarkapplication.model.Section;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by wjn on 2017-02-16.
 */

public class CourseFragment extends Fragment implements CourseAdapter.OnRecyclerViewClickListener{

    @BindView(R.id.tv_course_empty)
    protected TextView mTextViewCourseEmpty;
    @BindView(R.id.rv_course_list)
    protected RecyclerView mRecyclerViewCourse;

    private Realm mRealm;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course, container, false);
        ButterKnife.bind(this, rootView);

        RealmResults<Course> courses = mRealm.where(Course.class).findAllSorted(FIELD_NAME_ID)
                .sort(FIELD_NAME_FAVORITE, Sort.DESCENDING);
        mRecyclerViewCourse.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerViewCourse.setAdapter(new CourseAdapter(getContext(), courses, this));

        FloatingActionButton fabInsertCourse = (FloatingActionButton) getActivity().findViewById(R.id.fab_insert_course);
        fabInsertCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newCourseId = getNewIdByClass(Course.class);
                showCourseDialog(newCourseId, false, "", "", "");
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }

    @Override
    public void onItemClick(int id, int viewId) {
        Course course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, id).findFirst();
        switch (viewId) {
            case DEFAULT_VIEW_ID:
                Intent updateIntent = new Intent(getContext(), CourseActivity.class);
                updateIntent.putExtra(KEY_COURSE_ID, id);
                startActivity(updateIntent);
                break;
            case R.id.iv_favorite_y_main:
            case R.id.iv_favorite_n_main:
                toggleCourseFavorite(course);
                break;
            case R.id.btn_delete_course:
                deleteCourse(course);
                break;
        }
    }

    @Override
    public void onItemDoubleTap(int id) {
        Course course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, id).findFirst();
        showCourseDialog(course.getId(), true, course.getTitle(), course.getSearchWord(), course.getDesc());
    }

    private void showToastByForce(int resId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void toggleCourseFavorite(final Course course) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (course.isFavorite()) {
                    course.setFavorite(false);
                    showToastByForce(R.string.msg_favorite_n);
                } else {
                    course.setFavorite(true);
                    showToastByForce(R.string.msg_favorite_y);
                }
            }
        });
    }

    private void deleteCourse(final Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.msg_delete_confirm);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(R.string.string_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmList<Section> sections = course.getSections();
                        for (int i = sections.size() - 1; i >= 0; i--) {

                            RealmList<Item> sectionItems = sections.get(i).getItems();
                            for (int j = sectionItems.size() - 1; j >= 0; j--) {
                                sectionItems.get(j).deleteFromRealm();
                            }
                            sections.get(i).deleteFromRealm();
                        }
                        course.deleteFromRealm();
                        updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
                    }
                });
            }
        });
        builder.show();
    }

    private void showCourseDialog(final int courseId, final boolean isCreated, final String beforeTitle, final String beforeSearchWord, final String beforeDesc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);
        final EditText editTextTitle = (EditText) dialogView.findViewById(R.id.et_course_title);
        final EditText editTextSearchWord = (EditText) dialogView.findViewById(R.id.et_course_search_word);
        final EditText editTextDesc = (EditText) dialogView.findViewById(R.id.et_course_desc);

        if (isCreated) {
            editTextTitle.setText(beforeTitle);
            editTextSearchWord.setText(beforeSearchWord);
            editTextDesc.setText(beforeDesc);
        }

        builder.setTitle(R.string.string_course_info);
        builder.setNegativeButton(R.string.string_cancel, null);
        builder.setPositiveButton(isCreated ? R.string.string_update : R.string.string_register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString();
                String searchWord = editTextSearchWord.getText().toString();
                String Desc = editTextDesc.getText().toString();

                Course course;
                mRealm.beginTransaction();
                if (isCreated) {
                    course = mRealm.where(Course.class).equalTo(FIELD_NAME_ID, courseId).findFirst();
                    course.setTitle(title);
                    course.setSearchWord(searchWord);
                    course.setDesc(Desc);
                }
                else {
                    course = mRealm.createObject(Course.class, courseId);
                    course.setTitle(title);
                    course.setSearchWord(searchWord);
                    course.setDesc(Desc);

                    Intent insertIntent = new Intent(getContext(), CourseActivity.class);
                    insertIntent.putExtra(KEY_COURSE_ID, courseId);
                    startActivity(insertIntent);

                    updateTextViewEmptyVisibility(Course.class, 0, mTextViewCourseEmpty);
                }
                mRealm.commitTransaction();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editTextTitle.requestFocus();
        editTextTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextSearchWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editTextDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String afterTitle = editTextTitle.getText().toString();
                String afterSearchWord = editTextSearchWord.getText().toString();
                String afterDesc = editTextDesc.getText().toString();

                if (!afterTitle.trim().isEmpty() && !afterSearchWord.trim().isEmpty()
                        && (!beforeTitle.equals(afterTitle) || !beforeSearchWord.equals(afterSearchWord) || !beforeDesc.equals(afterDesc))) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };
        editTextTitle.addTextChangedListener(textWatcher);
        editTextSearchWord.addTextChangedListener(textWatcher);
        editTextDesc.addTextChangedListener(textWatcher);
    }
}
