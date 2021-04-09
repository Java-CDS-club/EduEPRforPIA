package com.edu.erp.activity.adminmodule;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.edu.erp.R;
import com.edu.erp.adapter.adminmodule.ClassAttendanceListAdapter;
import com.edu.erp.bean.admin.viewlist.AttendanceClass;
import com.edu.erp.bean.admin.viewlist.AttendanceClassList;
import com.edu.erp.helper.AlertDialogHelper;
import com.edu.erp.helper.ProgressDialogHelper;
import com.edu.erp.interfaces.DialogClickListener;
import com.edu.erp.servicehelpers.ServiceHelper;
import com.edu.erp.serviceinterfaces.IServiceListener;
import com.edu.erp.utils.CommonUtils;
import com.edu.erp.utils.EnsyfiConstants;
import com.edu.erp.utils.PreferenceStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClassAttendanceListActivity extends AppCompatActivity implements DialogClickListener, IServiceListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = ClassAttendanceListActivity.class.getName();
    protected ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;
    ArrayList<String> selectedclassesList = new ArrayList<>();
    String sendDate;
    ListView loadMoreListView;
    ClassAttendanceListAdapter classListAdapter;
    ArrayList<AttendanceClass> classArrayList = new ArrayList<>();;


    @Override
    protected void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_attendance_classes);
        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClassAttendanceActivity.class);
                startActivity(intent);
                finish();
            }
        });
        sendDate = getIntent().getStringExtra("date");
        selectedclassesList = getIntent().getStringArrayListExtra("class");
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
        loadMoreListView = findViewById(R.id.classes);
        loadMoreListView.setOnItemClickListener(this);
        sendSelectClass();
    }

    private void sendSelectClass() {
        if (CommonUtils.isNetworkAvailable(this)) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.PARAMS_DATE, sendDate);
                jsonObject.put(EnsyfiConstants.PARAMS_CLASS_IDS, selectedclassesList.toString().replace("[", "").replace("]", ""));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(getApplicationContext()) + EnsyfiConstants.SEND_CLASS_SECTION;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, "No Network connection");
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

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

    protected void updateListStudentAdapter(ArrayList<AttendanceClass> classAttendanceArrayList) {
        this.classArrayList.addAll(classAttendanceArrayList);
//        if (classStudentListAdapter == null) {
        classListAdapter = new ClassAttendanceListAdapter(this, this.classArrayList);
        loadMoreListView.setAdapter(classListAdapter);
//        } else {
        classListAdapter.notifyDataSetChanged();
//        }
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateSignInResponse(response)) {
            Gson gson = new Gson();
            AttendanceClassList classList = gson.fromJson(response.toString(), AttendanceClassList.class);
            if (classList.getClassDetails() != null && classList.getClassDetails().size() > 0) {
//                    totalCount = classStudentList.getCount();
//                    isLoadingForFirstTime = false;
                updateListStudentAdapter(classList.getClassDetails());
            }
        }
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onEvent list item click" + i);
        AttendanceClass classStudent = null;
        if ((classListAdapter != null) && (classListAdapter.ismSearching())) {
            Log.d(TAG, "while searching");
            int actualindex = classListAdapter.getActualEventPos(i);
            Log.d(TAG, "actual index" + actualindex);
            classStudent = classArrayList.get(actualindex);
        } else {
            classStudent = classArrayList.get(i);
        }
        Intent intent = new Intent(this, ClassAttendanceDetailActivity.class);
        intent.putExtra("date", sendDate);
        intent.putExtra("class", classStudent.getClassId());
        startActivity(intent);
    }
}
