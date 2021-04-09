package com.edu.erp.activity.teachermodule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.edu.erp.R;
import com.edu.erp.adapter.teachermodule.ClassTestMarkListAdapter;
import com.edu.erp.bean.teacher.viewlist.ClassTestMark;
import com.edu.erp.bean.teacher.viewlist.ClassTestMarkList;
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

/**
 * Created by Admin on 14-07-2017.
 */

public class ViewClassTestMarkActivity extends AppCompatActivity implements IServiceListener, DialogClickListener {

    long hwId;
    String homeWorkId;
    private static final String TAG = ViewClassTestMarkActivity.class.getName();
    private ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;
    ListView loadMoreListView;
    ClassTestMarkListAdapter classTestMarkListAdapter;
    ArrayList<ClassTestMark> classTestMarkArrayList;
    int totalCount = 0;
    protected boolean isLoadingForFirstTime = true;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_class_test_mark);

        hwId = getIntent().getExtras().getLong("hw_id");
        homeWorkId = String.valueOf(hwId);
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
        loadMoreListView = (ListView) findViewById(R.id.listView_events);

        classTestMarkArrayList = new ArrayList<>();

        GetClassTestMarkData();
        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.edit_mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddClassTestMarkActivity.class);
                intent.putExtra("hw_id", hwId);
                intent.putExtra("mark_array", "edit");
                startActivity(intent);
                finish();
            }
        });
    }

    private void GetClassTestMarkData() {


        if (classTestMarkArrayList != null)
            classTestMarkArrayList.clear();

        if (CommonUtils.isNetworkAvailable(this)) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.PARAM_HOMEWORK_ID, homeWorkId);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(getApplicationContext()) + EnsyfiConstants.GET_CLASS_TEST_MARK;
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
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    @Override
    public void onResponse(final JSONObject response) {
        progressDialogHelper.hideProgressDialog();

        if (validateSignInResponse(response)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialogHelper.hideProgressDialog();

                    Gson gson = new Gson();
                    ClassTestMarkList classTestMarkList = gson.fromJson(response.toString(), ClassTestMarkList.class);
                    if (classTestMarkList.getClassTestMark() != null && classTestMarkList.getClassTestMark().size() > 0) {
                        totalCount = classTestMarkList.getCount();
                        isLoadingForFirstTime = false;
                        updateListAdapter(classTestMarkList.getClassTestMark());
                    }
                }
            });
        }
    }

    @Override
    public void onError(final String error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialogHelper.hideProgressDialog();
                AlertDialogHelper.showSimpleAlertDialog(getApplicationContext(), error);
            }
        });
    }

    protected void updateListAdapter(ArrayList<ClassTestMark> classTestMarkArrayList) {
        this.classTestMarkArrayList.addAll(classTestMarkArrayList);
        if (classTestMarkListAdapter == null) {
            classTestMarkListAdapter = new ClassTestMarkListAdapter(this, this.classTestMarkArrayList);
            loadMoreListView.setAdapter(classTestMarkListAdapter);
        } else {
            classTestMarkListAdapter.notifyDataSetChanged();
        }
    }
}
