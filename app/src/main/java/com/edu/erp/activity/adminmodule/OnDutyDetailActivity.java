package com.edu.erp.activity.adminmodule;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.edu.erp.R;
import com.edu.erp.bean.general.viewlist.OnDuty;
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

public class OnDutyDetailActivity extends AppCompatActivity implements IServiceListener, DialogClickListener {

    private OnDuty onDuty;
    private TextView txtOnDutyReason, txtOnDutyStartDate, txtOnDutyEndDate, txtOnDutyNotes;
    private Button btnSend;
    private RadioGroup approve;
    protected ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;
    String onDutyApprovalStatus = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_duty_details);
        onDuty = (OnDuty) getIntent().getSerializableExtra("onDutyObj");
        initializeEventHelpers();
        initializeViews();
        populateData();
        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);

        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), OnDutyViewActivity.class);
                startActivity(intent);}
        });
    }

    protected void initializeEventHelpers() {
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
    }

    private void initializeViews() {
        txtOnDutyReason = (TextView) findViewById(R.id.txtOdFor);
        txtOnDutyNotes = (TextView) findViewById(R.id.txtOdNotes);
        txtOnDutyStartDate = (TextView) findViewById(R.id.txtFromDate);
        txtOnDutyEndDate = (TextView) findViewById(R.id.txtToDate);
        approve = findViewById(R.id.status_layout);
        approve.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                switch (checkedId) {
                    case R.id.txtStatusDecline:
                        onDutyApprovalStatus = "Rejected";
                        break;

                    case R.id.txtStatusApprove:
                        onDutyApprovalStatus = "Approved";
                        break;
                }
            }
        });
        btnSend = (Button) findViewById(R.id.send_status);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOdStatus();
                finish();
                Intent intent = new Intent(getApplicationContext(), OnDutyViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    private void populateData() {
        txtOnDutyReason.setText(onDuty.getOdFor());
        txtOnDutyNotes.setText(onDuty.getNotes());
        txtOnDutyStartDate.setText(onDuty.getFromDate());
        txtOnDutyEndDate.setText(onDuty.getToDate());
    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    private void sendOdStatus() {
        if (CommonUtils.isNetworkAvailable(this)) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(EnsyfiConstants.PARAMS_OD_APPROVAL_STATUS, onDutyApprovalStatus);
                jsonObject.put(EnsyfiConstants.PARAMS_OD_ID, onDuty.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = EnsyfiConstants.BASE_URL + PreferenceStorage.getInstituteCode(this) + EnsyfiConstants.APPROVE_OD_API;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);

        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, "No Network connection");
        }
    }

    @Override
    public void onResponse(JSONObject response) {

    }

    @Override
    public void onError(String error) {

    }
}
