package portfolio.projects.mrkimkim.ai_interview.NetworkModule;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.jsoup.Jsoup;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import portfolio.projects.mrkimkim.ai_interview.ActivityMain;
import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;
import portfolio.projects.mrkimkim.ai_interview.GlobalApplication;
import portfolio.projects.mrkimkim.ai_interview.R;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

public class LoginActivity extends AppCompatActivity {

    private SessionCallback callback;
    private long user_id;
    private long user_expiresInMilis;
    private String kakao_token;
    private byte[] app_token;
    UserProfile mUserProfile;

    String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Thread Policy를 지정
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // 기기 버전 체크 후 퍼미션 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissions()) {
                if (showRequestPermission()) {
                } else {
                    ActivityCompat.requestPermissions(this, permissions, 12345);
                }
            }
        }

        // Firebase 푸시 기기 등록
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();

        // 세션 콜백 등록
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        // 카카오 로그인 버튼
        LoginButton loginButton = (LoginButton)findViewById(R.id.com_kakao_login);
        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isConnected()) {
                        Toast.makeText(LoginActivity.this, "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
                if (isConnected()) return false;
                return true;
            }
        });

        // 세션 연결 시 즉시 사용자 정보를 받아옴
        if (Session.getCurrentSession().isOpened()) {
            loginButton.setVisibility(View.INVISIBLE);
            requestMe();
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 로그인 수행 결과를 받음.
    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            if (Session.getCurrentSession().isOpened()) requestMe();
        }

        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) Logger.e(exception);
        }
    }

    // 로그인 성공 시 사용자 정보를 받음.
    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) { Log.d("onFailure", errorResult + "");}

            @Override
            public void onSessionClosed(ErrorResult errorResult) { Log.d("onSessionClosed", errorResult + "");};

            @Override
            public void onSuccess(UserProfile userProfile) {
                GlobalApplication.mUserInfoManager.setUserProfile(userProfile);
                requestAccessTokenInfo();
            }

            @Override
            public void onNotSignedUp() { Toast.makeText(LoginActivity.this, "Not Signed Up", Toast.LENGTH_LONG).show();};
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    // 카카오로부터 로그인 토큰을 받아온다.
    public void requestAccessTokenInfo() {
        AuthService.requestAccessTokenInfo(new ApiResponseCallback<AccessTokenInfoResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d("requestAccessTokenInfo", "Fail to AccessTokenInfo");
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {

            }

            @Override
            public void onNotSignedUp() {
            }

            @Override
            public void onSuccess(AccessTokenInfoResponse accessTokenInfoResponse) {
                user_id = accessTokenInfoResponse.getUserId();
                kakao_token = Session.getCurrentSession().getTokenInfo().getAccessToken();
                GlobalApplication.mUserInfoManager.setKakaoToken(kakao_token.getBytes());
                GlobalApplication.mUserInfoManager.setUserExpiresInMilis(accessTokenInfoResponse.getExpiresInMillis());
                LoginToServer();
            }
        });
    }

    // 앱 서버에 로그인 한다.
    private void LoginToServer() {
        class t_loginToServer implements Runnable {
            @Override
            public void run() {
                try {
                    int buf_size = 512;
                    // 로그인 서버와 연결
                    Socket t_socket = new Socket();
                    t_socket.setReceiveBufferSize(buf_size);
                    t_socket.connect(new InetSocketAddress(getString(R.string.server_ip), Integer.parseInt(getString(R.string.login_server_port))), 1000);
                    DataInputStream networkDataReader = new DataInputStream(t_socket.getInputStream());
                    OutputStream networkDataWriter = t_socket.getOutputStream();

                    // 로그인 인증 패킷 전송
                    byte[] packet = Functions.concatBytes(Functions.longToBytes(user_id), kakao_token.getBytes());
                    networkDataWriter.write(packet);
                    networkDataWriter.flush();

                    // 로그인 결과로 앱 서버의 AccessToken을 얻음
                    app_token = new byte[64];
                    networkDataReader.read(app_token, 0, 64);
                    GlobalApplication.mUserInfoManager.setAppToken(app_token);
                    Log.d("AppToken : ", app_token.toString());

                    // FCM 푸시 토큰을 전송
                    networkDataWriter.write(FirebaseInstanceId.getInstance().getToken().getBytes());
                    networkDataWriter.flush();

                    // 로컬 DB 버전와 서버 DB를 동기화
                    DBHelper mDBHelper = DBHelper.getInstance(getApplicationContext());
                    int dbVersion = mDBHelper.getDBVersion();
                    packet = Functions.intToBytes(dbVersion);
                    networkDataWriter.write(packet);
                    networkDataWriter.flush();

                    packet = new byte[4];
                    networkDataReader.read(packet, 0, 4);
                    int db_size = Functions.bytesToInt(packet);
                    Log.d("Pakcet Size : ", String.valueOf(db_size));
                    if (db_size != 0) {
                        // csv 형태의 데이터를 받음
                        packet = new byte[db_size];

                        int offset = 0;
                        int len;
                        byte[] buffer = new byte[1024];
                        while (offset < db_size) {
                            len = networkDataReader.read(buffer);
                            if (len > 0) {
                                System.arraycopy(buffer, 0, packet, offset, len);
                                offset += len;
                            }
                        }
                        String[] csv = new String(packet, "utf-8").split("\\r?\\n");
                        mDBHelper.updateCategory(csv);
                    }

                    // 소켓 연결 종료
                    networkDataReader.close();
                    networkDataWriter.close();
                    t_socket.close();

                    if(GlobalApplication.mUserInfoManager.getTokenValidation()) redirectMainMenu();
                    else showFailDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                    showFailDialog();
                }
            }
        }
        Runnable t = new t_loginToServer();
        t.run();
    }

    void showFailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("앱 서버 로그인 실패");
        builder.setMessage("서버 점검 중이에요!\n잠시 후에 다시 시도해주세요");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginActivity.this.finish();
            }
        });
        builder.show();
    }

    // 스토어 버전 체크
    public class VersionChecker extends AsyncTask<String, String, String> {
        String newVersion;

        @Override
        protected String doInBackground(String... params) {
            try {
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + "package name" + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;
        }
    }

    // ActivityMain Activity로 전환
    private void redirectMainMenu() {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // 퍼미션 체크
    private boolean checkPermissions() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) return true;
        }
        return false;
    }

    // 퍼미션 요구 이유 체크
    private boolean showRequestPermission() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) return true;
        }
        return false;
    }

    //인터넷 연결상태 확인
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
