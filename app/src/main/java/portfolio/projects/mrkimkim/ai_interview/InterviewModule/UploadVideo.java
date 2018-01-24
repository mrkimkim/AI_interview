package portfolio.projects.mrkimkim.ai_interview.InterviewModule;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import portfolio.projects.mrkimkim.ai_interview.GlobalApplication;
import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;
import portfolio.projects.mrkimkim.ai_interview.R;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

public class UploadVideo extends AppCompatActivity {
    private Socket socket;
    private BufferedReader networkReader;
    private OutputStream networkWriter;
    private Handler mHandler;

    private Uri uri;
    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // 이전 액티비티의 영상 인텐트 전달
        Intent intent = getIntent();
        String path = intent.getStringExtra("uri");
        uri = Uri.parse(path);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Connect to Server
        mHandler = new Handler();
        try {
            setSocket(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port)));
            Upload.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Thread Upload = new Thread() {
        public void run() {
            try {
                int maxBufferSize = 2048;
                final byte[] buffers = new byte[maxBufferSize];

                // 유저 인증 토큰을 전송함.
                byte[] userId = Functions.longToBytes(GlobalApplication.mUserInfoManager.getUserProfile().getId());
                byte[] userToken = GlobalApplication.mUserInfoManager.getAppToken();
                byte[] packet = new byte[userId.length + userToken.length];
                networkWriter.write(packet);
                networkWriter.flush();

                // 비디오를 로드함
                String[] FileName = uri.getPath().split("/");
                File file = new File(uri.getPath());
                FileInputStream fis = new FileInputStream(file);
                long video_size = file.length();
                int read = 0;

                // 헤더 전송 (20byte)
                byte[] Opcode = NetworkService.getHeader(getResources().getInteger(R.integer.op_upload), 0, video_size);
                networkWriter.write(Opcode);
                networkWriter.flush();

                // 비디오 데이터 전송
                BufferedInputStream bis = new BufferedInputStream(fis);
                while((read = bis.read(buffers, 0, maxBufferSize)) != -1) {
                    networkWriter.write(buffers, 0, buffers.length);
                }
                networkWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "영상 전송에 실패했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void setSocket(String ip, int port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            networkWriter = socket.getOutputStream();
            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            Log.d("Error : ", "failed to connect server");
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
