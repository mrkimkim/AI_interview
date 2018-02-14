package com.mrkimkim.project.testviewpager;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void mOnPopupClick(View v) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
            return false; //I have tried here true also
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode==RESULT_OK) {
                String result = data.getStringExtra("result");
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
