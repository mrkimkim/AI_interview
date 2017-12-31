package portfolio.projects.mrkimkim.ai_interview;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import portfolio.projects.mrkimkim.ai_interview.NetworkModule.LoginActivity;

public class MainMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void load_question_activity(View v) {
        Intent intent = new Intent(MainMenu.this, ChooseQuestion.class);
        startActivity(intent);
    }


    /*
    public void onClickUnlink(View view) {
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
                                            redirectLoginActivity();
                                        }

                                        @Override
                                        public void onNotSignedUp() {

                                        }

                                        @Override
                                        public void onSuccess(Long result) {
                                            Toast.makeText(MainMenu.this, "회원 탈퇴 완료", Toast.LENGTH_LONG).show();
                                            redirectLoginActivity();
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


    private void redirectLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    */


}
