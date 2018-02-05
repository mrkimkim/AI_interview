package portfolio.projects.mrkimkim.ai_interview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;

import portfolio.projects.mrkimkim.ai_interview.Utils.InterviewData;


public class ActivityInterviewVideo extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    InterviewData interviewData;

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    SeekBar seekBar;

    String Video_path, Emotion_path, Subtitle_path;
    int duration;
    boolean isPlaying = false;

    class SeekBarThread extends Thread {
        @Override
        public void run() {
            while(isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
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
                if (seekBar.getMax() == progress) {
                    isPlaying = false;
                    mediaPlayer.stop();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                mediaPlayer.seekTo(seekBar.getProgress());
                mediaPlayer.start();
                new SeekBarThread().start();
            }
        });
    }

    private void playVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.setDataSource(Video_path);
                    duration = mediaPlayer.getDuration();
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
        /*
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        surfaceView.setLayoutParams(lp);
        */
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder Holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder Holder, int i, int j, int k) {

    }
}
