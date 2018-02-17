package portfolio.projects.mrkimkim.ai_interview;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import at.markushi.ui.CircleButton;
import portfolio.projects.mrkimkim.ai_interview.Utils.InterviewData;


public class A_InterviewVideo extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    InterviewData interviewData;
    InterviewData.EmotionData emotionData;
    InterviewData.SubtitleData subtitleData;
    InterviewData.PitchData pitchData;

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    ConstraintLayout hudEmotion, hudWps, hudPitch;
    SeekBar seekBar;
    TextView tv_duration, tv_progress, tv_subtitle;
    TextView tv_happy, tv_neutral, tv_sad, tv_nervous;
    TextView tv_wps, tv_pitch;
    ImageView btnEmotion, btnWps, btnPitch, btnSubtitle;
    ImageView btnReport;
    CircleButton btn_Play;


    String Video_path, Emotion_path, Subtitle_path, Pitch_path;
    int duration, pos = 0;
    boolean isPlaying = false;


    class SeekBarThread extends Thread {
        @Override
        public void run() {
            while(isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 100); // 0.1초 단위임
                pos = seekBar.getProgress();

                // 현재 재생 위치
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 현재 진행상황 표시
                        tv_progress.setText(String.format("%02d", pos/600) + ":" + String.format("%02d", (pos%600)/10));

                        // 현재 Emotion 표시
                        tv_subtitle.setText(subtitleData.getSingleWord(pos + 2));

                        // 현재 WPS 표시
                         tv_wps.setText(subtitleData.getSingleWPS(pos + 2));

                        // 현재 Pitch 표시
                        tv_pitch.setText(pitchData.getSinglePitch(pos + 2));

                        // 현재 자막 표시
                        tv_happy.setText(emotionData.getSingleHappy(pos));
                        tv_neutral.setText(emotionData.getSingleNeutral(pos));
                        tv_sad.setText(emotionData.getSingleSad(pos));
                        tv_nervous.setText(emotionData.getSingleNervous(pos));
                    }
                });

                // 재생 완료한 경우
                if (seekBar.getMax() - 2 <= pos && pos <= seekBar.getMax() + 2) {
                    isPlaying = false;
                    pos = 0;
                    seekBar.setProgress(pos);
                    mediaPlayer.seekTo(pos);
                    mediaPlayer.pause();

                    // 버튼 바꾸기
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_Play.setImageResource(R.drawable.icon_play);
                        }
                    });
                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_interview_video);


        // 이전 인텐트 값 복원
        Intent intent = getIntent();
        Video_path = intent.getStringExtra("Video_path");
        Emotion_path = intent.getStringExtra("Emotion_path");
        Subtitle_path = intent.getStringExtra("Subtitle_path");
        Pitch_path = intent.getStringExtra("Pitch_path");


        // 데이터 로드
        interviewData = new InterviewData(Emotion_path, Subtitle_path, Pitch_path);
        emotionData = interviewData.getmEmotionData();
        subtitleData = interviewData.getmSubtitleData();
        pitchData = interviewData.getmPitchData();


        // 비디오 설정
        surfaceView = (SurfaceView)findViewById(R.id.show_interview_video_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(A_InterviewVideo.this);


        // SeekBar 설정
        seekBar = (SeekBar)findViewById(R.id.show_interview_video_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                mediaPlayer.seekTo(seekBar.getProgress() * 100);
                mediaPlayer.start();
                new SeekBarThread().start();
            }
        });


        // onTogglePlay 버튼 설정
        btn_Play = (CircleButton)findViewById(R.id.show_interview_video_btn_play);
        btn_Play.setClickable(false);

        // Report 버튼 설정
        btnReport = (ImageView)findViewById(R.id.show_interview_result_btn_report);

        // Progress / Duration 텍스트 뷰 설정
        tv_duration = (TextView)findViewById(R.id.show_interview_video_tv_Duration);
        tv_progress = (TextView)findViewById(R.id.show_interview_video_tv_Progress);


        // Hud 설정
        hudEmotion = (ConstraintLayout)findViewById(R.id.show_interview_video_hud1);
        hudWps = (ConstraintLayout)findViewById(R.id.show_interview_video_hud2);
        hudPitch = (ConstraintLayout)findViewById(R.id.show_interview_video_hud3);
        tv_subtitle = (TextView)findViewById(R.id.show_interview_video_tv_subtitle);
        tv_happy = (TextView)findViewById(R.id.show_interview_video_emo_happy);
        tv_neutral = (TextView)findViewById(R.id.show_interview_video_emo_neutral);
        tv_sad = (TextView)findViewById(R.id.show_interview_video_emo_sad);
        tv_nervous = (TextView)findViewById(R.id.show_interview_video_emo_nervous);
        tv_wps = (TextView)findViewById(R.id.show_interview_video_tv_wps);
        tv_pitch = (TextView)findViewById(R.id.show_interview_video_tv_pitch);

        // Hud 컨트롤 버튼
        btnEmotion = (ImageView)findViewById(R.id.show_interview_video_btn_emotion);
        btnWps = (ImageView)findViewById(R.id.show_interview_video_btn_wps);
        btnPitch = (ImageView)findViewById(R.id.show_interview_video_btn_pitch);
        btnSubtitle = (ImageView)findViewById(R.id.show_interview_video_btn_subtitle);
    }


    @Override
    public void surfaceCreated(SurfaceHolder Holder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setOnPreparedListener(A_InterviewVideo.this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.setDataSource(Video_path);
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = mediaPlayer.getDuration();
        seekBar.setMax(duration / 100);
        seekBar.setProgress(0);
        tv_duration.setText(String.format("%02d",duration/60000) + ":" + String.format("%02d",(duration%60000)/1000));
        btn_Play.setClickable(true);
    }

    // 리포트 버튼
    public void onViewReport(View v) {
        Intent intent = new Intent(A_InterviewVideo.this, A_InterviewReport.class);

        // Emotion 평균 데이터
        intent.putExtra("avg_happy", emotionData.getAvg_happy());
        intent.putExtra("avg_neutral", emotionData.getAvg_neutral());
        intent.putExtra("avg_sad", emotionData.getAvg_sad());
        intent.putExtra("avg_nervous", emotionData.getAvg_nervous());

        // Emotion 그래프 데이터
        intent.putExtra("emotion_timestamp", emotionData.getTimestamp());
        intent.putExtra("happy", emotionData.getHappy());
        intent.putExtra("neutral", emotionData.getNeutral());
        intent.putExtra("sad", emotionData.getSad());
        intent.putExtra("nervous", emotionData.getNervous());

        // WPS 그래프 데이터
        intent.putExtra("wps_timestamp", subtitleData.getTimestamp());
        intent.putExtra("wps", subtitleData.getWps());
        intent.putExtra("avg_wps", subtitleData.getAvg_wps());

        // Pitch 그래프 데이터
        intent.putExtra("pitch_timestamp", pitchData.getTimestamp());
        intent.putExtra("pitch", pitchData.getPitch());
        intent.putExtra("avg_pitch", pitchData.getAvg_pitch());

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    // 토글 버튼 설정
    public void onTogglePlay(View v) {
        btn_Play.setClickable(false);
        if (!isPlaying) {
            isPlaying = true;
            mediaPlayer.start();
            new SeekBarThread().start();
            btn_Play.setImageResource(R.drawable.icon_pause);
        }
        else {
            isPlaying = false;
            mediaPlayer.pause();
            seekBar.setProgress(pos);
            btn_Play.setImageResource(R.drawable.icon_play);
        }
        btn_Play.setClickable(true);
    }

    public void onToggleEmotion(View v) {
        if (hudEmotion.getVisibility() == View.VISIBLE) {
            hudEmotion.setVisibility(View.GONE);
            btnEmotion.setImageResource(R.drawable.icon_emotion);
        } else {
            hudEmotion.setVisibility(View.VISIBLE);
            btnEmotion.setImageResource(R.drawable.icon_emotion_pressed);
        }
    }

    public void onToggleWps(View v) {
        if (hudWps.getVisibility() == View.VISIBLE) {
            hudWps.setVisibility(View.GONE);
            btnWps.setImageResource(R.drawable.icon_wps);
        } else {
            hudWps.setVisibility(View.VISIBLE);
            btnWps.setImageResource(R.drawable.icon_wps_pressed);
        }
    }

    public void onTogglePitch(View v) {
        if (hudPitch.getVisibility() == View.VISIBLE) {
            hudPitch.setVisibility(View.GONE);
            btnPitch.setImageResource(R.drawable.icon_pitch);
        } else {
            hudPitch.setVisibility(View.VISIBLE);
            btnPitch.setImageResource(R.drawable.icon_pitch_pressed);
        }
    }

    public void onToggleSubtitle(View v) {
        if (tv_subtitle.getVisibility() == View.VISIBLE) {
            tv_subtitle.setVisibility(View.GONE);
            btnSubtitle.setImageResource(R.drawable.icon_subtitle);
        } else {
            tv_subtitle.setVisibility(View.VISIBLE);
            btnSubtitle.setImageResource(R.drawable.icon_subtitle_pressed);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder Holder) { }

    @Override
    public void surfaceChanged(SurfaceHolder Holder, int i, int j, int k) { }

}
