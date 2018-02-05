package portfolio.projects.mrkimkim.ai_interview;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import at.markushi.ui.CircleButton;
import portfolio.projects.mrkimkim.ai_interview.Utils.InterviewData;


public class ActivityInterviewVideo extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    InterviewData interviewData;

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    SeekBar seekBar;
    CircleButton btn_Play;
    TextView tv_duration, tv_progress;

    String Video_path, Emotion_path, Subtitle_path;
    int duration, pos = 0;
    boolean isPlaying = false;


    class SeekBarThread extends Thread {
        @Override
        public void run() {
            while(isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 100);
                pos = seekBar.getProgress();

                // 현재 재생 위치
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_progress.setText(String.format("%02d", pos/600) + ":" + String.format("%02d", (pos%600)/10));
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

        // 데이터 로드
        interviewData = new InterviewData(Emotion_path, Subtitle_path);

        // 비디오 설정
        surfaceView = (SurfaceView)findViewById(R.id.show_interview_video_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(ActivityInterviewVideo.this);


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

        // Play 버튼 설정
        btn_Play = (CircleButton)findViewById(R.id.show_interview_video_btn_play);
        btn_Play.setClickable(false);

        // Progress / Duration 텍스트 뷰 설정
        tv_duration = (TextView)findViewById(R.id.show_interview_video_tv_Duration);
        tv_progress = (TextView)findViewById(R.id.show_interview_video_tv_Progress);
    }

    public void Play(View v) {
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


    private void playVideo() {
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
    public void surfaceCreated(SurfaceHolder Holder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setOnPreparedListener(ActivityInterviewVideo.this);
        mediaPlayer.setScreenOnWhilePlaying(true);
        playVideo();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = mediaPlayer.getDuration();
        seekBar.setMax(duration / 100);
        seekBar.setProgress(0);
        tv_duration.setText(String.format("%02d",duration/60000) + ":" + String.format("%02d",(duration%60000)/1000));
        btn_Play.setClickable(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder Holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder Holder, int i, int j, int k) {

    }
}
