package portfolio.projects.mrkimkim.ai_interview;

/**
 * Created by JHG on 2017-12-29.
 */

import android.app.Application;
import android.content.Context;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.util.Arrays;

public class GlobalApplication extends Application {
    public static GlobalApplication singleton;
    public static GlobalApplication getInstance() { return singleton; }

    public static UserInfoManager mUserInfoManager;

    public class UserInfoManager {
        final byte[] invalid_token = "                                                                ".getBytes();
        private long user_id = -1;
        private String user_email = null;
        private long user_expiresInMilis = -1;
        private byte[] app_token = null;
        private byte[] kakao_token = null;

        public void setUserId(long id) { user_id = id; }
        public long getUserId() {return user_id;}

        public void setAppToken(byte[] token) {
            app_token = new byte[token.length];
            System.arraycopy(token, 0, app_token, 0, token.length);
        }
        public byte[] getAppToken() { return app_token; }

        public void setKakaoToken(byte[] token) { kakao_token = token; }
        public byte[] getKakaoToken() { return kakao_token; }

        public void setUserEmail(String s) { user_email = s;}
        public String getUserEmail() { return user_email; }

        public void setUserExpiresInMilis(long val) { user_expiresInMilis = val; }
        public long getUserExpiresInMilis() { return user_expiresInMilis; }

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
    }
}