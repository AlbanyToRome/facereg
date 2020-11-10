package com.babbangona.bgfr.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.babbangona.bgfr.BuildConfig;
import com.babbangona.bgfr.R;
import com.babbangona.bgfr.databinding.ActivityInstructionsBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstructionsActivity extends AppCompatActivity {

    /**
     * Note: Butterknife replaced with data binding
     */
    ActivityInstructionsBinding binding;
    Toolbar toolbar;
    TextView toolbar_title;
    TextView toolbar_version;
    Button button;


    private void setBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instructions);
        toolbar =  binding.mainToolbarr.toolbarMain;
        toolbar_title = binding.mainToolbarr.toolbarTitle;
        toolbar_version =  binding.mainToolbarr.toolbarVersion;
        button =  binding.continueButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        setToolBar("BABBANGONA");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstructionsActivity.this, FaceRecognitionActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setToolBar(String title){
        toolbar_title.setText(title);
        toolbar.setTitle("");
        toolbar_version.setText("v"+ BuildConfig.VERSION_NAME);
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}