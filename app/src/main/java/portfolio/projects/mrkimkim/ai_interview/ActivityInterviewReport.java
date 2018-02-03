package portfolio.projects.mrkimkim.ai_interview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import portfolio.projects.mrkimkim.ai_interview.Utils.item_subtitle;

public class ActivityInterviewReport extends Activity {
    FlexboxLayout flexbox;
    LineView chart_emotion;
    LineChart chart_wps, chart_pitch;

    ListView subtitleView;
    ListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();

        // 데이터 파싱 및 가공 작업
        // processReportData();

        // 풀 스크린 조정
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        int screenHeight = (int) (metrics.heightPixels * 0.90);
        setContentView(R.layout.dialog_show_interview_report);
        getWindow().setLayout(screenWidth, screenHeight);

        // 태그 설정
        setTag(this, null);

        // 감정 차트
        setChart_emotion(null, null);

        // 초당 단어 속도
        setChart_wps(null);

        // 음성 피치
        setChart_pitch(null);

        // 자막
        setSubtitle(null, null);
    }


    public void setTag(Context context, ArrayList<String> tag) {
        ArrayList<Integer> drawable;
        flexbox = (FlexboxLayout)findViewById(R.id.flexbox);

        // 태그 설정
        ChipCloudConfig config = new ChipCloudConfig()
                .selectMode(ChipCloud.SelectMode.multi)
                .checkedChipColor(Color.parseColor("#50b7ff"))
                .checkedTextColor(Color.parseColor("#ffffff"))
                .uncheckedChipColor(Color.parseColor("#50b7ff"))
                .uncheckedTextColor(Color.parseColor("#ffffff"))
                .useInsetPadding(true);

        // 태그 추가
        ChipCloud chipCloud = new ChipCloud(context, flexbox, config);
        for(int i = 0; i < Math.min(tag.size(), 10); ++i) {
            if (i == 0) chipCloud.addChip(tag.get(i), ContextCompat.getDrawable(context, R.drawable.icon_1st));
            else if (i == 1) chipCloud.addChip(tag.get(i), ContextCompat.getDrawable(context, R.drawable.icon_2nd));
            else if (i == 2) chipCloud.addChip(tag.get(i), ContextCompat.getDrawable(context, R.drawable.icon_3rd));
            else chipCloud.addChip(tag.get(i));
        }
    }


    public void setChart_emotion(ArrayList<String> x_axis, ArrayList<ArrayList<Float>> y_axis) {
        chart_emotion = (LineView) findViewById(R.id.line_view);
        chart_emotion.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional
        chart_emotion.setBottomTextList(x_axis);
        chart_emotion.setColorArray(new int[]{Color.BLACK,Color.GREEN,Color.GRAY});
        chart_emotion.setFloatDataList(y_axis); //or chart_emotion.setFloatDataList(floatDataLists)
        chart_emotion.setDrawDotLine(true);

        //chart_emotion.setPivotY(2);
    }


    public void setChart_wps(List<Entry> entries) {
        // 차트 리소스를 찾음
        chart_wps = (LineChart)findViewById(R.id.chart_wps);

        // 차트 설정
        LineData lineData = getLineData(entries);
        chart_wps.setData(lineData);
    }


    public void setChart_pitch(List<Entry> entries) {
        // 차트 리소스를 찾음
        chart_pitch = (LineChart)findViewById(R.id.chart_wps);

        // 차트 설정
        LineData lineData = getLineData(entries);
        chart_pitch.setData(lineData);
    }


    public LineData getLineData(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "WPS");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(true);
        dataSet.setLineWidth(1.0f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setHighLightColor(Color.rgb(244, 117, 117));
        dataSet.setColor(Color.WHITE);
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


    public void setSubtitle(ArrayList<String> timestamp, ArrayList<String> text) {
        adapter = new ListViewAdapter();
        subtitleView = (ListView)findViewById(R.id.dialog_show_interview_report_subtitleView);
        subtitleView.setAdapter(adapter);

        for(int i = 0; i < timestamp.size(); ++i) {
            adapter.addItem(timestamp.get(i), text.get(i));
        }
    }

    class ListViewAdapter extends BaseAdapter {
        private ArrayList<item_subtitle> mItems = new ArrayList<item_subtitle>();

        public ListViewAdapter() {

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_subtitle, parent, false);
            }

            TextView timestamp = (TextView)convertView.findViewById(R.id.item_transcript_timestamp);
            TextView text = (TextView)convertView.findViewById(R.id.item_transcript_text);

            item_subtitle item = mItems.get(position);
            timestamp.setText(item.getTimestamp());
            text.setText(item.getText());

            return convertView;
        }

        @Override
        public int getCount() {
            return mItems.size() ;
        }

        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return mItems.get(position) ;
        }

        public void addItem(String timestamp, String text) {
            mItems.add(new item_subtitle(timestamp, text));
        }
    }


    public void mOnClose(View v) {
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
        finish();
        return;
    }
}
