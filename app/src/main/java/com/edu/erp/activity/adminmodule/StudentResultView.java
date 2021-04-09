package com.edu.erp.activity.adminmodule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.edu.erp.R;
import com.edu.erp.activity.studentmodule.ExamMarksActivity;
import com.edu.erp.activity.studentmodule.ExamOnlyTotalMarksActivity;
import com.edu.erp.adapter.adminmodule.ClassStudentListAdapter;
import com.edu.erp.bean.admin.viewlist.ClassStudent;
import com.edu.erp.bean.admin.viewlist.ClassStudentList;
import com.edu.erp.bean.student.viewlist.Exams;
import com.edu.erp.helper.AlertDialogHelper;
import com.edu.erp.helper.ProgressDialogHelper;
import com.edu.erp.interfaces.DialogClickListener;
import com.edu.erp.servicehelpers.ServiceHelper;
import com.edu.erp.serviceinterfaces.IServiceListener;
import com.edu.erp.utils.CommonUtils;
import com.edu.erp.utils.EnsyfiConstants;
import com.edu.erp.utils.PreferenceStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Admin on 18-07-2017.
 */

public class StudentResultView extends AppCompatActivity implements IServiceListener, DialogClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = StudentResultView.class.getName();
    private ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;
    private String storeClassId, storeSectionId;
    ListView loadMoreListView;
    ClassStudentListAdapter classStudentListAdapter;
    ArrayList<ClassStudent> classStudentArrayList;
    int pageNumber = 0, totalCount = 0;
    protected boolean isLoadingForFirstTime = true;
    Handler mHandler = new Handler();
    private Exams exams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_result_view);
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
        loadMoreListView = (ListView) findViewById(R.id.listView_events);
        loadMoreListView.setOnItemClickListener(this);
        classStudentArrayList = new ArrayList<>();
        exams = (Exams) getIntent().getSerializableExtra("eventObj");
        Bundle extras = getIntent().getExtras();
        storeClassId = extras.getString("storeClassId");
        storeSectionId = extras.getString("storeSectionId");

        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GetStudentData();
    }

    private void GetStudentData() {
        if (classStudentArrayList != null)
            classStudentArrayList.clear();

        if (CommonUtils.isNetworkAvailable(this)) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.PARAMS_CLASS_ID_LIST, storeClassId);
                jsonObject.put(EnsyfiConstants.PARAMS_SECTION_ID_LIST, storeSectionId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(getApplicationContext()) + EnsyfiConstants.GET_STUDENT_LISTS;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, "No Network connection");
        }
    }

    private boolean validateSignInResponse(JSONObject response) {
        boolean signInsuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(EnsyfiConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInsuccess = false;
                        Log.d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);

                    } else {
                        signInsuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return signInsuccess;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onEvent list item click" + position);
        ClassStudent classStudent = null;
        if ((classStudentListAdapter != null) && (classStudentListAdapter.ismSearching())) {
            Log.d(TAG, "while searching");
            int actualindex = classStudentListAdapter.getActualEventPos(position);
            Log.d(TAG, "actual index" + actualindex);
            classStudent = classStudentArrayList.get(actualindex);
        } else {
            classStudent = classStudentArrayList.get(position);
        }

        PreferenceStorage.saveStudentRegisteredIdPreference(this, classStudent.getEnrollId());

        String isInternalExternal = exams.getIsInternalExternal();
        String isMarkStatus = exams.getMarkStatus();
        int intMarkStatus = Integer.parseInt(isMarkStatus);

        if (intMarkStatus == 1) {

            if (isInternalExternal.equalsIgnoreCase("1")) {
                Intent intent = new Intent(this, ExamMarksActivity.class);
                intent.putExtra("eventObj", exams);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ExamOnlyTotalMarksActivity.class);
                intent.putExtra("eventObj", exams);
                startActivity(intent);
            }
        } else {
            AlertDialogHelper.showSimpleAlertDialog(StudentResultView.this, "Exam result not published yet !");
        }
    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    @Override
    public void onResponse(final JSONObject response) {
        progressDialogHelper.hideProgressDialog();

        if (validateSignInResponse(response)) {

            try {
                JSONArray getData = response.getJSONArray("data");

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialogHelper.hideProgressDialog();
                        Gson gson = new Gson();
                        ClassStudentList classStudentList = gson.fromJson(response.toString(), ClassStudentList.class);
                        if (classStudentList.getClassStudent() != null && classStudentList.getClassStudent().size() > 0) {
                            totalCount = classStudentList.getCount();
                            isLoadingForFirstTime = false;
                            updateListAdapter(classStudentList.getClassStudent());
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "Error while sign In");
        }
    }

    @Override
    public void onError(final String error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialogHelper.hideProgressDialog();
                AlertDialogHelper.showSimpleAlertDialog(StudentResultView.this, error);
            }
        });
    }

    protected void updateListAdapter(ArrayList<ClassStudent> classStudentArrayList) {
        this.classStudentArrayList.addAll(classStudentArrayList);
        if (classStudentListAdapter == null) {
            classStudentListAdapter = new ClassStudentListAdapter(this, this.classStudentArrayList);
            loadMoreListView.setAdapter(classStudentListAdapter);
        } else {
            classStudentListAdapter.notifyDataSetChanged();
        }
    }
}
