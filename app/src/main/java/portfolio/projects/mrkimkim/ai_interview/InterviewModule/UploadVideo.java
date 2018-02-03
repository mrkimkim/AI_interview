package portfolio.projects.mrkimkim.ai_interview.InterviewModule;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.ActivityMain;
import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;
import portfolio.projects.mrkimkim.ai_interview.R;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

public class UploadVideo extends AppCompatActivity {
    private Socket socket;
    private InputStream networkReader;
    private OutputStream networkWriter;
    private Handler mHandler;
    private Uri uri;

    private long question_idx;
    private String video_path;

    ProgressBar progressBar;
    TextView tv_progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        progressBar = (ProgressBar) findViewById(R.id.upload_video_progress_bar);
        tv_progress = (TextView) findViewById(R.id.upload_video_tv_progress);

        // 버전 체크
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // 이전 액티비티의 영상 인텐트 전달
        Intent intent = getIntent();
        String path = intent.getStringExtra("uri");

        question_idx = intent.getLongExtra("question_idx", 0);
        video_path = intent.getStringExtra("video_path");
        uri = Uri.parse(path);


        // 서버에 연결
        mHandler = new Handler();
        try {
            setSocket(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port)));
            Upload.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setProgressBar(final int p) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(p);
                tv_progress.setText(String.valueOf(p/10) + "% 전송 완료");
            }
        });
    }

    public void setProgressText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_progress.setText(msg);
            }
        });
    }

    private Thread Upload = new Thread() {
        public void run() {
            try {
                int maxBufferSize = 2048;
                final byte[] buffers = new byte[maxBufferSize];

                // 유저 인증 토큰을 전송함.
                NetworkService.Auth_User(networkReader, networkWriter);

                // 비디오를 로드함
                String[] FileName = uri.getPath().split("/");
                File file = new File(uri.getPath());
                FileInputStream fis = new FileInputStream(file);
                final long video_size = file.length();
                int read = 0;


                // 헤더 전송 (20byte)
                byte[] Opcode = NetworkService.getHeader(getResources().getInteger(R.integer.op_upload), Long.valueOf(String.valueOf(question_idx)), video_size);
                networkWriter.write(Opcode);
                networkWriter.flush();


                // 비디오 데이터 전송
                int uploaded_size = 0;
                BufferedInputStream bis = new BufferedInputStream(fis);
                while((read = bis.read(buffers, 0, maxBufferSize)) != -1) {
                    networkWriter.write(buffers, 0, read);
                    uploaded_size += read;
                    Log.d("BUFFER SIZE : ", String.valueOf(uploaded_size));
                    setProgressBar((uploaded_size * 1000) / Integer.valueOf(String.valueOf(video_size)));
                }
                networkWriter.flush();


                // 업로드 된 영상의 task_idx를 DB에 저장한다.
                setProgressText("DB를 업데이트 중입니다.");

                byte[] packet = new byte[8];
                networkReader.read(packet, 0, 8);
                long task_idx = Functions.bytesToLong(packet);

                DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
                mDBHelper.update("InterviewData",
                        new String[]{"task_idx", "question_idx"},
                        new String[]{String.valueOf(task_idx), String.valueOf(question_idx)}, "video_path= ?", new String[]{video_path});

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UploadVideo.this, "영상 전송에 성공했습니다.", Toast.LENGTH_LONG).show();
                    }
                });

                // 메인 액티비티로 돌아간다.
                Intent intent = new Intent(UploadVideo.this, ActivityMain.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UploadVideo.this, "영상 전송에 실패했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            networkReader.close();
            networkWriter.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket(String ip, int port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            networkWriter = socket.getOutputStream();
            networkReader = socket.getInputStream();
        } catch (IOException e) {
            Log.d("Error : ", "failed to connect server");
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
