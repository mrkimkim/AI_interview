package portfolio.projects.mrkimkim.ai_interview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import portfolio.projects.mrkimkim.ai_interview.NetworkModule.NetworkService;

public class A_UserInfo extends AppCompatActivity {
    GlobalApplication.UserInfoManager userInfoManager;
    CircularImageView ib_userThumbnail;
    TextView tv_userNick, tv_userEmail, tv_userMsg, tv_userNumtry, tv_userUpvote, tv_userCredit;

    String thumbnailImagePath, userNick, userEmail, userMsg;
    int userNumtry, userUpvote, userCredit;

    Socket socket;
    InputStream networkReader;
    OutputStream networkWriter;

    public Socket setSocket(String ip, int port) {
        try {
            if (socket.isConnected()) socket.close();
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

    // 사용자 남김말을 패치함
    private Thread patchUserMsg = new Thread() {
        public void run() {
            try {
                byte[] packet;

                setSocket(getString(R.string.server_ip), Integer.parseInt(getString(R.string.cmd_server_port)));

                // 유저 인증 토큰을 전송함.
                NetworkService.Auth_User(networkReader, networkWriter);

                // 헤더를 전송함 Opcode 3000
                networkWriter.write(NetworkService.getHeader(3000));
                networkWriter.flush();

                // 메시지 패킷
                packet = new byte[4 + userMsg.length()];
            } catch (Exception e) {

            }
        }
    };

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

        // 소켓 설정
    }

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

    public void loadUserData() {
        thumbnailImagePath = GlobalApplication.mUserInfoManager.getUserProfile().getThumbnailImagePath();
        userNick = GlobalApplication.mUserInfoManager.getUserProfile().getNickname();
        userEmail = GlobalApplication.mUserInfoManager.getUserProfile().getEmail();
        userMsg = GlobalApplication.mUserInfoManager.getUserMsg();
        userNumtry = GlobalApplication.mUserInfoManager.getUserNumtry();
        userUpvote = GlobalApplication.mUserInfoManager.getUserUpvote();
        userCredit = GlobalApplication.mUserInfoManager.getUserCredit();
    };

    public static Drawable LoadThumbnail(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public void setUserData() {
        ib_userThumbnail.setImageDrawable(LoadThumbnail(thumbnailImagePath));
        tv_userNick.setText(userNick);
        tv_userEmail.setText(userEmail);
        tv_userMsg.setText(userMsg);
        tv_userNumtry.setText(String.valueOf(userNumtry));
        tv_userUpvote.setText(String.valueOf(userUpvote));
        tv_userCredit.setText(String.valueOf(userCredit));
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
                tv_userMsg.setText(value);

                // 로컬 데이터 패치
                GlobalApplication.mUserInfoManager.setUserMsg(value);

                // 원격 데이터 패치


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
}
