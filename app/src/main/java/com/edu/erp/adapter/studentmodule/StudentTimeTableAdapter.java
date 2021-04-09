package com.edu.erp.adapter.studentmodule;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.edu.erp.bean.teacher.viewlist.TTDays;
import com.edu.erp.fragments.DynamicTTFragment;

import java.util.ArrayList;

public class StudentTimeTableAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    ArrayList<TTDays> ttDays;
    public StudentTimeTableAdapter(FragmentManager fm, int NumOfTabs, ArrayList<TTDays> ttDaysArrayList) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.ttDays = ttDaysArrayList;
    }
    @Override
    public Fragment getItem(int position) {
        return DynamicTTFragment.newInstance(position, ttDays);
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}