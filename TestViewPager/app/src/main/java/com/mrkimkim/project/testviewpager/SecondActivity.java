package com.mrkimkim.project.testviewpager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import im.dacer.androidcharts.LineView;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        int screenHeight = (int) (metrics.heightPixels * 0.90);
        setContentView(R.layout.activity_second);
        getWindow().setLayout(screenWidth, screenHeight);

        FlexboxLayout flexbox = (FlexboxLayout)findViewById(R.id.flexbox);

        ChipCloudConfig config = new ChipCloudConfig()
                .selectMode(ChipCloud.SelectMode.multi)
                .checkedChipColor(Color.parseColor("#50b7ff"))
                .checkedTextColor(Color.parseColor("#ffffff"))
                .uncheckedChipColor(Color.parseColor("#50b7ff"))
                .uncheckedTextColor(Color.parseColor("#ffffff"))
                .useInsetPadding(true);



        ChipCloud chipCloud = new ChipCloud(this, flexbox, config);
        chipCloud.addChip("김김이", ContextCompat.getDrawable(this, R.drawable.icon_1st));
        chipCloud.addChip("김성희", ContextCompat.getDrawable(this, R.drawable.icon_2nd));
        chipCloud.addChip("호호호", ContextCompat.getDrawable(this, R.drawable.icon_3rd));
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");
        chipCloud.addChip("호호호");



        // 차트 만들기

        ArrayList<String> strList = new ArrayList<String>();
        strList.add("00:10");
        strList.add("00:20");
        strList.add("00:30");
        strList.add("00:40");
        strList.add("00:50");
        strList.add("00:60");

        ArrayList<ArrayList<Float>> dataLists = new ArrayList<ArrayList<Float>>();
        dataLists.add(new ArrayList<Float>());
        dataLists.add(new ArrayList<Float>());
        dataLists.add(new ArrayList<Float>());

        dataLists.get(0).add(0.9f);
        dataLists.get(0).add(0.9f);
        dataLists.get(0).add(1.0f);
        dataLists.get(0).add(1.0f);
        dataLists.get(0).add(1.0f);
        dataLists.get(0).add(1.0f);

        dataLists.get(1).add(1.0f);
        dataLists.get(1).add(2.0f);
        dataLists.get(1).add(3.0f);
        dataLists.get(1).add(4.0f);
        dataLists.get(1).add(5.0f);
        dataLists.get(1).add(5.0f);

        dataLists.get(2).add(1.0f);
        dataLists.get(2).add(1.5f);
        dataLists.get(2).add(1.1f);
        dataLists.get(2).add(1.6f);
        dataLists.get(2).add(1.7f);
        dataLists.get(2).add(1.9f);

        LineView lineView = (LineView) findViewById(R.id.line_view);
        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional
        lineView.setBottomTextList(strList);
        lineView.setColorArray(new int[]{Color.BLACK,Color.GREEN,Color.GRAY});
        lineView.setFloatDataList(dataLists); //or lineView.setFloatDataList(floatDataLists)
        lineView.setDrawDotLine(true);
        lineView.setPivotY(2);

    }

    public ArrayList<ArrayList<Float>> dataNormalizer(ArrayList<ArrayList<Float>> data) {
        ArrayList<ArrayList<Float>> ret = new ArrayList<ArrayList<Float>>();

        // 최댓값을 찾는다.


        for(int i = 0; i < data.size(); ++i) {
            ret.add(new ArrayList<Float>());
        }


        return ret;
    }

    public void mOnClose(View v) {
        Intent intent = new Intent();
        intent.putExtra("result", "Close PopUp");
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
