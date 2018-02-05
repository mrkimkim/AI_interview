package portfolio.projects.mrkimkim.ai_interview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ActivityMain extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("App_Token : ", new String(GlobalApplication.mUserInfoManager.getAppToken()));
        Log.d("App_Token_Length : ", String.valueOf(GlobalApplication.mUserInfoManager.getAppToken().length));
    }

    public void onClickUserInfo(View v) {
        Intent intent = new Intent(ActivityMain.this, ActivityUserInfo.class);
        startActivity(intent);
    }

    public void onClickStartInterview(View v) {
        Intent intent = new Intent(ActivityMain.this, ActivityChooseCategory.class);
        startActivity(intent);
    }

    public void onClickSearchQuestion(View v) {
        Intent intent = new Intent(ActivityMain.this, ActivityInterviewVideo.class);
        startActivity(intent);
    }

    public void onClickInterviewResult(View v) {
        Intent intent = new Intent(ActivityMain.this, ActivityInterviewResult.class);
        startActivity(intent);
    }

    public void onClickGroupStudy(View v) {

    }

    public void onClickSetting(View v) {

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
                                            Toast.makeText(ActivityMain.this, "회원 탈퇴 완료", Toast.LENGTH_LONG).show();
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
