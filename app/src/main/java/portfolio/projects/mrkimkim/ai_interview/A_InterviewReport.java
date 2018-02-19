package portfolio.projects.mrkimkim.ai_interview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import portfolio.projects.mrkimkim.ai_interview.Utils.item_subtitle;

public class A_InterviewReport extends Activity {
    int parentWidth = 0;
    ConstraintLayout parentConstraintLayout;
    RelativeLayout barHappy, barNeutral, barSad, barNervous;
    FlexboxLayout flexbox;
    LineChart chart_emotion, chart_wps, chart_pitch;

    ListView subtitleView;
    ListViewAdapter adapter;
    RelativeLayout standard;

    ArrayList<Float> emotion_timestamp, wps_timestamp, pitch_timestamp;
    ArrayList<Float> happy, neutral, sad, nervous, wps, pitch;
    Float avg_happy, avg_neutral, avg_sad, avg_nervous, avg_wps, avg_pitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        getApplicationData(intent);

        /* 풀 스크린 조정 */
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        int screenHeight = (int) (metrics.heightPixels * 0.90);
        setContentView(R.layout.dialog_show_interview_report);
        getWindow().setLayout(screenWidth, screenHeight);

        /* 차트 리소스를 찾음 */
        chart_emotion = (LineChart)findViewById(R.id.chart_emotion);
        chart_wps = (LineChart)findViewById(R.id.chart_wps);
        chart_pitch = (LineChart)findViewById(R.id.chart_pitch);

        // 태그 설정
        ArrayList<String> keyWord = new ArrayList<String>();
        keyWord.add("김김이");
        keyWord.add("김성희");
        keyWord.add("호호호");
        keyWord.add("개발자");
        keyWord.add("목표탈경");
        keyWord.add("게임보이");
        setKeyword(getApplicationContext(), keyWord);

