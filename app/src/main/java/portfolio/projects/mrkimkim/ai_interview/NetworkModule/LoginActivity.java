package portfolio.projects.mrkimkim.ai_interview.NetworkModule;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
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
import java.util.Arrays;

import portfolio.projects.mrkimkim.ai_interview.A_Main;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Thread Policy를 지정
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

    /**
     * KaKao 계정의 Token을 받아온다.
     */
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

    /**
     * 앱 서버에 로그인한다.
     */
    private void LoginToServer() {
        class t_loginToServer implements Runnable {
            @Override
            public void run() {
                try {
                    int buf_size = 512;
                    int packet_size = 0;
                    int offset = 0;
                    int len;

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


                    // 사용자 메시지, 총 시도 횟수, 받은 좋아요, 크레딧 정보를 받음
                    packet = new byte[12];
                    while (offset < 12) {
                        len = networkDataReader.read(packet, offset, 12 - offset);
                        if (len > 0) offset += len;
                    }
                    GlobalApplication.mUserInfoManager.setUserNumtry(Functions.bytesToInt(Arrays.copyOfRange(packet, 0, 4)));
                    GlobalApplication.mUserInfoManager.setUserUpvote(Functions.bytesToInt(Arrays.copyOfRange(packet, 4, 8)));
                    GlobalApplication.mUserInfoManager.setUserCredit(Functions.bytesToInt(Arrays.copyOfRange(packet,8 , 12)));

                    offset = 0;
                    packet = new byte[4];
                    networkDataReader.read(packet, 0, 4);
                    packet_size = Functions.bytesToInt(packet);
                    packet = new byte[packet_size];
                    while (offset < packet_size) {
                        len = networkDataReader.read(packet, offset, packet_size - offset);
                        if (len > 0) offset += len;
                    }
                    GlobalApplication.mUserInfoManager.setUserMsg(new String(packet, "utf-8"));


                    // 로그인 결과로 앱 서버의 AccessToken을 얻음
                    app_token = new byte[64];
                    offset = 0;
                    while (offset < 64) {
                        len = networkDataReader.read(app_token, offset , 64 - offset);
                        if (len > 0) offset += len;
                    }
                    GlobalApplication.mUserInfoManager.setAppToken(app_token);


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
                    if (db_size != 0) {
                        // csv 형태의 데이터를 받음
                        packet = new byte[db_size];
                        offset = 0;
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
        builder.setTitle(getResources().getString(R.string.connectionFailedTitle));
        builder.setMessage(getResources().getString(R.string.connectionFailedBody));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginActivity.this.finish();
            }
        });
        builder.show();
    }

    // A_Main Activity로 전환
    private void redirectMainMenu() {
        Intent intent = new Intent(this, A_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
