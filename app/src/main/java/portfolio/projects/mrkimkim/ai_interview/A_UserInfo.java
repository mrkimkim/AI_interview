package portfolio.projects.mrkimkim.ai_interview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import portfolio.projects.mrkimkim.ai_interview.NetworkModule.LoginActivity;
import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;
import portfolio.projects.mrkimkim.ai_interview.Utils.Functions;

import static portfolio.projects.mrkimkim.ai_interview.Utils.Functions.LoadThumbnail;

public class A_UserInfo extends AppCompatActivity {
    GlobalApplication.UserInfoManager userInfoManager;
    CircularImageView ib_userThumbnail;
    TextView tv_userNick, tv_userEmail, tv_userMsg, tv_userNumtry, tv_userUpvote, tv_userCredit;

    String thumbnailImagePath, userNick, userEmail, userMsg;
    int userNumtry, userUpvote, userCredit;

    Socket socket;
    InputStream networkReader;
    OutputStream networkWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        // UI를 로드
        loadUIElement();

        // 사용자 데이터를 불러온다.
        loadUserData();

        // 사용자 데이터를 UI에 설정한다
        setUserData();
    }

    /**
     * 사용자 상태 메시지를 변경하는 함수
     */
    public void ChangeUserMsg(View v) {
        // 다이얼로그를 띄운다.
        final AlertDialog.Builder builder = new AlertDialog.Builder(A_UserInfo.this);
        builder.setTitle("상태 메시지를 입력해 주세요");

        final EditText new_msg = new EditText(A_UserInfo.this);
        new_msg.setHint(userMsg);
        builder.setView(new_msg);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 새로운 입력값을 받아옴
                String value = new_msg.getText().toString();

                // 사용자 남김말을 변경
                userMsg = value;
                tv_userMsg.setText(value);

                // 로컬 데이터 패치
                GlobalApplication.mUserInfoManager.setUserMsg(value);

                // 원격 데이터 패치
                Thread t = new Thread(patchUserMsg);
                t.start();

                // 다이얼로그 종료
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 서버의 사용자 메시지를 업데이트 함
     */
    private Thread patchUserMsg = new Thread() {
        public void run() {
            try {
                // 소켓 설정
                setSocket(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port)));

                // 유저 인증 토큰을 전송함.
                NetworkService.Auth_User(networkReader, networkWriter);

                // 헤더를 전송함 Opcode 3000
                networkWriter.write(NetworkService.getHeader(3000));

                // UTF-8 인코딩 메시지 전송
                networkWriter.write(Functions.intToBytes(userMsg.getBytes("utf-8").length));
                networkWriter.write(userMsg.getBytes("utf-8"));
                networkWriter.flush();

                // 소켓을 닫음
                closeSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 사용자 탈퇴를 처리하는 함수
     */
    public void onClickUnlink(View v) {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManagement.requestUnlink(new UnLinkResponseCallback() {
                                    @Override
                                    public void onSessionClosed(ErrorResult errorResult) {
                                        redirectLoginActivity();
                                    }

                                    @Override
                                    public void onNotSignedUp() {

                                    }

                                    @Override
                                    public void onSuccess(Long result) {
                                        redirectLoginActivity();
                                    }
                                });
                                dialog.dismiss();
                            }
                        })
        .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 크레딧을 구매하는 함수
     */
    public void onClickBuyCredit(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.connectionFailedTitle));
        builder.setMessage(getResources().getString(R.string.connectionFailedBody));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                A_UserInfo.this.finish();
            }
        });
        builder.show();
    }

    /**
     * 로그인 화면으로 이동
     */
    public void redirectLoginActivity() {
        Intent intent = new Intent(A_UserInfo.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 서버와 통신할 소켓을 설정한다
     * @param ip
     * @param port
     * @return
     */
    public Socket setSocket(String ip, int port) {
        try {
            if (socket != null && socket.isConnected()) socket.close();
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            networkReader = socket.getInputStream();
            networkWriter = socket.getOutputStream();
            return socket;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 서버와 통신할 소켓을 닫는다.
     */
    public void closeSocket() {
        try {
            socket.close();
            networkReader.close();
            networkWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 레이아웃의 UI 요소들을 불러온다
     */
    public void loadUIElement() {
        // ImageButton
        ib_userThumbnail = (CircularImageView)findViewById(R.id.userinfo_userThumbnail);

        // TextView
        tv_userNick = (TextView)findViewById(R.id.userinfo_userNick);
        tv_userEmail = (TextView)findViewById(R.id.userinfo_userEmail);
        tv_userMsg = (TextView)findViewById(R.id.userinfo_userMsg);
        tv_userNumtry = (TextView)findViewById(R.id.userinfo_userNumtry);
        tv_userUpvote = (TextView)findViewById(R.id.userinfo_userUpvote);
        tv_userCredit = (TextView)findViewById(R.id.userinfo_userCredit);
    }

    /**
     * 유저 데이터를 로드한다
     */
    public void loadUserData() {
        thumbnailImagePath = GlobalApplication.mUserInfoManager.getUserProfile().getThumbnailImagePath();
        userNick = GlobalApplication.mUserInfoManager.getUserProfile().getNickname();
        userEmail = GlobalApplication.mUserInfoManager.getUserProfile().getEmail();
        userMsg = GlobalApplication.mUserInfoManager.getUserMsg();
        userNumtry = GlobalApplication.mUserInfoManager.getUserNumtry();
        userUpvote = GlobalApplication.mUserInfoManager.getUserUpvote();
        userCredit = GlobalApplication.mUserInfoManager.getUserCredit();
    };

    /**
     * 레이아웃 UI에 유저데이터를 표시한다
     */
    public void setUserData() {
        ib_userThumbnail.setImageDrawable(LoadThumbnail(thumbnailImagePath));
        tv_userNick.setText(userNick);
        tv_userEmail.setText(userEmail);
        tv_userMsg.setText(userMsg);
        tv_userNumtry.setText(String.valueOf(userNumtry));
        tv_userUpvote.setText(String.valueOf(userUpvote));
        tv_userCredit.setText(String.valueOf(userCredit));
    }

}
