package com.edu.erp.activity.general;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.edu.erp.R;
import com.edu.erp.bean.general.viewlist.GroupList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupingDetailActivity extends AppCompatActivity {

    private static final String TAG = "GroupingActivity";

    public TextView txtGroupName, txtnotes, txtDateTime;
    GroupList groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_notification_item);

        groupList = (GroupList) getIntent().getSerializableExtra("eventObj");


        txtGroupName = (TextView) findViewById(R.id.group_title);
        txtnotes = (TextView) findViewById(R.id.mini_notes);
        txtDateTime = (TextView) findViewById(R.id.sent_time_date);

        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String start = groupList.getCreated_at();
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(start);
            SimpleDateFormat sent_date = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String sent_date_name = sent_date.format(date.getTime());
            if (start != null) {
                txtDateTime.setText(sent_date_name);
            } else {
                txtDateTime.setText("N/A");
            }
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        txtGroupName.setText(groupList.getGroup_title());
        txtnotes.setText(groupList.getNotes());
        
        
    }

}