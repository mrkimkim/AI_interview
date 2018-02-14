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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import im.dacer.androidcharts.LineView;

public class SecondActivity extends Activity {

    FlexboxLayout flexbox;
    LineView chart_emotion;
    LineChart chart_wps;
    LineChart chart_pitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 풀 스크린 조정
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        int screenHeight = (int) (metrics.heightPixels * 0.90);
        setContentView(R.layout.activity_second);
        getWindow().setLayout(screenWidth, screenHeight);

        // 태그 설정
        flexbox = (FlexboxLayout)findViewById(R.id.flexbox);

        ChipCloudConfig config = new ChipCloudConfig()
                .selectMode(ChipCloud.SelectMode.multi)
                .checkedChipColor(Color.parseColor("#50b7ff"))
                .checkedTextColor(Color.parseColor("#ffffff"))
                .uncheckedChipColor(Color.parseColor("#50b7ff"))
                .uncheckedTextColor(Color.parseColor("#ffffff"))
                .useInsetPadding(true);

        ChipCloud chipCloud = new ChipCloud(this, flexbox, config);
        chipCloud.addChip("공무원", ContextCompat.getDrawable(this, R.drawable.icon_1st));
        chipCloud.addChip("철밥통", ContextCompat.getDrawable(this, R.drawable.icon_2nd));
        chipCloud.addChip("최고야", ContextCompat.getDrawable(this, R.drawable.icon_3rd));
        chipCloud.addChip("국가관");
        chipCloud.addChip("대민봉사");
        chipCloud.addChip("혁신");
        chipCloud.addChip("사랑");
        chipCloud.addChip("시민사회");
        chipCloud.addChip("복지");
        chipCloud.addChip("국민");
        chipCloud.addChip("최고");
        chipCloud.addChip("노오력");


        // 감정 차트 (꺾은 선)

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

        chart_emotion = (LineView) findViewById(R.id.line_view);
        chart_emotion.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional
        chart_emotion.setBottomTextList(strList);
        chart_emotion.setColorArray(new int[]{Color.BLACK,Color.GREEN,Color.GRAY});
        chart_emotion.setFloatDataList(dataLists); //or chart_emotion.setFloatDataList(floatDataLists)
        chart_emotion.setDrawDotLine(true);
        chart_emotion.setPivotY(2);

        // 초당 단어 속도 차트
        getWPSchart();

        // 음성 피치 차트
        getPitchchart();
    }

    public LineData getLineData(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "WPS");
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setCubicIntensity(0.1f);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1.0f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setHighLightColor(Color.rgb(244, 117, 117));
        dataSet.setColor(Color.BLACK);
        dataSet.setFillColor(Color.GREEN);
        dataSet.setFillAlpha(100);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return -10;
            }
        });

        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(true);

        return lineData;
    }

    public void getPitchchart() {
        // 차트 리소스를 찾음
        chart_pitch = (LineChart)findViewById(R.id.chart_pitch);

        // 데이터를 설정함
        List<Entry>entries = new ArrayList<Entry>();
        for(int i = 0; i < 5; ++i) entries.add(new Entry(i, 280 + i));
        for(int i = 5; i < 10; ++i) entries.add(new Entry(i, 280 - i));

        // 차트 설정
        LineData lineData = getLineData(entries);
        chart_pitch.setData(lineData);

    }

    public void getWPSchart() {
        // 차트 리소스를 찾음
        chart_wps = (LineChart)findViewById(R.id.chart_wps);

        // 데이터를 설정함
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < 5; ++i) entries.add(new Entry(i, i + 2));
        for(int i = 5; i < 10; ++i) entries.add(new Entry(i, 10 - i));

        // 차트 설정
        LineData lineData = getLineData(entries);
        chart_wps.setData(lineData);
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
        Intent intent = new Intent();
        intent.putExtra("result", "Close PopUp");
        setResult(RESULT_OK, intent);
        finish();
    }
}
