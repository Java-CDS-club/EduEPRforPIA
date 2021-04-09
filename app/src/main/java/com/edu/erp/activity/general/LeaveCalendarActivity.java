package com.edu.erp.activity.general;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.edu.erp.R;
import com.edu.erp.adapter.general.HolidayFragmentAdapter;
import com.edu.erp.helper.ProgressDialogHelper;
import com.edu.erp.interfaces.DialogClickListener;
import com.edu.erp.servicehelpers.ServiceHelper;
import com.edu.erp.serviceinterfaces.IServiceListener;
import org.json.JSONObject;

public class LeaveCalendarActivity extends AppCompatActivity implements IServiceListener, DialogClickListener {

    ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_calendar_tabs);

        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);

        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("Upcoming Leaves"));
        tabLayout.addTab(tabLayout.newTab().setText("All Leaves"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        final HolidayFragmentAdapter adapter = new HolidayFragmentAdapter
                (getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
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

    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
    }
}
