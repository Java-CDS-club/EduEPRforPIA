package com.edu.erp.activity.adminmodule;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.edu.erp.R;

public class CircularMasterListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_master_list);

        ImageView addCircular = (ImageView) findViewById(R.id.add_circular);

        ImageView bckbtn = (ImageView) findViewById(R.id.back_res);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addCircular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent navigationIntent = new Intent(getApplicationContext(), CircularMasterCreationActivity.class);
                navigationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(navigationIntent);
            }
        });
    }
}
