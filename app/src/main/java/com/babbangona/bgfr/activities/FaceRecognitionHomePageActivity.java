package com.babbangona.bgfr.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.babbangona.bgfr.BuildConfig;
import com.babbangona.bgfr.R;
import com.babbangona.bgfr.databinding.ActivityFaceRecognitionHomePageBinding;


public class FaceRecognitionHomePageActivity extends AppCompatActivity {

    /**
     *
     * Note To thomas: Replaced ButterKnife with Android Native Data Binding
     */
    ActivityFaceRecognitionHomePageBinding binding;
    private static final int menuAbour = R.id.menuAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_face_recognition_home_page);

        setToolBar("BABBANGONA");


       binding.moveToInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(FaceRecognitionHomePageActivity.this, com.babbangona.bgfr.activities.InstructionsActivity.class);
                startActivity(intent);
            }
        });

    }


    private void setToolBar(String title){
        binding.mainToolbarr.toolbarTitle.setText(title);
        binding.mainToolbarr.toolbarMain.setTitle("");
        binding.mainToolbarr.toolbarVersion.setText("v"+ BuildConfig.VERSION_NAME);
        setSupportActionBar(binding.mainToolbarr.toolbarMain);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home ){
            Toast.makeText(this,"Home Pressed",Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId() == R.id.menuAbout){
            Toast.makeText(this,"Sync Pressed",Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 23);
            }
        }
    }
}