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

    TextView tvDuration, tvProgress, tvSubtitle;
    TextView tvHappy, tvNeutral, tvSad, tvNervous;
    TextView tvWps, tvPitch;

    ImageView ivEmotion, ivWps, ivPitch, ivSubtitle, ivReport;
    CircleButton btn_Play;

    String Video_path, Emotion_path, Subtitle_path, Pitch_path;
    int duration, pos = 0;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_interview_video);

        // 이전 인텐트 값 복원
        Intent intent = getIntent();
        loadIntentData(intent);

        // UI 세팅
        setUiElement();
    }

    /* 동영상 SeekBar를 제어하는 스레드 */
    class SeekBarThread extends Thread {
        @Override
        public void run() {
            while(isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 100); // 0.1초 단위로 seekBar는 움직임
                pos = seekBar.getProgress();

                // 현재 재생 위치
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 현재 진행상황 표시
                        tvProgress.setText(String.format("%02d", pos/600) + ":" + String.format("%02d", (pos%600)/10));

                        // 현재 Emotion 표시
                        tvSubtitle.setText(subtitleData.getSingleWord(pos));

                        // 현재 WPS 표시
                        tvWps.setText(subtitleData.getSingleWPS(pos));

                        // 현재 Pitch 표시
                        tvPitch.setText(pitchData.getSinglePitch(pos));

                        // 현재 자막 표시
                        tvHappy.setText(emotionData.getSingleHappy(pos));
                        tvNeutral.setText(emotionData.getSingleNeutral(pos));
                        tvSad.setText(emotionData.getSingleSad(pos));
                        tvNervous.setText(emotionData.getSingleNervous(pos));
                    }
                });
            }
        }
    }

    /* 인텐트 데이터를 로드한다 */
    private void loadIntentData(Intent intent) {
        Video_path = intent.getStringExtra("Video_path");
        Emotion_path = intent.getStringExtra("Emotion_path");
        Subtitle_path = intent.getStringExtra("Subtitle_path");
        Pitch_path = intent.getStringExtra("Pitch_path");

        // 데이터 로드
        interviewData = new InterviewData(Emotion_path, Subtitle_path, Pitch_path);
        emotionData = interviewData.getmEmotionData();
        subtitleData = interviewData.getmSubtitleData();
        pitchData = interviewData.getmPitchData();
    }

    /* UI 세팅 함수 */
    private void setUiElement() {
        // onTogglePlay 버튼 설정
        btn_Play = (CircleButton)findViewById(R.id.show_interview_video_btn_play);
        btn_Play.setClickable(false);

        // Report 버튼 설정
        ivReport = (ImageView)findViewById(R.id.show_interview_result_btn_report);

        // 표시 토글버튼
        ivEmotion = (ImageView)findViewById(R.id.show_interview_video_btn_emotion);
        ivWps = (ImageView)findViewById(R.id.show_interview_video_btn_wps);
        ivPitch = (ImageView)findViewById(R.id.show_interview_video_btn_pitch);
        ivSubtitle = (ImageView)findViewById(R.id.show_interview_video_btn_subtitle);

        // Hud 레이아웃
        hudEmotion = (ConstraintLayout)findViewById(R.id.show_interview_video_hud1);
        hudWps = (ConstraintLayout)findViewById(R.id.show_interview_video_hud2);
        hudPitch = (ConstraintLayout)findViewById(R.id.show_interview_video_hud3);

        // 데이터 표시 TextView
        tvDuration = (TextView)findViewById(R.id.show_interview_video_tv_Duration);
        tvProgress = (TextView)findViewById(R.id.show_interview_video_tv_Progress);
        tvSubtitle = (TextView)findViewById(R.id.show_interview_video_tv_subtitle);
        tvHappy = (TextView)findViewById(R.id.show_interview_video_emo_happy);
        tvNeutral = (TextView)findViewById(R.id.show_interview_video_emo_neutral);
        tvSad = (TextView)findViewById(R.id.show_interview_video_emo_sad);
        tvNervous = (TextView)findViewById(R.id.show_interview_video_emo_nervous);
        tvWps = (TextView)findViewById(R.id.show_interview_video_tv_wps);
        tvPitch = (TextView)findViewById(R.id.show_interview_video_tv_pitch);

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
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = mediaPlayer.getDuration();
        seekBar.setMax(duration / 100);
        seekBar.setProgress(0);
        tvDuration.setText(String.format("%02d",duration/60000) + ":" + String.format("%02d",(duration%60000)/1000));
        btn_Play.setClickable(true);
    }

    /* 리포트 보기 버튼 */
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

    /* 토글 버튼 설정 */
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
            ivEmotion.setImageResource(R.drawable.icon_emotion);
        } else {
            hudEmotion.setVisibility(View.VISIBLE);
            ivEmotion.setImageResource(R.drawable.icon_emotion_pressed);
        }
    }

    public void onToggleWps(View v) {
        if (hudWps.getVisibility() == View.VISIBLE) {
            hudWps.setVisibility(View.GONE);
            ivWps.setImageResource(R.drawable.icon_wps);
        } else {
            hudWps.setVisibility(View.VISIBLE);
            ivWps.setImageResource(R.drawable.icon_wps_pressed);
        }
    }

    public void onTogglePitch(View v) {
        if (hudPitch.getVisibility() == View.VISIBLE) {
            hudPitch.setVisibility(View.GONE);
            ivPitch.setImageResource(R.drawable.icon_pitch);
        } else {
            hudPitch.setVisibility(View.VISIBLE);
            ivPitch.setImageResource(R.drawable.icon_pitch_pressed);
        }
    }

    public void onToggleSubtitle(View v) {
        if (tvSubtitle.getVisibility() == View.VISIBLE) {
            tvSubtitle.setVisibility(View.GONE);
            ivSubtitle.setImageResource(R.drawable.icon_subtitle);
        } else {
            tvSubtitle.setVisibility(View.VISIBLE);
            ivSubtitle.setImageResource(R.drawable.icon_subtitle_pressed);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder Holder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
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
            }
        });
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
    public void surfaceDestroyed(SurfaceHolder Holder) { }

    @Override
    public void surfaceChanged(SurfaceHolder Holder, int i, int j, int k) { }

}
