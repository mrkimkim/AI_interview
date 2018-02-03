package portfolio.projects.mrkimkim.ai_interview;

/**
 * Created by JHG on 2017-12-29.
 */

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.usermgmt.response.model.UserProfile;

import java.util.Arrays;

import portfolio.projects.mrkimkim.ai_interview.DBHelper.DBHelper;

public class GlobalApplication extends Application {
    public static GlobalApplication singleton;
    public static GlobalApplication getInstance() { return singleton; }

    public static DBHelper mDBHelper;
    public static UserInfoManager mUserInfoManager;

    public static String ApplicationDataPath;

    public class UserInfoManager {
        final byte[] invalid_token = "                                                                ".getBytes();

        private UserProfile userProfile;
        private String userMsg;
        private int userNumtry;
        private int userUpvote;
        private int userCredit;
        private byte[] app_token = null;
        private byte[] kakao_token = null;
        private long kakao_tokenExpiresInMilis = -1;

        // 사용자 프로필 관련 함수
        public void setUserProfile(UserProfile up) {userProfile = up;}
        public UserProfile getUserProfile() { return userProfile; }

        // 사용자 수치 정보
        public void setUserNumtry(int value) {this.userNumtry = value;}
        public void setUserUpvote(int value) {this.userUpvote = value;}
        public void setUserCredit(int value) {this.userCredit = value;}
        public int getUserNumtry() { return this.userNumtry;}
        public int getUserUpvote() { return this.userUpvote;}
        public int getUserCredit() { return this.userCredit;}

        // 토큰 정보 관련 함수
        public void setAppToken(byte[] token) {
            app_token = new byte[token.length];
            System.arraycopy(token, 0, app_token, 0, token.length);
        }
        public byte[] getAppToken() { return app_token; }

        public void setKakaoToken(byte[] token) { kakao_token = token; }
        public byte[] getKakaoToken() { return kakao_token; }

        public void setUserExpiresInMilis(long val) { kakao_tokenExpiresInMilis = val; }
        public long getUserExpiresInMilis() { return kakao_tokenExpiresInMilis; }

        public boolean getTokenValidation() {
            return(!Arrays.equals(app_token, invalid_token) && !Arrays.equals(kakao_token, invalid_token));
        }

    }

    private class KakaoSDKAdapter extends KakaoAdapter {
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return true;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return GlobalApplication.this.getApplicationContext();
                }
            };
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        KakaoSDK.init(new KakaoSDKAdapter());
        mUserInfoManager = new UserInfoManager();

        ApplicationDataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/interview/";

    }
}