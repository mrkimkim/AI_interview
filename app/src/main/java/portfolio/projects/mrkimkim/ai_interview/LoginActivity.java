package portfolio.projects.mrkimkim.ai_interview;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

public class LoginActivity extends AppCompatActivity {
    private SessionCallback callback;

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

        // 기기 버전 체크 후 퍼미션 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissions()) {
                if (showRequestPermission()) {

                } else {
                    ActivityCompat.requestPermissions(this, permissions, 12345);
                }
            }
        }

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

        if (Session.getCurrentSession().isOpened()) requestMe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            if (Session.getCurrentSession().isOpened()) requestMe();
        }

        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) Logger.e(exception);
        }
    }

    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) { Log.d("onFailure", errorResult + "");}

            @Override
            public void onSessionClosed(ErrorResult errorResult) { Log.d("onSessionClosed", errorResult + "");};

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d("onSuccess", userProfile.toString());
                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNotSignedUp() { Log.e("onNotSignedUp", "onNotSignedUp");};
        });
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) return true;
        return false;
    }

    // 퍼미션 체크
    public boolean checkPermissions() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) return true;
        }
        return false;
    }

    // 퍼미션 요구 이유 체크
    public boolean showRequestPermission() {
        for (int i = 0; i < permissions.length; ++i) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    public void onClickUnlink(View v) {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        try {
            new AlertDialog.Builder(this)
                    .setMessage(appendMessage)
                    .setPositiveButton(getString(R.string.com_kakao_ok_button),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    UserManagement.requestUnlink(new UnLinkResponseCallback() {
                                        @Override
                                        public void onFailure(ErrorResult errorResult) {
                                            Logger.e(errorResult.toString());
                                        }

                                        @Override
                                        public void onSessionClosed(ErrorResult errorResult) {
                                            Toast.makeText(LoginActivity.this, "Sessons is Closed", Toast.LENGTH_LONG);
                                        }

                                        @Override
                                        public void onNotSignedUp() {

                                        }

                                        @Override
                                        public void onSuccess(Long result) {
                                            Toast.makeText(LoginActivity.this, "회원 탈퇴 완료", Toast.LENGTH_LONG);
                                        }
                                    });
                                    dialogInterface.dismiss();
                                }
                            })
                    .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
        } catch (KakaoException e) {
            e.printStackTrace();
        }
    }
}