        setGraphEmotion(); // 감정 바 설정
        setChartEmotion(); // 감정 차트
        setChartWps(); // 초당 단어 속도
        setChartPitch(); // 음성 피치
        //setSubtitle(null, null); // 자막
    }

    /* 이전 액티비티에서 받은 인텐트 데이터를 얻는다*/
    public void getApplicationData(Intent intent) {
        // Emotion 데이터 받기
        emotion_timestamp = (ArrayList<Float>) intent.getSerializableExtra("emotion_timestamp");
        happy = (ArrayList<Float>) intent.getSerializableExtra("happy");
        neutral = (ArrayList<Float>) intent.getSerializableExtra("neutral");
        sad = (ArrayList<Float>) intent.getSerializableExtra("sad");
        nervous = (ArrayList<Float>) intent.getSerializableExtra("nervous");
        avg_happy = intent.getFloatExtra("avg_happy", 0.0f);
        avg_neutral = intent.getFloatExtra("avg_neutral", 0.0f);
        avg_sad = intent.getFloatExtra("avg_sad", 0.0f);
        avg_nervous = intent.getFloatExtra("avg_nervous", 0.0f);

        // Pitch 데이터 받기
        pitch_timestamp = (ArrayList<Float>) intent.getSerializableExtra("pitch_timestamp");
        pitch = (ArrayList<Float>) intent.getSerializableExtra("pitch");
        avg_pitch = intent.getFloatExtra("avg_pitch", 0.0f);

        // WPS 데이터 받기
        wps_timestamp = (ArrayList<Float>) intent.getSerializableExtra("wps_timestamp");
        wps = (ArrayList<Float>) intent.getSerializableExtra("wps");
        avg_wps = intent.getFloatExtra("avg_wps", 0.0f);
    }

    /* 키워드 표시*/
    public void setKeyword(Context context, ArrayList<String> tag) {
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

    /* 감정 가로 막대 표시*/
    public void setGraphEmotion() {
        parentConstraintLayout = (ConstraintLayout)findViewById(R.id.show_interview_result_emotionBox);
        parentConstraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parentConstraintLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                parentWidth = (int)(parentConstraintLayout.getMeasuredWidth() * 0.55);
            }
        });

        barHappy = (RelativeLayout)findViewById(R.id.show_interview_result_barHappy);
        barHappy.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (parentWidth > 0) {
                    Log.d("HAPPY : ", String.valueOf(avg_happy));
                    barHappy.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) barHappy.getLayoutParams();
                    lp.width = (int) (parentWidth * avg_happy / 100.0);
                    barHappy.setLayoutParams(lp);
                }
            }
        });

        barNeutral = (RelativeLayout)findViewById(R.id.show_interview_result_barNeutral);
        barNeutral.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (parentWidth > 0) {
                    Log.d("NEUTRAL : ", String.valueOf(avg_neutral));
                    barNeutral.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) barNeutral.getLayoutParams();
                    lp.width = (int) (parentWidth * avg_neutral / 100.0);
                    barNeutral.setLayoutParams(lp);
                }
            }
        });

        barSad = (RelativeLayout)findViewById(R.id.show_interview_result_barSad);
        barSad.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (parentWidth > 0) {
                    Log.d("SAD : ", String.valueOf(avg_sad));
                    barSad.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) barSad.getLayoutParams();
                    lp.width = (int) (parentWidth * avg_sad / 100.0);
                    barSad.setLayoutParams(lp);
                }
            }
        });


        barNervous = (RelativeLayout)findViewById(R.id.show_interview_result_barNervous);
        barNervous.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (parentWidth > 0) {
                    Log.d("NERVOUS : ", String.valueOf(avg_nervous));
                    barNervous.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) barNervous.getLayoutParams();
                    lp.width = (int) (parentWidth * avg_nervous / 100.0);
                    barNervous.setLayoutParams(lp);
                }

            }
        });
    }

    /* 감정 꺾은 선 표시 */
    public void setChartEmotion() {
        ArrayList<ArrayList<Float>> dataset = new ArrayList<ArrayList<Float>>();
        dataset.add(happy);
        dataset.add(neutral);
        dataset.add(sad);
        dataset.add(nervous);

        // 데이터 설정
        ArrayList<List<Entry>> entries = new ArrayList<List<Entry>>();
        for(int i = 0; i < 4; ++i) {
            entries.add(new ArrayList<Entry>());
            for(int j = 0; j < emotion_timestamp.size(); ++j) {
                Log.d("TIME / EMO :", String.valueOf(emotion_timestamp.get(j)) + "/" + dataset.get(i).get(j));
                entries.get(i).add(new Entry(emotion_timestamp.get(j), dataset.get(i).get(j)));
            }
            Log.d("=== END === :", "END");
        }

        // 차트 설정
        LineData lineData = getCurvedLineData(entries);

    }

    /* 발음 속도 꺾은 선 표시 */
    public void setChartWps() {
        // 데이터 설정
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < wps_timestamp.size(); ++i) entries.add(new Entry(wps_timestamp.get(i), wps.get(i)));

        // 차트 설정
        LineData lineData = getLineData(entries, "WPS");
        chart_wps.setData(lineData);
    }

    /* 음정  꺾은 선 표시 */
    public void setChartPitch() {
        // 데이터 설정
        List<Entry> entries = new ArrayList<Entry>();
        for(int i = 0; i < pitch_timestamp.size(); ++i) entries.add(new Entry(pitch_timestamp.get(i), pitch.get(i)));

        // 차트 설정
        LineData lineData = getLineData(entries, "PITCH");
        chart_pitch.setData(lineData);
    }

    /* 꺾은 선 복수 데이터 */
    public LineData getCurvedLineData(ArrayList<List<Entry>> entries) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<LineDataSet> dataSet = new ArrayList<LineDataSet>();
        String[] Label = new String[]{"Happy", "Neutral", "Sad", "Nervous"};
        int[] color = new int[]{Color.GREEN, Color.MAGENTA, Color.BLUE, Color.BLACK};

        for(int i = 0; i < 4; ++i) {
            dataSet.add(new LineDataSet(entries.get(i), Label[i]));
            dataSet.get(i).setMode(LineDataSet.Mode.LINEAR);
            dataSet.get(i).setCubicIntensity(0.2f);
            dataSet.get(i).setDrawCircles(false);
            dataSet.get(i).setLineWidth(1.0f);
            dataSet.get(i).setCircleRadius(4f);
            dataSet.get(i).setCircleColor(color[i]);
            dataSet.get(i).setHighLightColor(Color.rgb(244, 117, 117));
            dataSet.get(i).setColor(color[i]);
            dataSet.get(i).setFillAlpha(100);
            dataSet.get(i).setDrawHorizontalHighlightIndicator(false);
            dataSet.get(i).setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });
            dataSets.add(dataSet.get(i));
        }

        LineData lineData = new LineData(dataSets);
        lineData.setDrawValues(true);
        chart_emotion.setData(lineData);
        return lineData;
    }

    /* 꺾은 선 단일 데이터 */
    public LineData getLineData(List<Entry> entries, String Label) {
        LineDataSet dataSet = new LineDataSet(entries, Label);
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

    /* 자막 표시 */
    public void setSubtitle(ArrayList<String> timestamp, ArrayList<String> text) {
        /*
        adapter = new ListViewAdapter();
        subtitleView = (ListView)findViewById(R.id.dialog_show_interview_report_subtitleView);
        subtitleView.setAdapter(adapter);

        for(int i = 0; i < timestamp.size(); ++i) {
            adapter.addItem(timestamp.get(i), text.get(i));
        }
        */
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
    }

}
