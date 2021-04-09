package com.edu.erp.activity.adminmodule;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.edu.erp.R;
import com.edu.erp.adapter.studentmodule.DayViewListAdapter;
import com.edu.erp.bean.student.viewlist.DayView;
import com.edu.erp.bean.student.viewlist.DayViewList;
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

public class ClassAttendanceDetailActivity extends AppCompatActivity implements IServiceListener, DialogClickListener {

    private static final String TAG = ClassAttendanceDetailActivity.class.getName();
    protected ProgressDialogHelper progressDialogHelper;
    private String checkDayMonthType = "day";
    ServiceHelper serviceHelper;
    ArrayList<DayView> dayViewArrayList;
    String sendDate, selectedclassesList;
    DayViewListAdapter dayViewListAdapter;
    ListView loadMoreListView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_classes_detail);

        sendDate = getIntent().getStringExtra("date");
        selectedclassesList = getIntent().getStringExtra("class");
        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
        dayViewArrayList = new ArrayList<>();
        loadMoreListView = (ListView) findViewById(R.id.listView_events);
        callOnDayAttendanceViewService();
    }


    private void callOnDayAttendanceViewService() {

        if (dayViewArrayList != null)
            dayViewArrayList.clear();

        if (CommonUtils.isNetworkAvailable(this)) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.PARAM_CLASS_ID, selectedclassesList);
                jsonObject.put(EnsyfiConstants.PARAMS_DISPLAY_TYPE, checkDayMonthType);
                jsonObject.put(EnsyfiConstants.PARAMS_DISPLAY_DATE, sendDate);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(getApplicationContext()) + EnsyfiConstants.GET_STUDENT_ATTENDANCE_API;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, "No Network connection");
        }
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

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        try {
            if (validateSignInResponse(response)) {
                Log.d("ajazFilterresponse : ", response.toString());
                if (checkDayMonthType.equalsIgnoreCase("day")) {

                    JSONArray getData = response.getJSONArray("attendenceDetails");
                    if (getData != null && getData.length() > 0) {

                        Gson gson = new Gson();
                        DayViewList dayViewList = gson.fromJson(response.toString(), DayViewList.class);
                        if (dayViewList.getDayView() != null && dayViewList.getDayView().size() > 0) {
                            updateDayViewListAdapter(dayViewList.getDayView());
                        }

                    } else {
                        if (dayViewArrayList != null) {
                            dayViewArrayList.clear();
                            dayViewListAdapter = new DayViewListAdapter(this, this.dayViewArrayList);
                            loadMoreListView.setAdapter(dayViewListAdapter);
                        }
                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void updateDayViewListAdapter(ArrayList<DayView> dayViewArrayList) {
        this.dayViewArrayList.addAll(dayViewArrayList);
//        if (dayViewListAdapter == null) {
        dayViewListAdapter = new DayViewListAdapter(ClassAttendanceDetailActivity.this, this.dayViewArrayList);
        loadMoreListView.setAdapter(dayViewListAdapter);
//        } else {
        dayViewListAdapter.notifyDataSetChanged();
//        }
    }

    @Override
    public void onError(String error) {

    }
}
