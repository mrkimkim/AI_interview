package portfolio.projects.mrkimkim.ai_interview;

import android.content.Intent;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class UploadVideo extends AppCompatActivity {
    private Socket socket;
    private BufferedReader networkReader;
    private OutputStream networkWriter;

    private Handler mHandler;

    private Info mUserInfo = new Info();
    private Info mMetaInfo = new Info();
    private String ServerState;

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
        setContentView(R.layout.activity_send_to_server);

        // 이전 액티비티의 영상 인텐트 전달
        Intent intent = getIntent();
        String path = intent.getStringExtra("uri");
        uri = Uri.parse(path);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.d("State : ", "I am trying");
        // Connect to Server
        mHandler = new Handler();
        try {
            setSocket(getString(R.string.ServerIP), Integer.parseInt(getString(R.string.ServerPort)));
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
                String sData;

                // Upload UserInfo
                mUserInfo.setDataString(new String[]{"Name","Gender","Age"}, new String[]{"HyeonGyuJang", "Man", "28"});
                sData = mUserInfo.getDataString();
                networkWriter.write(sData.getBytes(), 0, sData.length());
                networkWriter.flush();

                while (true) {
                    ServerState = networkReader.readLine();
                    if (ServerState.length() > 10) break;
                }

                // Load Video
                String[] FileName = uri.getPath().split("/");
                File file = new File(uri.getPath());
                FileInputStream fis = new FileInputStream(file);
                int read = 0;


                // Upload MetaInfo
                mMetaInfo.setDataString(new String[]{"fileName","size"}, new String[]{FileName[FileName.length - 1],Long.toString(file.length())});
                sData = mMetaInfo.getDataString();
                networkWriter.write(sData.getBytes(), 0, sData.length());
                networkWriter.flush();

                while (true) {
                    ServerState = networkReader.readLine();
                    if (ServerState.length() > 10) break;
                }


                // Upload Video
                BufferedInputStream bis = new BufferedInputStream(fis);
                while((read = bis.read(buffers, 0, maxBufferSize)) != -1) {
                    networkWriter.write(buffers, 0, buffers.length);
                }
                networkWriter.flush();

                while (true) {
                    ServerState = networkReader.readLine();
                    if (ServerState.length() > 10) break;
                }

                // Success to Upload Video
            } catch (Exception e) {
                e.printStackTrace();
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

    class Info {
        String[] Field;
        String[] Data;
        int Len = 100;

        public Info() {};

        public String getDataString() {
            String ret = "";
            for (int i = 0; i < Field.length; ++i) {
                ret += Field[i] + ":" + Data[i];
                if (i != Field.length - 1) ret += ":";
            }
            ret += new String(new char[Len - ret.length()]).replace("\0", " ");
            return ret;
        }

        public void setDataString(String[] Field, String[] Data) {
            this.Field = new String[Field.length];
            this.Data = new String[Data.length];

            for(int i = 0; i < Field.length; ++i) this.Field[i] = Field[i];
            for(int i = 0; i < Data.length; ++i) this.Data[i] = Data[i];
        }

        public void setDataLen(int len) {
            this.Len = len;
        }
    }
}
