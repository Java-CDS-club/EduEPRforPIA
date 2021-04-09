package com.edu.erp.activity.teachermodule;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.edu.erp.R;
import com.edu.erp.adapter.teachermodule.SubstitutionClassListAdapter;
import com.edu.erp.bean.teacher.viewlist.SubstitutionClass;
import com.edu.erp.bean.teacher.viewlist.SubstitutionClassList;
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

public class SubstitutionActivity extends AppCompatActivity implements IServiceListener, AdapterView.OnItemClickListener, DialogClickListener {

    private static final String TAG = "SubstitutionActivity";
    ListView loadMoreListView;
    View view;
    SubstitutionClassListAdapter circularListAdapter;
    ServiceHelper serviceHelper;
    ArrayList<SubstitutionClass> circularArrayList;
    int totalCount = 0;
    protected ProgressDialogHelper progressDialogHelper;
    protected boolean isLoadingForFirstTime = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substitution);
        loadMoreListView = (ListView) findViewById(R.id.list_classes);
        loadMoreListView.setOnItemClickListener(this);
        circularArrayList = new ArrayList<>();
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);

        callGetCircularService();
        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void callGetCircularService() {

        if (circularArrayList != null) {
            circularArrayList.clear();
        }

        if (CommonUtils.isNetworkAvailable(this)) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.KEY_USER_ID, PreferenceStorage.getUserId(getApplicationContext()));


            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = "";
            url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(getApplicationContext()) + EnsyfiConstants.GET_SUBSTITUTION_CLASS;
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
            Log.d("ajazFilterresponse : ", response.toString());
            Gson gson = new Gson();
            SubstitutionClassList circularList = gson.fromJson(response.toString(), SubstitutionClassList.class);
            if (circularList.getClassTestMark() != null && circularList.getClassTestMark().size() > 0) {
                totalCount = circularList.getCount();
                isLoadingForFirstTime = false;
                updateListAdapter(circularList.getClassTestMark());
            }
        } else {
            Log.d(TAG, "Error while sign In");
        }
    }

    protected void updateListAdapter(ArrayList<SubstitutionClass> circularArrayList) {
        this.circularArrayList.addAll(circularArrayList);
        if (circularListAdapter == null) {
            circularListAdapter = new SubstitutionClassListAdapter(this, this.circularArrayList);
            loadMoreListView.setAdapter(circularListAdapter);
        } else {
            circularListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(final String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(SubstitutionActivity.this, error);
    }
}