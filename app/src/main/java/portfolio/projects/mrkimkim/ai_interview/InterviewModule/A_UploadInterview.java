package portfolio.projects.mrkimkim.ai_interview.InterviewModule;

import android.content.Intent;
import android.net.Uri;
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
import portfolio.projects.mrkimkim.ai_interview.A_MainMenu;
import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;
import portfolio.projects.mrkimkim.ai_interview.R;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

public class A_UploadInterview extends AppCompatActivity {
    private Socket socket;
    private InputStream networkReader;
    private OutputStream networkWriter;
    private Uri uri;

    private long question_idx;
    private String video_path;

    ProgressBar progressBar;
    TextView tv_progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_interview);

        progressBar = (ProgressBar) findViewById(R.id.show_interview_video_seekbar);
        tv_progress = (TextView) findViewById(R.id.upload_video_tv_progress);

        // 이전 액티비티의 영상 인텐트, 문제 번호, 로컬 비디오 경로를 받음
        Intent intent = getIntent();
        String path = intent.getStringExtra("uri");
        question_idx = intent.getLongExtra("questionIdx", 0);
        video_path = intent.getStringExtra("video_path");
        uri = Uri.parse(path);


        // 서버에 연결
        try {
            setSocket(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port)));
            Upload.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

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

    /* 업로드 완료 비율을 표시 */
    public void setProgressBar(final int p) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(p);
                tv_progress.setText(String.valueOf(p/10) + "% 전송 완료");
            }
        });
    }

    /* 상태 메시지를 표시 */
    public void setProgressMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_progress.setText(msg);
            }
        });
    }

    /* 데이터 업로드 수행 */
    private Thread Upload = new Thread() {
        public void run() {
            try {
                int maxBufferSize = 2048;
                final byte[] buffers = new byte[maxBufferSize];

                // 유저 인증 토큰을 전송함.
                NetworkService.Auth_User(networkReader, networkWriter);

                // 비디오를 로드함
                File file = new File(uri.getPath());
                FileInputStream fis = new FileInputStream(file);
                final long video_size = file.length();
                int read;


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
                    setProgressBar((uploaded_size * 1000) / Integer.valueOf(String.valueOf(video_size)));
                }
                networkWriter.flush();

                // DB 업데이트 메시지 표시
                setProgressMsg(getString(R.string.updatingLocalDB));

                /* 로컬 DB를 패치 */
                byte[] packet = NetworkService.receive(networkReader, 8);
                long task_idx = Functions.bytesToLong(packet);
                updateLocalDB(task_idx);

                /* 데이터 전송 성공을 알림 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(A_UploadInterview.this, getString(R.string.finishUploadInterview), Toast.LENGTH_LONG).show();
                    }
                });

                /* 메인 액티비티로 돌아간다. */
                Intent intent = new Intent(A_UploadInterview.this, A_MainMenu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(A_UploadInterview.this, getString(R.string.failUploadInterview), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    /* 로컬 데이터베이스의 task_idx를 서버와 동기화한다. */
    public void updateLocalDB(long task_idx) {
        DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
        mDBHelper.update("InterviewData",
                new String[]{"task_idx", "questionIdx"},
                new String[]{String.valueOf(task_idx), String.valueOf(question_idx)}, "video_path= ?", new String[]{video_path});
    }

    /* 소켓 초기화 */
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
